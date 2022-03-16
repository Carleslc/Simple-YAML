package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.utils.StringUtils;

import java.io.Closeable;
import java.io.IOException;

public abstract class YamlCommentReader extends YamlCommentMapper implements Closeable {

    protected String currentLine;
    protected String trim;

    protected int indent;
    protected String key;

    protected int position = -1;
    protected char currentChar;

    protected boolean isEscaping = false;
    protected boolean isLiteral = false;
    protected ReadingQuoteStyle quoteNotation = ReadingQuoteStyle.NONE;

    protected boolean beginExplicit = false;
    protected ReadingExplicitStyle explicitNotation;

    protected KeyTree.Node currentNode;
    protected KeyTree.Node currentList;
    protected boolean isListElement = false;

    protected ReaderStage stage = ReaderStage.START;

    protected YamlCommentReader(final YamlConfigurationOptions options) {
        super(options);
    }

    protected abstract String readLine() throws IOException;

    protected synchronized boolean nextLine() throws IOException {
        this.currentLine = this.readLine();
        this.position = -1;
        this.currentChar = '\0';
        this.isListElement = false;
        if (this.currentLine != null) {
            this.stage = ReaderStage.NEW_LINE;
            boolean literal = this.isLiteral;
            int indent = this.readIndent();
            this.trim = this.currentLine.substring(indent).trim();
            this.checkSpecialLines(literal, indent);
            return true;
        } else {
            this.indent = 0;
            this.trim = null;
            this.stage = ReaderStage.END_OF_FILE;
            this.endExplicitNotation();
            return false;
        }
    }

    protected boolean nextChar() {
        if (this.hasNext()) {
            this.position++;
            this.currentChar = this.currentLine.charAt(this.position);
            return this.checkSpecialChars();
        }
        this.stage = ReaderStage.END_OF_LINE;
        return false;
    }

    protected boolean hasChar() {
        return this.stage != ReaderStage.END_OF_LINE && this.stage != ReaderStage.END_OF_FILE;
    }

    protected boolean hasNext() {
        return this.position + 1 < this.currentLine.length();
    }

    protected char peek(int offset) {
        return this.currentLine.charAt(this.position + offset);
    }

    protected boolean isBlank() {
        return this.trim.isEmpty();
    }

    public static boolean isSpace(char c) {
        return c == ' ' || c == '\t';
    }

    protected boolean isComment() {
        if (this.stage == ReaderStage.COMMENT) {
            return true;
        }
        if (this.currentChar == '#' && this.canStartComment()) {
            this.stage = ReaderStage.COMMENT;
            return true;
        }
        return false;
    }

    protected boolean canStartComment() {
        return this.position == 0 || this.stage == ReaderStage.QUOTE_CLOSE ||
                (!this.isInQuote() && this.position > 0 && isSpace(this.peek(-1)));
    }

    protected boolean isInQuote() {
        return this.quoteNotation != ReadingQuoteStyle.NONE;
    }

    protected boolean isExplicit() {
        return this.explicitNotation != null;
    }

    protected void endExplicitNotation() throws IOException {
        if (this.isExplicit()) {
            this.explicitNotation.finish();
            this.currentNode = this.explicitNotation.getNode();
            this.endExplicitBlock();
        }
        this.explicitNotation = null;
    }

    protected void processLine() throws IOException {
        // overriden in children to process the current line
    }

    @SuppressWarnings("unused")
    protected void processMultiline(boolean inQuoteBlock) throws IOException {
        // overriden in children to process the current line while reading a multiline value
    }

    @SuppressWarnings("unused")
    protected void endExplicitBlock() throws IOException {
        // overriden in children to process an explicit block
    }

    protected boolean isLiteralChar() {
        return this.currentChar == '|' || this.currentChar == '>';
    }

    protected void checkSpecialLines(final boolean wasLiteral, final int indent) throws IOException {
        if (wasLiteral && this.isLiteral) {
            if (this.quoteNotation != ReadingQuoteStyle.LITERAL) {
                // First line of the block scalar literal
                this.quoteNotation = ReadingQuoteStyle.LITERAL;
            } else if (indent <= this.indent) {
                // Indentation reset, block scalar literal finished
                this.quoteNotation = ReadingQuoteStyle.NONE;
                this.isLiteral = false;
                this.indent = indent;
                this.checkSpecialChars();
            }
        } else {
            this.indent = indent;
        }
        if (this.beginExplicit) {
            this.beginExplicit = false;
            this.endExplicitNotation();
            this.explicitNotation = new ReadingExplicitStyle(indent);
        } else if (this.isExplicit() && !this.isBlank() && indent <= this.explicitNotation.getIndentation()
                && this.trim.charAt(0) != ReadingExplicitStyle.VALUE) {
            this.endExplicitNotation();
        }
        if (this.currentList != null) {
            int currentListIndent = this.currentList.getIndentation();
            if (indent < currentListIndent || (!this.isListElement && indent == currentListIndent)) {
                // No more elements for the current list
                this.currentList = null;
            }
        }
    }

    protected boolean checkSpecialChars() {
        if (this.quoteNotation == ReadingQuoteStyle.NONE) {
            // Default notation
            if (!this.isLiteral && (this.stage == ReaderStage.NEW_LINE || this.stage == ReaderStage.AFTER_KEY)) {
                // Check opening quote
                if (this.currentChar == ReadingQuoteStyle.SINGLE.getChar()) {
                    this.inQuote(ReadingQuoteStyle.SINGLE);
                    return this.nextChar();
                } else if (this.currentChar == ReadingQuoteStyle.DOUBLE.getChar()) {
                    this.inQuote(ReadingQuoteStyle.DOUBLE);
                    return this.nextChar();
                } else if (this.isLiteralChar()) {
                    this.isLiteral = true; // Flag new lines to be a block scalar literal until indentation resets
                }
            }
        } else if (this.quoteNotation == ReadingQuoteStyle.SINGLE) {
            // Single quote notation
            if (!this.isEscaping) {
                if (this.currentChar == this.quoteNotation.getChar()) {
                    // Check if it is an escape or closing quote
                    this.isEscaping = true;
                    boolean hasChar = this.nextChar();
                    if (!hasChar || this.currentChar != this.quoteNotation.getChar()) {
                        // Closing single quote
                        this.inQuote(ReadingQuoteStyle.NONE);
                        this.isEscaping = false;
                    }
                    return hasChar;
                }
            } else {
                this.isEscaping = false;
            }
        } else if (this.quoteNotation == ReadingQuoteStyle.DOUBLE) {
            // Double quote notation
            if (!this.isEscaping) {
                if (this.currentChar == this.quoteNotation.getChar()) {
                    // Closing double quote
                    this.inQuote(ReadingQuoteStyle.NONE);
                    return this.nextChar();
                } else if (this.currentChar == '\\') {
                    this.isEscaping = true;
                    return this.nextChar();
                }
            } else {
                this.isEscaping = false;
            }
        }
        return true;
    }

    protected void inQuote(ReadingQuoteStyle quoteStyle) {
        this.quoteNotation = quoteStyle;

        if (this.hasChar()) {
            if (quoteStyle == ReadingQuoteStyle.NONE) {
                this.stage = ReaderStage.QUOTE_CLOSE;
            } else {
                this.stage = ReaderStage.QUOTE_OPEN;
            }
        }
    }

    protected boolean isSectionKey() {
        if (this.currentChar == ':' && (this.stage == ReaderStage.KEY || this.stage == ReaderStage.QUOTE_CLOSE)) {
            if (this.hasNext()) {
                if (isSpace(this.peek(+1))) {
                    // space after colon, valid key
                    this.nextChar();
                    this.stage = ReaderStage.AFTER_KEY;
                    this.readTag();
                    return true;
                }
                // no space after colon, thus the colon is not a key-value delimiter
                return false;
            }
            // end of line after colon, valid key
            return true;
        }
        return false;
    }

    protected void readTag() {
        this.readIndent(false);

        if (this.hasChar() && this.currentChar == '!' && this.hasNext() && this.peek(+1) == '!') {
            this.nextChar();

            //noinspection StatementWithEmptyBody
            while (this.nextChar() && !isSpace(this.currentChar)) {
                // skip
            }

            this.readIndent(false);
        }
    }

    protected int readIndent(boolean special) {
        int indent = 0;
        while (this.nextChar() && this.stage != ReaderStage.QUOTE_OPEN) {
            if (isSpace(this.currentChar)) {
                indent++;
            } else {
                if (special && this.canStartSpecialIndent(indent) && (this.isListChar() || this.isExplicitChar())) {
                    // Skip special indent (do not increment indent)
                    this.nextChar();
                    this.readIndent(!this.isListElement); // read additional spaces (like after - on a list item)
                }
                break;
            }
        }
        return indent;
    }

    protected int readIndent() {
        return readIndent(true);
    }

    protected boolean canStartSpecialIndent(int indent) {
        return this.stage == ReaderStage.NEW_LINE &&
                (this.quoteNotation == ReadingQuoteStyle.NONE || (this.quoteNotation == ReadingQuoteStyle.LITERAL && indent <= this.indent));
    }

    protected boolean isListChar() {
        if (this.currentChar == '-') {
            this.isListElement = this.nextIsSpace();
            return this.isListElement;
        }
        return false;
    }

    protected boolean isExplicitChar() {
        if (this.currentChar == ReadingExplicitStyle.KEY && this.nextIsSpace()) {
            this.beginExplicit = true;
            return true;
        } else if (this.isExplicit() && this.currentChar == ReadingExplicitStyle.VALUE && this.nextIsSpace()) {
            this.explicitNotation.valueStep();
            return true;
        }
        return false;
    }

    protected final boolean nextIsSpace() {
        return this.hasNext() && isSpace(this.peek(+1));
    }

    protected final boolean isMultiline() {
        return !this.hasChar() && this.isInQuote();
    }

    protected boolean hasKey() {
        if (this.isExplicit()) {
            return this.explicitNotation.isKey() || this.explicitNotation.isListKey;
        }
        return !this.isLiteral; // - |
    }

    protected String readKey() throws IOException {
        String key = null;

        boolean hasChar = this.hasChar();

        if (hasChar && this.hasKey()) {
            StringBuilder keyBuilder = new StringBuilder();

            boolean withinQuotes = this.isInQuote();

            this.stage = ReaderStage.KEY;

            boolean explicitLiteral = this.isLiteral && this.isExplicit();

            if (this.quoteNotation == ReadingQuoteStyle.LITERAL) {
                keyBuilder.append(this.currentLine.substring(this.position));
                this.skipToEnd();
            } else if (explicitLiteral) { // ? |
                if (this.isLiteralChar()) {
                    this.nextChar();
                }
                this.stage = ReaderStage.COMMENT;
                this.processMultiline(true);
                this.skipToEnd();
            } else {
                while (hasChar && !this.isSectionKey() && this.stage != ReaderStage.QUOTE_CLOSE && !this.isComment()) {
                    keyBuilder.append(this.currentChar);
                    hasChar = this.nextChar();
                }
            }

            if (explicitLiteral || this.isMultiline()) {
                this.readKeyMultiline(keyBuilder);
            }

            key = keyBuilder.toString();

            if (!withinQuotes) {
                key = key.trim();
            }
        }

        return key;
    }

    protected void readKeyMultiline(final StringBuilder keyBuilder) throws IOException {
        ReadingQuoteStyle lastQuote = this.quoteNotation;
        if (this.nextLine() && (!this.isExplicit() || this.explicitNotation.isKey())) {
            boolean inQuoteBlock = lastQuote != ReadingQuoteStyle.LITERAL || this.quoteNotation == lastQuote;
            this.processMultiline(inQuoteBlock);
            if (inQuoteBlock) {
                final String nextKey = this.readKey();
                if (nextKey != null) {
                    if (keyBuilder.length() > 0) {
                        keyBuilder.append(' ');
                    }
                    keyBuilder.append(nextKey);
                }
            } else {
                // End literal block, it is not multiline anymore
                this.processLine();
            }
        }
    }

    protected void readValue() throws IOException {
        boolean hasChar = this.hasChar();

        if (hasChar) {
            if (this.stage != ReaderStage.QUOTE_CLOSE) {
                this.stage = ReaderStage.VALUE;
            }
            if (this.quoteNotation == ReadingQuoteStyle.LITERAL) {
                this.skipToEnd();
            } else if (!this.isComment()) {
                while (hasChar && !this.isComment()) {
                    hasChar = this.nextChar();
                }
            }
        }

        if (this.isMultiline()) {
            this.readValueMultiline();
        }

        if (this.isComment()) {
            // Do not skip the comment indent
            while (this.position > 0 && isSpace(this.peek(-1))) {
                this.position--;
            }
        }

        // Value is not stored because is not relevant to track comments (we are only handling quotes and literal blocks here)
    }

    protected void readValueMultiline() throws IOException {
        ReadingQuoteStyle lastQuote = this.quoteNotation;
        if (this.nextLine() && (!this.isExplicit() || this.explicitNotation.isValue())) {
            boolean inQuoteBlock = lastQuote != ReadingQuoteStyle.LITERAL || this.quoteNotation == lastQuote;
            this.processMultiline(inQuoteBlock);
            if (inQuoteBlock) {
                this.readValue();
            } else {
                // End literal block, it is not multiline anymore
                this.processLine();
            }
        }
    }

    protected void skipToEnd() {
        this.position = this.currentLine.length() - 1;
        this.currentChar = this.peek(0);
        this.nextChar();
    }

    protected boolean isSectionEnd() {
        return this.currentNode != null && this.indent <= (this.currentNode.getIndentation() - this.options().indent());
    }

    protected KeyTree.Node track() throws IOException {
        if (this.quoteNotation == ReadingQuoteStyle.LITERAL) {
            // Currently in a literal block, cannot add comments in this line
            return null;
        }
        if (this.isExplicit()) {
            return this.trackExplicit();
        }

        this.key = this.readKey();

        if (this.isListElement) {
            this.trackListElement();
        } else {
            this.track(this.indent, this.key);
        }

        return this.currentNode;
    }

    protected void trackListElement() {
        if (this.currentList == null || (this.currentNode != null && this.indent > this.currentNode.indent)) {
            this.currentList = this.keyTree.findParent(this.indent + 2); // "- " prefix
            if (this.currentList.listSize == null || this.currentList.size() == 0) {
                this.currentList.isList(0);
            }
        }
        if (this.isExplicit() && this.indent == this.explicitNotation.getIndentation()) { // : - value
            this.currentNode = this.currentList.add(this.key);
        } else { // - value
            this.currentNode = this.currentList.add(this.indent, this.key);
        }
        this.currentList.isList(this.currentList.listSize + 1);
        this.currentNode.setElementIndex(this.currentList.listSize - 1);
    }

    protected KeyTree.Node trackExplicit() throws IOException {
        boolean addKey = this.explicitNotation.isKey();
        boolean isListKey = addKey && this.isListElement; // ? -

        this.key = this.readKey();

        if (addKey) {
            this.explicitNotation.addKey(this.key, isListKey);

            if (this.explicitNotation.isKey()) {
                return null; // may be more key lines to add
            }
        }

        this.currentNode = this.explicitNotation.track();

        if (!addKey && this.isListElement) {
            this.trackListElement();
        }

        return this.currentNode;
    }

    protected KeyTree.Node track(final int indent, final String key) {
        final KeyTree.Node parent = this.keyTree.findParent(indent);
        this.currentNode = parent.add(indent, key);
        return this.currentNode;
    }

    protected void clearCurrentNode() {
        super.clearNode(this.currentNode);
        this.currentNode = null;
    }

    protected void clearCurrentNodeIfNoComments() {
        super.clearNodeIfNoComments(this.currentNode);
        this.currentNode = null;
    }

    @Override
    public String toString() {
        return "YamlCommentReader{" +
                "currentLine='" + currentLine + '\'' +
                ", trim='" + trim + '\'' +
                ", stage=" + stage +
                ", indent=" + indent +
                ", key='" + key + '\'' +
                ", position=" + position +
                ", currentChar=" + currentChar +
                ", isEscaping=" + isEscaping +
                ", isLiteral=" + isLiteral +
                ", quoteNotation=" + quoteNotation +
                ", explicit= " + explicitNotation +
                ", keyTree=" + keyTree +
                '}';
    }

    protected enum ReaderStage {
        START,
        NEW_LINE,
        KEY,
        AFTER_KEY,
        VALUE,
        COMMENT,
        QUOTE_OPEN,
        QUOTE_CLOSE,
        END_OF_LINE,
        END_OF_FILE
    }

    public enum ReadingQuoteStyle {
        NONE ('\0'),
        SINGLE ('\''),
        DOUBLE ('"'),
        LITERAL ('|');

        private final char quote;

        ReadingQuoteStyle(char quote) {
            this.quote = quote;
        }

        public char getChar() {
            return this.quote;
        }
    }

    public final class ReadingExplicitStyle {

        public static final char KEY = '?';
        public static final char VALUE = ':';

        private char step;
        private final int indent;

        private StringBuilder key;
        private StringBuilder keyComment;
        private StringBuilder valueComment;

        private KeyTree.Node node;

        private boolean finished = false;
        private boolean isListKey = false;

        ReadingExplicitStyle(int indent) {
            this.step = KEY;
            this.indent = indent;
        }

        public boolean isKey() {
            return this.step == KEY;
        }

        public boolean isValue() {
            return this.step == VALUE;
        }

        public void valueStep() {
            this.step = VALUE;
        }

        public boolean isFinished() {
            return this.finished;
        }

        public KeyTree.Node track() {
            if (this.node == null) {
                this.node = YamlCommentReader.this.track(this.getIndentation(), this.getKey());
            }
            return this.node;
        }

        public void finish() {
            this.track();
            this.finished = true;
        }

        public int getIndentation() {
            return this.indent;
        }

        public KeyTree.Node getNode() {
            return this.node;
        }

        public String getKey() {
            if (this.key == null) {
                return "";
            }
            String key = this.key.toString();
            return this.isListKey ? key + ']' : key;
        }

        public void addKey(String key, final boolean isListKey) {
            if (key != null && !key.isEmpty()) {
                if (this.key == null) {
                    if (isListKey) {
                        this.isListKey = true;
                        key = '[' + key;
                    }
                    this.key = new StringBuilder(key);
                } else {
                    this.key.append(this.isListKey ? ", " : " ").append(key);
                }
            }
        }

        public String getKeyComment() {
            return this.keyComment != null ? this.keyComment.toString() : null;
        }

        public String getValueComment() {
            return this.valueComment != null ? this.valueComment.toString() : null;
        }

        public void addComment(final String comment) {
            if (this.isKey()) {
                this.keyComment = this.addComment(comment, this.keyComment);
            } else {
                this.valueComment = this.addComment(comment, this.valueComment);
            }
        }

        private StringBuilder addComment(final String comment, StringBuilder commentBuilder) {
            if (commentBuilder == null) {
                commentBuilder = new StringBuilder();
                if (this.isKey()) {
                    this.appendIndentComment(comment, commentBuilder);
                } else {
                    commentBuilder.append(comment);
                }
            } else {
                commentBuilder.append('\n');
                this.appendIndentComment(comment, commentBuilder);
            }
            return commentBuilder;
        }

        private void appendIndentComment(final String comment, final StringBuilder commentBuilder) {
            commentBuilder.append(StringUtils.indentation(this.getIndentation()));
            commentBuilder.append(StringUtils.stripIndentation(comment));
        }

        public String getStep() {
            return this.isFinished() ? "END" : (this.isKey() ? "KEY" : "VALUE");
        }

        @Override
        public String toString() {
            return "{" +
                    "step = " + this.getStep() +
                    ", indent = " + this.getIndentation() +
                    ", key = " + this.getKey() +
                    ", keyComment = " + this.getKeyComment() +
                    ", valueComment = " + this.getValueComment() +
                    "}";
        }
    }

}

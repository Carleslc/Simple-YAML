package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.utils.Validate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class YamlCommentReader extends YamlCommentMapper implements Closeable {

    protected final BufferedReader reader;

    protected String currentLine;
    protected String trim;

    protected int indent;
    protected String key;

    protected int position = -1;
    protected char currentChar;

    protected boolean isEscaping = false;
    protected boolean isLiteral = false;
    protected QuoteStyle quoteNotation = QuoteStyle.NONE;

    protected ReaderStage stage = ReaderStage.START;

    protected YamlCommentReader(final YamlConfigurationOptions options, final Reader reader) {
        super(options);
        Validate.notNull(reader, "Reader is null!");
        this.reader = new BufferedReader(reader);
    }

    protected boolean nextLine() throws IOException {
        this.currentLine = this.reader.readLine();
        this.position = -1;
        this.currentChar = '\0';
        if (this.currentLine != null) {
            this.stage = ReaderStage.NEW_LINE;
            int indent = this.readIndent();
            this.checkLiteral(indent);
            this.trim = this.currentLine.substring(indent).trim();
            return true;
        } else {
            this.indent = 0;
            this.trim = null;
            this.stage = ReaderStage.END_OF_FILE;
            return false;
        }
    }

    protected boolean nextChar() {
        this.position++;
        if (this.position < this.currentLine.length()) {
            this.currentChar = this.currentLine.charAt(this.position);
            return this.checkSpecials();
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

    protected boolean isBlank() {
        return this.trim.isEmpty();
    }

    protected boolean isSpace(char c) {
        return c == ' ' || c == '\t';
    }

    protected boolean isSpecialIndent(char c) {
        return c == '-' || c == '?' || c == ':';
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
                (!this.isInQuote() && this.position > 0 && isSpace(this.currentLine.charAt(this.position - 1)));
    }

    protected boolean isInQuote() {
        return this.quoteNotation != QuoteStyle.NONE;
    }

    protected boolean isLiteralChar() {
        return this.currentChar == '|' || this.currentChar == '>';
    }

    protected void checkLiteral(final int indent) {
        if (this.isLiteral) {
            if (this.quoteNotation != QuoteStyle.LITERAL) {
                // First line of the block scalar literal
                this.quoteNotation = QuoteStyle.LITERAL;
            } else if (indent <= this.indent) {
                // Indentation reset, block scalar literal finished
                this.quoteNotation = QuoteStyle.NONE;
                this.isLiteral = false;
                this.indent = indent;
            }
        } else {
            this.indent = indent;
        }
    }

    protected boolean checkSpecials() {
        if (this.quoteNotation == QuoteStyle.NONE) {
            // Default notation
            if (this.stage == ReaderStage.NEW_LINE || this.stage == ReaderStage.AFTER_KEY) {
                // Check opening quote
                if (this.currentChar == QuoteStyle.SINGLE.getChar()) {
                    this.inQuote(QuoteStyle.SINGLE);
                    return this.nextChar();
                } else if (this.currentChar == QuoteStyle.DOUBLE.getChar()) {
                    this.inQuote(QuoteStyle.DOUBLE);
                    return this.nextChar();
                } else if (this.stage == ReaderStage.AFTER_KEY && this.isLiteralChar()) {
                    this.isLiteral = true; // Flag new lines to be a block scalar literal until indentation resets
                }
            }
        } else if (this.quoteNotation == QuoteStyle.SINGLE) {
            // Single quote notation
            if (!this.isEscaping) {
                if (this.currentChar == QuoteStyle.SINGLE.getChar()) {
                    // Check if it is an escape or closing quote
                    this.isEscaping = true;
                    boolean hasNext = this.nextChar();
                    if (!hasNext || this.currentChar != QuoteStyle.SINGLE.getChar()) {
                        // Closing single quote
                        this.inQuote(QuoteStyle.NONE);
                        this.isEscaping = false;
                    }
                    return hasNext;
                }
            } else {
                this.isEscaping = false;
            }
        } else if (this.quoteNotation == QuoteStyle.DOUBLE) {
            // Double quote notation
            if (!this.isEscaping) {
                if (this.currentChar == this.quoteNotation.getChar()) {
                    // Closing double quote
                    this.inQuote(QuoteStyle.NONE);
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

    protected void inQuote(QuoteStyle quoteStyle) {
        this.quoteNotation = quoteStyle;

        if (quoteStyle == QuoteStyle.NONE) {
            this.stage = ReaderStage.QUOTE_CLOSE;
        } else {
            this.stage = ReaderStage.QUOTE_OPEN;
        }
    }

    protected boolean isSectionKey() {
        if (this.currentChar == ':' && (this.stage == ReaderStage.KEY || this.stage == ReaderStage.QUOTE_CLOSE)) {
            if (this.hasNext()) {
                if (this.isSpace(this.currentLine.charAt(this.position + 1))) {
                    // space after colon, valid key
                    this.nextChar();
                    this.stage = ReaderStage.AFTER_KEY;
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

    protected int readIndent() {
        int indent = 0;
        while (this.nextChar() && this.stage != ReaderStage.QUOTE_OPEN) {
            if (this.isSpace(this.currentChar)) {
                indent++;
            } else {
                if (this.isSpecialIndent(this.currentChar) && this.hasNext()
                        && this.isSpace(this.currentLine.charAt(this.position + 1))) {
                    // Skip special indent (do not increment indent)
                    this.nextChar();
                }
                break;
            }
        }
        return indent;
    }

    protected String readKey() {
        String key = null;

        if (this.currentLine != null) {
            StringBuilder keyBuilder = new StringBuilder();

            boolean hasChar = this.hasChar();
            boolean withinQuotes = this.isInQuote();

            while (hasChar && !this.isSectionKey() && this.stage != ReaderStage.QUOTE_CLOSE && !this.isComment()) {
                this.stage = ReaderStage.KEY;
                keyBuilder.append(this.currentChar);
                hasChar = this.nextChar();
            }

            key = keyBuilder.toString();

            if (!withinQuotes) {
                key = key.trim();
            }
        }

        return key;
    }

    protected void readValue() throws IOException {
        boolean hasChar = this.hasChar();
        boolean endOfValue = !hasChar || this.isComment();

        if (!endOfValue) {
            hasChar = this.nextChar();
            if (hasChar && this.stage != ReaderStage.QUOTE_CLOSE) {
                this.stage = ReaderStage.VALUE;
            }
            endOfValue = !hasChar || this.isComment();
        }

        while (!endOfValue) {
            hasChar = this.nextChar();
            endOfValue = !hasChar || this.isComment();
        }

        if (this.isMultiline() && this.nextLine()) {
            this.readValue();
        }

        if (this.isComment()) {
            // Do not skip the comment indent
            this.position--;
            while (this.position >= 0 && this.isSpace(this.currentLine.charAt(this.position))) {
                this.position--;
            }
            this.position++;
        }

        // Value is not stored because is not relevant to track comments (we are only handling quotes and literal blocks here)
    }

    protected final boolean isMultiline() {
        return !this.hasChar() && this.isInQuote();
    }

    protected KeyTree.Node track() throws IOException {
        if (this.quoteNotation == QuoteStyle.LITERAL) {
            return null;
        }
        this.key = this.readKey();
        return this.track(this.indent, this.key);
    }

    protected KeyTree.Node track(final int indent, final String key) {
        final KeyTree.Node parent = this.keyTree.findParent(indent);
        return parent.add(indent, key);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
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

    public enum QuoteStyle {
        NONE ('\0'),
        SINGLE ('\''),
        DOUBLE ('"'),
        LITERAL ('|');

        private final char quote;

        QuoteStyle(char quote) {
            this.quote = quote;
        }

        public char getChar() {
            return this.quote;
        }
    }

}

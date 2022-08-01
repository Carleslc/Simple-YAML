package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class YamlCommentParser extends YamlCommentReader {

    protected final BufferedReader reader;

    protected StringBuilder blockComment;
    protected boolean blockCommentStarted = false;
    protected boolean headerParsed = false;

    public YamlCommentParser(final YamlConfigurationOptions options, final Reader reader) {
        super(options);
        Validate.notNull(reader, "Reader is null!");
        this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public void parse() throws IOException {
        while (this.nextLine()) {
            this.processLine();
        }

        // Footer
        this.track();

        this.close();
    }

    @Override
    protected String readLine() throws IOException {
        return this.reader.readLine();
    }

    @Override
    protected void processLine() throws IOException {
        if (this.isBlank()) {
            this.appendLine();
        } else if (this.isComment()) {
            this.appendCommentLine();
        } else {
            this.track();
        }
    }

    protected void appendLine() {
        if (!this.isExplicit()) {
            if (this.blockComment == null) {
                this.blockComment = new StringBuilder();
            }
            this.blockComment.append('\n');
        }
    }

    protected void appendCommentLine() {
        this.trackSideCommentBelow();
        if (this.isExplicit()) {
            this.explicitNotation.addComment(this.currentLine);
        } else {
            if (this.blockComment == null) {
                this.blockComment = new StringBuilder(this.currentLine);
            } else {
                if (this.blockCommentStarted) {
                    // multiline comment
                    this.blockComment.append('\n');
                }
                this.blockComment.append(this.currentLine);
            }
            this.blockCommentStarted = true;
        }
    }

    @Override
    protected KeyTree.Node track() throws IOException {
        this.trackSideCommentBelow();
        this.currentNode = super.track();
        this.trackBlockComment(this.currentNode);
        this.trackSideComment(this.currentNode);
        return this.currentNode;
    }

    @Override
    protected void endExplicitBlock() throws IOException {
        this.trackBlockCommentExplicit(this.currentNode);
        this.trackSideComment(this.currentNode);
    }

    protected String trackBlockComment(final KeyTree.Node node) {
        String blockComment = null;
        if (node != null && this.blockComment != null && (!this.isExplicit() || this.explicitNotation.getNode() == node)) {
            blockComment = this.blockComment.toString();
            if (!this.headerParsed) {
                // Remove header from first key comment
                blockComment = removeHeader(blockComment, this.options());
                this.headerParsed = true;
            }
            this.setRawComment(node, blockComment, CommentType.BLOCK);
            this.blockComment = null;
            this.blockCommentStarted = false;
        }
        return blockComment;
    }

    protected void trackBlockCommentExplicit(final KeyTree.Node node) {
        String blockComment = this.trackBlockComment(node);
        final String explicitBlockComment = this.explicitNotation.getKeyComment();
        if (explicitBlockComment != null) {
            if (blockComment == null) {
                blockComment = node.getComment();
            }
            if (blockComment == null) {
                blockComment = explicitBlockComment;
            } else {
                blockComment += '\n' + explicitBlockComment;
            }
            this.setRawComment(node, blockComment, CommentType.BLOCK);
        }
    }

    public static String removeHeader(String blockComment, final YamlConfigurationOptions options) {
        final String header = options.headerFormatter().dump(options.header());
        if (header != null && !header.isEmpty()) {
            boolean headerWithBlankLine = header.endsWith("\n\n");
            if (headerWithBlankLine) {
                // header ends with a blank new line, but block comments end one line above, so we add a '\n' to strip the header
                blockComment += '\n';
            }
            blockComment = blockComment.replaceFirst(Pattern.quote(header), "");
            if (blockComment.isEmpty()) {
                blockComment = null; // blockComment was the header
            } else if (headerWithBlankLine) {
                blockComment = blockComment.substring(0, blockComment.length() - 1); // restore extra '\n'
            }
        }
        return blockComment;
    }

    protected void trackSideComment(final KeyTree.Node node) throws IOException {
        if (this.isExplicit()) {
            if (this.currentLine != null && !this.explicitNotation.isFinished()) {
                this.readValue();

                if (this.isComment() && this.isExplicit()) { // ensure it is still explicit, because reading multiline value it can be finished
                    final String comment = this.currentLine.substring(this.position);
                    if (node == null || node == this.explicitNotation.getNode()) {
                        this.explicitNotation.addComment(comment);
                    } else {
                        this.setSideComment(node, comment);
                    }
                }
            } else if (node != null) {
                this.setSideComment(node, this.explicitNotation.getValueComment());
            }
        } else if (this.currentLine != null && node != null) {
            this.readValue();

            if (this.isComment()) {
                this.setSideComment(node, this.currentLine.substring(this.position));
            }
        }
    }

    protected void setSideComment(final KeyTree.Node node, String sideComment) {
        if (sideComment != null && !sideComment.isEmpty() && !isSpace(sideComment.charAt(0))) {
            sideComment = " " + sideComment;
        }
        this.setRawComment(node, sideComment, CommentType.SIDE);
    }

    protected void trackSideCommentBelow() {
        if (this.isSectionEnd()) {
            // Indent level changed
            if (this.blockComment != null && this.blockCommentStarted) {
                // Add current block comment as a side comment below the last key
                String sideComment = this.getRawComment(this.currentNode, CommentType.SIDE);
                if (sideComment == null) {
                    sideComment = "";
                }
                sideComment += '\n';
                // Split trailing blank lines for next key
                final String[] split = StringUtils.splitTrailingNewLines(this.blockComment.toString());
                sideComment += split[0];
                if (split[1].isEmpty()) {
                    this.blockComment = null;
                } else {
                    this.blockComment = new StringBuilder(split[1]);
                }
                this.blockCommentStarted = false;
                this.setRawComment(this.currentNode, sideComment, CommentType.SIDE);
            }
            // Last key is not on the same indent level so next comment lines belong to the next key
            this.clearCurrentNodeIfNoComments();
        }
    }

    @Override
    protected void processMultiline(boolean inQuoteBlock) {
        if (this.isExplicit() && this.isComment()) {
            this.explicitNotation.addComment(this.currentLine.substring(this.position));
        }
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}

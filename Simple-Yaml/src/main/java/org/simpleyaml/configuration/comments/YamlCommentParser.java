package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.utils.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class YamlCommentParser extends YamlCommentReader {

    private StringBuilder blockComment;
    private boolean blockCommentStarted = false;
    private boolean headerParsed = false;

    private KeyTree.Node currentNode;

    public YamlCommentParser(final YamlConfigurationOptions options, final Reader reader) {
        super(options, reader);
    }

    public void parse() throws IOException {
        while (this.nextLine()) {
            if (this.isBlank()) {
                this.appendLine();
            } else if (this.isComment()) {
                this.appendCommentLine();
            } else {
                this.track();
            }
        }

        // Footer
        this.track();
    }

    private void appendLine() {
        if (this.blockComment == null) {
            this.blockComment = new StringBuilder();
        }
        this.blockComment.append('\n');
    }

    private void appendCommentLine() {
        this.trackSideCommentBelow();
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

    @Override
    protected KeyTree.Node track() throws IOException {
        this.currentNode = super.track();
        this.trackBlockComment(this.currentNode);
        this.trackSideComment(this.currentNode);
        return this.currentNode;
    }

    private void trackBlockComment(final KeyTree.Node node) {
        if (node != null && this.blockComment != null) {
            String blockComment = this.blockComment.toString();
            if (!this.headerParsed) {
                // Remove header from first key comment
                blockComment = this.removeHeader(blockComment);
                this.headerParsed = true;
            }
            this.setRawComment(node, blockComment, CommentType.BLOCK);
            this.blockComment = null;
        }
        this.blockCommentStarted = false;
    }

    private String removeHeader(String blockComment) {
        final String header = this.options().headerFormatter().dump(this.options().header());
        if (header != null && !header.isEmpty()) {
            blockComment = blockComment.replaceFirst(Pattern.quote(header), "");
            if (blockComment.isEmpty()) {
                blockComment = null; // blockComment was the header
            }
        }
        return blockComment;
    }

    private void trackSideComment(final KeyTree.Node node) throws IOException {
        if (node != null && this.currentLine != null) {
            this.readValue();

            if (this.isComment()) {
                String sideComment = this.currentLine.substring(this.position);
                if (!sideComment.isEmpty() && !this.isSpace(sideComment.charAt(0))) {
                    sideComment = " " + sideComment;
                }
                this.setRawComment(node, sideComment, CommentType.SIDE);
            }
        }
    }

    private void trackSideCommentBelow() {
        if (this.currentNode != null && this.indent <= (this.currentNode.getIndentation() - this.options().indent())) {
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
            this.currentNode = null;
        }
    }

}

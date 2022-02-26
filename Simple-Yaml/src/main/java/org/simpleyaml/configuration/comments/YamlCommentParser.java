package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class YamlCommentParser extends YamlCommentReader {

    private StringBuilder blockComment;
    private boolean blockCommentStarted = false;
    private boolean headerParsed = false;

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
        final KeyTree.Node node = super.track();
        this.trackBlockComment(node);
        this.trackSideComment(node);
        return node;
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

    private void trackSideComment(final KeyTree.Node node) throws IOException {
        if (node != null && this.currentLine != null) {
            this.readValue();

            // TODO Side comments below (dangling), matching indent with the current indent

            if (this.isComment()) {
                String sideComment = this.currentLine.substring(this.position);
                if (!sideComment.isEmpty() && !this.isSpace(sideComment.charAt(0))) {
                    sideComment = " " + sideComment;
                }
                this.setRawComment(node, sideComment, CommentType.SIDE);
            }
        }
    }

    private String removeHeader(String blockComment) {
        final String header = this.options().headerFormatter().dump(this.options().header());
        if (!header.isEmpty()) {
            blockComment = blockComment.replaceFirst(Pattern.quote(header), "");
            if (blockComment.isEmpty()) {
                blockComment = null; // blockComment was the header
            }
        }
        return blockComment;
    }

}

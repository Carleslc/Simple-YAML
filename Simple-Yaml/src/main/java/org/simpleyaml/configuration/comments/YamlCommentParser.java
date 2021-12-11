package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlCommentParser extends YamlCommentReader {

    private StringBuilder currentComment; // block comment

    private boolean inQuote = false;

    public YamlCommentParser(final YamlConfigurationOptions options, final Reader reader) {
        super(options, reader);
    }

    public void parse() throws IOException {
        while (this.nextLine()) {
            if (this.isBlank() || this.isComment()) {
                this.appendLine();
            } else {
                this.trackComment();
            }
        }

        // Last comment
        this.trackComment();

        this.reader.close();
    }

    private void appendLine() {
        if (this.currentComment == null) {
            this.currentComment = new StringBuilder(this.currentLine);
        } else {
            this.currentComment.append(this.currentLine);
        }
        this.currentComment.append('\n');
    }

    private void trackComment() {
        final KeyTree.Node node = this.track();
        if (this.currentComment != null) {
            node.setComment(this.currentComment.toString());
            this.currentComment = null;
        }
        this.setSideComment(node);
    }

    private void setSideComment(final KeyTree.Node node) {
        if (this.currentLine != null) {
            int lastSpace = 0;
            char[] chars = this.currentLine.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == ' ') {
                    lastSpace = i;
                }
                if (this.inQuote) {
                    if (c == '\'' || c == '\"') {
                        this.inQuote = false;
                    }
                } else {
                    if (c == '\'' || c == '\"') {
                        this.inQuote = true;
                    } else if (c == '#') {
                        node.setSideComment(this.currentLine.substring(lastSpace));
                        break;
                    }
                }
            }
        }
    }

}

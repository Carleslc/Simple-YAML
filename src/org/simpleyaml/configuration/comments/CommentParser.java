package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentParser extends CommentReader {

    private static final Pattern SIDE_COMMENT_REGEX = Pattern.compile("^[ \\t]*[^#\\s].*?([ \\t]*#.*)");

    private StringBuilder currentComment; // block comment

    public CommentParser(Reader reader) {
        super(reader);
    }

    private void appendLine() {
        if (currentComment == null) {
            currentComment = new StringBuilder(currentLine);
        } else {
            currentComment.append(currentLine);
        }
        currentComment.append('\n');
    }

    private void trackComment() {
        KeyTree.Node node = track();
        if (currentComment != null) {
            node.setComment(currentComment.toString());
            currentComment = null;
        }
        setSideComment(node);
    }

    private void setSideComment(KeyTree.Node node) {
        if (currentLine != null) {
            Matcher sideCommentMatcher = SIDE_COMMENT_REGEX.matcher(currentLine);
            if (sideCommentMatcher.matches()) {
                node.setSideComment(sideCommentMatcher.group(1));
            }
        }
    }

    public void parse() throws IOException {
        while (nextLine()) {
            if (isBlank() || isComment()) {
                appendLine();
            } else {
                trackComment();
            }
        }

        // Last comment
        trackComment();

        reader.close();
    }
}

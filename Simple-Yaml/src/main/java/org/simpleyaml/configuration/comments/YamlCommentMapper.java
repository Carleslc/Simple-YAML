package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

public class YamlCommentMapper implements Commentable {

    protected KeyTree keyTree;

    public YamlCommentMapper(final YamlConfigurationOptions options) {
        this.keyTree = new KeyTree(options);
    }

    @Override
    public void setComment(final String path, String comment, final CommentType type) {
        KeyTree.Node node = this.getNode(path);
        if (node == null) {
            node = this.keyTree.add(path);
        }
        if (comment == null || comment.isEmpty()) {
            this.setComment(node, null, type);
        } else if (comment.matches("\n+")) {
            this.setComment(node, comment, type);
        } else {
            comment = Commentable.COMMENT_PREFIX + comment;
            comment = comment.replaceAll("[ \\t]*\n", "\n" + Commentable.COMMENT_PREFIX);
            if (type == CommentType.BLOCK) {
                node.setComment(this.indent(comment, node.getIndentation()));
            } else {
                node.setSideComment(" " + comment);
            }
        }
    }

    @Override
    public String getComment(final String path, final CommentType type) {
        final KeyTree.Node node = this.getNode(path);
        if (node == null) {
            return null;
        }
        String comment = type == CommentType.BLOCK ? node.getComment() : node.getSideComment();
        if (comment != null) {
            comment = comment.replaceAll("[ \\t]*#+[ \\t]*", "").trim();
        }
        return comment;
    }

    protected KeyTree.Node getNode(final String path) {
        return this.keyTree.get(path);
    }

    private void setComment(final KeyTree.Node node, final String comment, final CommentType type) {
        if (type == CommentType.BLOCK) {
            node.setComment(comment);
        } else {
            node.setSideComment(comment);
        }
    }

    private String indent(final String s, final int n) {
        final String padding = this.padding(n);
        final String[] lines = s.split("\n");
        final StringBuilder builder = new StringBuilder(s.length() + n * lines.length);
        for (final String line : lines) {
            builder.append(padding).append(line).append('\n');
        }
        return builder.toString();
    }

    private String padding(final int n) {
        final StringBuilder builder = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

}

package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

public class CommentMapper implements Commentable {

    protected KeyTree keyTree;

    public CommentMapper(YamlConfigurationOptions options) {
        keyTree = new KeyTree(options);
    }

    protected KeyTree.Node getNode(String path) {
        return keyTree.get(path);
    }

    @Override
    public void setComment(String path, String comment, CommentType type) {
        KeyTree.Node node = getNode(path);
        if (node == null) {
            node = keyTree.add(path);
        }
        if (comment == null || comment.isEmpty()) {
            setComment(node, null, type);
        } else if (comment.matches("\n+")) {
            setComment(node, comment, type);
        } else {
            comment = "# " + comment.replaceAll("[ \\t]*\n", "\n# ");
            if (type == CommentType.BLOCK) {
                node.setComment(indent(comment, node.getIndentation()));
            } else {
                node.setSideComment(" " + comment);
            }
        }
    }

    private void setComment(KeyTree.Node node, String comment, CommentType type) {
        if (type == CommentType.BLOCK) {
            node.setComment(comment);
        } else {
            node.setSideComment(comment);
        }
    }

    @Override
    public String getComment(String path, CommentType type) {
        KeyTree.Node node = getNode(path);
        if (node == null) {
            return null;
        }
        String comment = type == CommentType.BLOCK ? node.getComment() : node.getSideComment();
        if (comment != null) {
            comment = comment.replaceAll("[ \\t]*#+[ \\t]*", "").trim();
        }
        return comment;
    }

    private String indent(String s, int n) {
        String padding = padding(n);
        String[] lines = s.split("\n");
        StringBuilder builder = new StringBuilder(s.length() + (n * lines.length));
        for (String line : lines) {
            builder.append(padding).append(line).append('\n');
        }
        return builder.toString();
    }

    private String padding(int n) {
        StringBuilder builder = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

}

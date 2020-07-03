package org.simpleyaml.configuration.comments;

public class CommentMapper implements Commentable {

    protected KeyTree keyTree = new KeyTree();

    protected KeyTree.Node getNode(String path) {
        return keyTree.get(path);
    }

    @Override
    public void addComment(String path, String comment, CommentType type) {
        KeyTree.Node node = getNode(path);
        if (node == null) {
            node = keyTree.add(path);
        }
        comment = "# " + comment.replace("\n", "\n# ");
        if (type == CommentType.BLOCK) {
            node.setComment(indent(comment + "\n", node.getIndentation()));
        } else {
            node.setSideComment(" " + comment);
        }
    }

    private String indent(String s, int n) {
        StringBuilder builder = new StringBuilder(n + s.length());
        for (int i = 0; i < n; i++) {
            builder.append(' ');
        }
        builder.append(s);
        return builder.toString();
    }

}

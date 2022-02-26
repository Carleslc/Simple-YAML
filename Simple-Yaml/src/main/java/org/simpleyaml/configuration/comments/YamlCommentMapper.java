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
        this.setFormattedComment(node, comment, type);
    }

    protected final void setFormattedComment(final KeyTree.Node node, final String comment, final CommentType type) {
        if (node == null) {
            return;
        }
        final String formattedComment = this.options().commentFormatter().dump(comment, type, node);
        this.setRawComment(node, formattedComment, type);
    }

    protected final void setRawComment(final KeyTree.Node node, final String comment, final CommentType type) {
        if (node == null) {
            return;
        }
        if (type == CommentType.BLOCK) {
            node.setComment(comment);
        } else {
            node.setSideComment(comment);
        }
    }

    @Override
    public String getComment(final String path, final CommentType type) {
        return getComment(this.getNode(path), type);
    }

    protected final String getComment(final KeyTree.Node node, final CommentType type) {
        final String raw = getRawComment(node, type);
        if (raw == null) {
            return null;
        }
        return this.options().commentFormatter().parse(raw, type, node);
    }

    protected final String getRawComment(final KeyTree.Node node, final CommentType type) {
        if (node == null) {
            return null;
        }
        return type == CommentType.BLOCK ? node.getComment() : node.getSideComment();
    }

    protected KeyTree.Node getNode(final String path) {
        return this.keyTree.get(path);
    }

    protected YamlConfigurationOptions options() {
        return (YamlConfigurationOptions) this.keyTree.options();
    }

}

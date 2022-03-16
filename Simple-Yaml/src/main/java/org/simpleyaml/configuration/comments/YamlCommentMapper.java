package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.io.IOException;
import java.util.function.Predicate;

public class YamlCommentMapper implements Commentable {

    protected final KeyTree keyTree;

    public YamlCommentMapper(final YamlConfigurationOptions options) {
        this(new YamlKeyTree(options));
    }

    protected YamlCommentMapper(final YamlKeyTree keyTree) {
        this.keyTree = keyTree;
    }

    @Override
    public void setComment(final String path, String comment, final CommentType type) {
        if (comment == null) {
            this.removeComment(this.getNode(path), type);
        } else {
            this.setFormattedComment(this.getOrAddNode(path), comment, type);
        }
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
        return this.getComment(this.getNode(path), type);
    }

    protected final String getComment(final KeyTree.Node node, final CommentType type) {
        final String raw = getRawComment(node, type);
        if (raw == null) {
            return null;
        }
        try {
            return this.options().commentFormatter().parse(raw, type, node);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse comment", e);
        }
    }

    protected final String getRawComment(final KeyTree.Node node, final CommentType type) {
        if (node == null) {
            return null;
        }
        return type == CommentType.BLOCK ? node.getComment() : node.getSideComment();
    }

    public String getRawComment(final String path, final CommentType type) {
        return this.getRawComment(this.getNode(path), type);
    }

    public void removeComment(final String path, final CommentType type) {
        this.removeComment(this.getNode(path), type);
    }

    protected final void removeComment(final KeyTree.Node node, final CommentType type) {
        if (node != null) {
            if (type == CommentType.BLOCK) {
                node.setComment(null);
            } else {
                node.setSideComment(null);
            }
        }
    }

    public KeyTree getKeyTree() {
        return this.keyTree;
    }

    protected YamlConfigurationOptions options() {
        return (YamlConfigurationOptions) this.keyTree.options();
    }

    public KeyTree.Node getNode(final String path) {
        return this.keyTree.get(path);
    }

    protected KeyTree.Node getPriorityNode(final String path) {
        return this.keyTree.getPriority(path);
    }

    protected KeyTree.Node getOrAddNode(final String path) {
        return this.keyTree.add(path);
    }

    /*
      Free memory of empty nodes
     */

    protected static final Predicate<KeyTree.Node> NO_COMMENTS = node -> node.getComment() == null && node.getSideComment() == null;

    protected void clearNodeIfNoComments(final KeyTree.Node node) {
        if (node != null) {
            KeyTree.Node parent = node.getParent();
            parent = parent != null ? parent : node;
            parent.clearIf(NO_COMMENTS);
        }
    }

    protected void clearNode(final KeyTree.Node node) {
        if (node != null) {
            KeyTree.Node parent = node.getParent();
            parent = parent != null ? parent : node;
            parent.clear();
        }
    }
}

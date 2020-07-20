package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class KeyTree {

    private final KeyTree.Node root = new KeyTree.Node(null, 0, "");

    private final Map<String, KeyTree.Node> nodes = new HashMap<>();

    private final YamlConfigurationOptions options;

    public KeyTree(final YamlConfigurationOptions options) {
        this.options = options;
    }

    public KeyTree.Node add(final String path) {
        KeyTree.Node parent = this.root;
        String key = path;
        if (path != null) {
            final int i = path.lastIndexOf('.');
            if (i >= 0) {
                final String parentPath = path.substring(0, i);
                key = path.substring(i + 1);
                parent = this.get(parentPath);
                if (parent == null) {
                    parent = this.add(parentPath);
                }
            }
        }
        return parent.add(key);
    }

    /**
     * Get the last node that can be a parent of a child with the indent provided.
     *
     * @param indent the indent to look for
     * @return the last most inner child that has less indent than the indent provided, or parent otherwise
     */
    public KeyTree.Node findParent(final int indent) {
        return this.findParent(this.root, indent);
    }

    /**
     * Get a child from its path.
     *
     * @param path the path of names separated by dot character to look for
     * @return the child that has the provided path or null if not found
     */
    public KeyTree.Node get(final String path) {
        return this.nodes.get(path);
    }

    public Set<String> keys() {
        return this.nodes.keySet();
    }

    public Set<Map.Entry<String, KeyTree.Node>> entries() {
        return this.nodes.entrySet();
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    private KeyTree.Node findParent(final KeyTree.Node parent, final int indent) {
        if (parent.children.isEmpty()) {
            return parent;
        }
        final KeyTree.Node last = parent.children.getLast();
        if (last.indent < indent) {
            return this.findParent(last, indent);
        }
        return parent;
    }

    public class Node {

        private final String name;

        private final KeyTree.Node parent;

        private final LinkedList<KeyTree.Node> children = new LinkedList<>();

        private final int indent;

        private String comment;

        private String sideComment;

        private String path;

        Node(final KeyTree.Node parent, final int indent, final String name) {
            this.parent = parent;
            this.indent = indent;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String getComment() {
            return this.comment;
        }

        public void setComment(final String comment) {
            this.comment = comment;
        }

        public String getSideComment() {
            return this.sideComment;
        }

        public void setSideComment(final String sideComment) {
            this.sideComment = sideComment;
        }

        public int getIndentation() {
            return this.indent;
        }

        public String getPath() {
            if (this.path == null) {
                this.path = this.getPath(this.parent, this.name, KeyTree.this.options.pathSeparator());
            }
            return this.path;
        }

        public KeyTree.Node add(final String key) {
            return this.add(this == KeyTree.this.root ? 0 : this.indent + KeyTree.this.options.indent(), key);
        }

        public KeyTree.Node add(final int indent, final String key) {
            return this.add(new KeyTree.Node(this, indent, key));
        }

        @Override
        public String toString() {
            return "{" +
                "indent=" + this.indent +
                ", name='" + this.name + '\'' +
                ", comment='" + this.comment + '\'' +
                ", side='" + this.sideComment + '\'' +
                ", children=" + this.children +
                '}';
        }

        private String getPath(final KeyTree.Node parent, String name, final char separator) {
            if (parent == null) {
                return name;
            }
            if (parent != KeyTree.this.root) {
                name = parent.name + separator + name;
            }
            return this.getPath(parent.parent, name, separator);
        }

        private KeyTree.Node add(final KeyTree.Node child) {
            this.children.add(child);
            KeyTree.this.nodes.put(child.getPath(), child);
            return child;
        }

    }

}

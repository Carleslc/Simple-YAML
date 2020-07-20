package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class KeyTree {

    private final Node root = new Node(null, 0, "");

    private final Map<String, Node> nodes = new HashMap<>();

    private final YamlConfigurationOptions options;

    public KeyTree(YamlConfigurationOptions options) {
        this.options = options;
    }

    public Node add(String path) {
        Node parent = root;
        String key = path;
        if (path != null) {
            int i = path.lastIndexOf('.');
            if (i >= 0) {
                String parentPath = path.substring(0, i);
                key = path.substring(i + 1);
                parent = get(parentPath);
                if (parent == null) {
                    parent = add(parentPath);
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
    public Node findParent(int indent) {
        return findParent(root, indent);
    }

    private Node findParent(Node parent, int indent) {
        if (parent.children.isEmpty()) {
            return parent;
        }
        Node last = parent.children.getLast();
        if (last.indent < indent) {
            return findParent(last, indent);
        }
        return parent;
    }

    /**
     * Get a child from its path.
     *
     * @param path the path of names separated by dot character to look for
     * @return the child that has the provided path or null if not found
     */
    public Node get(String path) {
        return nodes.get(path);
    }

    public Set<String> keys() {
        return nodes.keySet();
    }

    public Set<Map.Entry<String, Node>> entries() {
        return nodes.entrySet();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public class Node {

        private final String name;
        private String comment;
        private String sideComment;

        private final Node parent;

        private final LinkedList<Node> children = new LinkedList<>();

        private final int indent;

        private String path;

        Node(Node parent, int indent, String name) {
            this.parent = parent;
            this.indent = indent;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getSideComment() {
            return sideComment;
        }

        public void setSideComment(String sideComment) {
            this.sideComment = sideComment;
        }

        public int getIndentation() {
            return indent;
        }

        public String getPath() {
            if (path == null) {
                path = getPath(parent, name, options.pathSeparator());
            }
            return path;
        }

        private String getPath(Node parent, String name, char separator) {
            if (parent == null) {
                return name;
            }
            if (parent != root) {
                name = parent.name + separator + name;
            }
            return getPath(parent.parent, name, separator);
        }

        private Node add(Node child) {
            children.add(child);
            nodes.put(child.getPath(), child);
            return child;
        }

        public Node add(String key) {
            return add(this == root ? 0 : indent + options.indent(), key);
        }

        public Node add(int indent, String key) {
            return add(new Node(this, indent, key));
        }

        @Override
        public String toString() {
            return "{" +
                "indent=" + indent +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", side='" + sideComment + '\'' +
                ", children=" + children +
                '}';
        }
    }
}

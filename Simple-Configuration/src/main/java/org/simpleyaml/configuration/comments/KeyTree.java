package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.ConfigurationOptions;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class KeyTree implements Iterable<KeyTree.Node> {

    protected final KeyTree.Node root;

    protected final ConfigurationOptions options;

    public KeyTree(final ConfigurationOptions options) {
        Validate.notNull(options);
        this.options = options;
        this.root = this.createNode(null, 0, "");
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
     * Get the root node.
     * @return the root node of this key tree
     */
    public KeyTree.Node getRoot() {
        return this.root;
    }

    /**
     * Get a child from its path. May contain repetitions with list elements when using names (values can be repeated on a list).
     *
     * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
     * @return the child that has the provided path or null if not found
     */
    public KeyTree.Node get(final String path) {
        return this.root.get(path, false, false);
    }

    /**
     * Get a child from its path. Repetitions on list values are only allowed to keys inserted with priority using {@link #add(String)}.
     *
     * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
     * @return the child that has the provided path or null if not found
     */
    public KeyTree.Node getPriority(final String path) {
        return this.root.get(path, false, true);
    }

    /**
     * Get a child from its path. It is created if it does not exist.
     *
     * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
     * @return the child that has the provided path
     */
    public KeyTree.Node getOrAdd(final String path) {
        return this.root.get(path, true, false);
    }

    /**
     * Get a child from its path. It is created with priority if it does not exist.
     *
     * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
     * @return the child that has the provided path
     */
    public KeyTree.Node add(final String path) {
        return this.root.get(path, true, true);
    }

    public Set<String> keys() {
        return this.root.keys();
    }

    public List<KeyTree.Node> children() {
        return this.root.children();
    }

    public Set<Map.Entry<String, KeyTree.Node>> entries() {
        return this.root.entries();
    }

    public ConfigurationOptions options() {
        return this.options;
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    @Override
    public Iterator<Node> iterator() {
        return this.root.iterator();
    }

    protected KeyTree.Node findParent(final KeyTree.Node parent, final int indent) {
        final KeyTree.Node last = parent.getLast();
        if (last != null && last.indent < indent) {
            return this.findParent(last, indent);
        }
        return parent;
    }

    protected KeyTree.Node createNode(final KeyTree.Node parent, final int indent, final String key) {
        return new KeyTree.Node(parent, indent, key);
    }

    public class Node implements Iterable<KeyTree.Node> {

        protected final KeyTree.Node parent;

        protected String name;
        protected int indent;

        protected List<KeyTree.Node> children;
        protected Map<String, KeyTree.Node> indexByName; // allows repetitions
        protected Map<String, KeyTree.Node> priorityIndex; // nodes added programmatically (not parsed)
        protected Map<Integer, KeyTree.Node> indexByElementIndex; // parent list

        protected String comment;
        protected String sideComment;

        protected boolean isList; // parent
        protected Integer listSize; // parent
        protected Integer elementIndex; // children

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

        public KeyTree.Node getParent() {
            return this.parent;
        }

        public boolean isRootNode() {
            return this.parent == null;
        }

        public boolean isFirstNode() {
            if (!this.isRootNode() && this.parent.isRootNode()) {
                KeyTree.Node first = this.parent.getFirst();
                if (first.getName() == null && this.parent.children.size() > 1) { // footer
                    first = this.parent.children.get(1);
                }
                if (first == this) {
                    final Iterator<String> keys = KeyTree.this.options.configuration().getKeys(false).iterator();
                    return !keys.hasNext() || keys.next().equals(first.getName());
                }
            }
            return false;
        }

        public int getIndentation() {
            return this.indent;
        }

        /**
         * Get a child from its path, or optionally add a new one if it is not created.
         *
         * @param path the path of children names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
         * @param add if a new node must be added if it does not exist
         * @param priority if true the priority index is used to allow specific repetitions on list values (manually added), otherwise all nodes are allowed (loaded)
         * @return the child that has the provided path or null if not found and not added
         */
        protected KeyTree.Node get(final String path, boolean add, boolean priority) {
            KeyTree.Node node = null;
            if (path != null && (this.indexByName == null || !this.indexByName.containsKey(path))) {
                final int i = StringUtils.firstSeparatorIndex(path, KeyTree.this.options.pathSeparator());
                if (i >= 0) {
                    final String childPath = path.substring(0, i);
                    KeyTree.Node child = this.get(childPath, add, priority);
                    if (child == null) {
                        return null;
                    }
                    return child.get(path.substring(i + 1), add, priority);
                }
                Matcher listIndex = StringUtils.LIST_INDEX.matcher(path);
                if (listIndex.matches()) {
                    final String child = listIndex.group(1);
                    if (child != null && !child.isEmpty()) {
                        node = this.get(child, add, priority);
                        if (node == null) {
                            return null;
                        }
                    } else {
                        node = this;
                    }
                    return node.getElement(Integer.parseInt(listIndex.group(2)), add);
                }
            }
            if (priority && this.isList) {
                node = this.priorityIndex != null ? this.priorityIndex.get(path) : null;
                if (add && node == null && this.indexByName != null) {
                    node = this.indexByName.get(path);
                    if (node != null) {
                        this.setPriority(path, node);
                    }
                }
            } else if (this.indexByName != null) {
                node = this.indexByName.get(path);
            }
            if (node == null && add) {
                node = this.add(path, priority);
            }
            return node;
        }

        /**
         * Get a child from its path. May contain repetitions with list elements when using names (values can be repeated on a list).
         *
         * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
         * @return the child that has the provided path or null if not found
         */
        public KeyTree.Node get(final String path) {
            return this.get(path, false, false);
        }

        /**
         * Get a child from its path. Repetitions on list values are only allowed to keys inserted with priority using {@link #add(String)}.
         *
         * @param path the path of names to look for separated by {@link #options()} {@link ConfigurationOptions#pathSeparator()}
         * @return the child that has the provided path or null if not found
         */
        public KeyTree.Node getPriority(final String path) {
            return this.get(path, false, true);
        }

        /**
         * Get a child list element from its index, or optionally add a new one if it is not created.
         * <p>
         * <br>If <code>i</code> is negative then gets the child at index <code>{@link #size()} + i</code>
         * <br>Example: <code>node.get(-1)</code> gets the last child of <code>node</code>
         * </p>
         * @param i the index of the child
         * @param add if a new node must be added if it does not exist
         * @return the child with index i or null if not found and not created
         */
        protected KeyTree.Node getElement(int i, boolean add) {
            KeyTree.Node child = null;
            if (this.isList) {
                if (this.indexByElementIndex != null) {
                    child = this.indexByElementIndex.get(i);
                    if (child == null && !add) {
                        if (i < 0) {
                            child = this.indexByElementIndex.get(this.listSize + i);
                        } else {
                            child = this.indexByElementIndex.get(i - this.listSize);
                        }
                    }
                }
            } else if (!add) {
                child = this.get(i);
            }
            if (child == null && add) {
                child = this.addIndexed(i);
            }
            return child;
        }

        /**
         * Get a child list element from its index.
         * <p>
         * <br>If <code>i</code> is negative then gets the child element indexed by <code>{@link #size()} + i</code>
         * <br>Example: <code>node.get(-1)</code> gets the last child element of the <code>node</code> list.
         * </p>
         * @param i the index of the child element
         * @return the child with index i or null if not found
         */
        public KeyTree.Node getElement(int i) {
            return this.getElement(i, false);
        }

        /**
         * Get a child from its index.
         * <p>
         * <br>If <code>i</code> is negative then gets the child at index <code>{@link #size()} + i</code>
         * <br>Example: <code>node.get(-1)</code> gets the last child of <code>node</code>
         * </p>
         * @param i the index of the child
         * @return the child with index i or null if not found
         */
        public KeyTree.Node get(int i) {
            KeyTree.Node child = null;
            if (this.hasChildren()) {
                i = this.asListIndex(i, this.children.size());
                if (i >= 0 && i < this.children.size()) {
                    child = this.children.get(i);
                }
            }
            return child;
        }

        public KeyTree.Node getFirst() {
            if (!this.hasChildren()) {
                return null;
            }
            return this.children.get(0);
        }

        public KeyTree.Node getLast() {
            if (!this.hasChildren()) {
                return null;
            }
            return this.children.get(this.children.size() - 1);
        }

        public KeyTree.Node add(final String key) {
            return this.add(key, false);
        }

        public KeyTree.Node add(final int indent, final String key) {
            return this.add(indent, key, false);
        }

        protected KeyTree.Node add(final String key, final boolean priority) {
            int indent = this == KeyTree.this.root ? 0 : this.indent + KeyTree.this.options.indent();
            return this.add(indent, key, priority);
        }

        protected KeyTree.Node add(final int indent, final String key, final boolean priority) {
            final KeyTree.Node child = KeyTree.this.createNode(this, indent, key);
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.add(child);
            if (this.indexByName == null) {
                this.indexByName = new LinkedHashMap<>();
            }
            this.indexByName.putIfAbsent(key, child);
            if (priority) {
                this.setPriority(key, child);
            }
            child.checkList();
            return child;
        }

        protected void setPriority(final String key, final KeyTree.Node child) {
            if (this.priorityIndex == null) {
                this.priorityIndex = new LinkedHashMap<>();
            }
            this.priorityIndex.putIfAbsent(key, child);
        }

        protected void checkList() {
            if (this.name != null || this.elementIndex != null) {
                final Object value = this.getValue();
                if (value instanceof Collection) {
                    this.isList(((Collection<?>) value).size());
                }
            }
        }

        public Object getValue() {
            final String path = this.getPath();
            return path != null ? KeyTree.this.options.configuration().get(path) : null;
        }

        public boolean hasChildren() {
            return this.children != null && !this.children.isEmpty();
        }

        public List<KeyTree.Node> children() {
            return this.hasChildren() ? Collections.unmodifiableList(this.children) : Collections.emptyList();
        }

        public Set<String> keys() {
            return this.indexByName != null ? Collections.unmodifiableSet(this.indexByName.keySet()) : Collections.emptySet();
        }

        public Set<Map.Entry<String, KeyTree.Node>> entries() {
            return this.indexByName != null ? Collections.unmodifiableSet(this.indexByName.entrySet()) : Collections.emptySet();
        }

        public int size() {
            return this.hasChildren() ? this.children.size() : 0;
        }

        public boolean isList() {
            return this.isList;
        }

        public void isList(int listSize) {
            this.isList = true;
            this.listSize = listSize;
        }

        public void setElementIndex(int elementIndex) {
            if (this.parent != null) {
                if (this.parent.indexByElementIndex == null) {
                    this.parent.indexByElementIndex = new HashMap<>();
                } else if (this.elementIndex != null) {
                    this.parent.indexByElementIndex.remove(this.elementIndex);
                }

                this.elementIndex = elementIndex;

                this.parent.indexByElementIndex.put(this.elementIndex, this);
            }
        }

        public Integer getElementIndex() {
            return this.elementIndex;
        }

        public String getPath() {
            if (this.parent == null || this.parent == KeyTree.this.root) {
                return this.name;
            } else if (this.parent.isList && this.elementIndex != null) {
                return indexedName(this.parent.getPath(), this.elementIndex);
            }
            return this.getPathWithNameUnchecked();
        }

        public String getPathWithName() { // name may be repeated in lists
            if (this.parent == null || this.parent == KeyTree.this.root) {
                return this.name;
            }
            return this.getPathWithNameUnchecked();
        }

        private String getPathWithNameUnchecked() {
            char sep = KeyTree.this.options.pathSeparator();
            return this.parent.getPath() + sep + StringUtils.escape(this.name);
        }

        private String indexedName(String name, int listIndex) {
            return name + "[" + listIndex + "]";
        }

        private KeyTree.Node addIndexed(final int i) {
            KeyTree.Node child = null;
            Object value = this.getValue();
            if (value != null) {
                if (value instanceof Collection) {
                    final int size = ((Collection<?>) value).size();
                    if (!this.isList) {
                        this.isList(size);
                    }

                    if (value instanceof List) {
                        final int index = this.asListIndex(i, size);
                        if (index >= 0 && index < size) {
                            Object item = ((List<?>) value).get(index);
                            final String name = (item instanceof String || item instanceof Number || item instanceof Boolean)
                                    ? String.valueOf(item) : null;
                            child = this.add(name);
                        }
                    }
                } else {
                    if (value instanceof ConfigurationSection) {
                        value = ((ConfigurationSection) value).getValues(false);
                    }
                    if (value instanceof Map) {
                        final int mapSize = ((Map<?,?>) value).size();
                        final int index = this.asListIndex(i, mapSize);
                        if (index >= 0 && index < mapSize) {
                            Object key = null;
                            Iterator<?> it = ((Map<?,?>) value).keySet().iterator();
                            int j = -1;
                            while (it.hasNext() && ++j <= index) {
                                key = it.next();
                            }
                            if (key != null) {
                                child = this.add(String.valueOf(key));
                            }
                        }
                    }
                }
            }
            if (child == null) {
                child = this.add(null);
            }
            child.setElementIndex(i);
            child.checkList();
            return child;
        }

        private int asListIndex(int i, int size) {
            if (i < 0) {
                return size + i; // convert negative to positive indexing
            }
            return i;
        }

        protected void clearNode() {
            if (this.children != null) {
                this.children.clear();
                this.children = null;
            }
            if (this.indexByName != null) {
                this.indexByName.clear();
                this.indexByName = null;
            }
            if (this.priorityIndex != null) {
                this.priorityIndex.clear();
                this.priorityIndex = null;
            }
            if (this.indexByElementIndex != null) {
                this.indexByElementIndex.clear();
                this.indexByElementIndex = null;
            }
            if (this.parent != null) {
                if (this.parent.indexByName != null && this.parent.indexByName.get(this.name) == this) {
                    this.parent.indexByName.remove(this.name);

                    if (this.parent.priorityIndex != null) {
                        this.parent.priorityIndex.remove(this.name);
                    }

                    if (this.parent.indexByElementIndex != null && this.elementIndex != null) {
                        this.parent.indexByElementIndex.remove(this.elementIndex);
                    }
                }
            }
        }

        protected boolean clearIf(final Predicate<Node> condition, final boolean removeFromParent) {
            if (this.children != null) {
                this.children.removeIf(child -> child.clearIf(condition, false));
            }
            if (!this.hasChildren() && condition.test(this)) {
                this.clearNode();
                if (removeFromParent && this.parent != null) {
                    this.parent.children.remove(this);
                }
                return true;
            }
            return false;
        }

        public boolean clearIf(final Predicate<Node> condition) {
            return this.clearIf(condition, true);
        }

        public void clear() {
            this.clearNode();

            if (this.parent != null) {
                this.parent.children.remove(this);
            }
        }

        @Override
        public Iterator<Node> iterator() {
            return this.hasChildren() ? this.children.iterator() : Collections.emptyIterator();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("{");

            builder.append("indent=").append(this.indent)
                    .append(", path=").append(StringUtils.wrap(this.getPath()))
                    .append(", name=").append(StringUtils.wrap(this.name))
                    .append(", comment=").append(StringUtils.wrap(this.comment))
                    .append(", side=").append(StringUtils.wrap(this.sideComment));

            builder.append(", isList=").append(this.isList);

            if (this.isList) {
                builder.append("(").append(this.listSize).append(")");
            }

            builder.append(", children=");

            if (this.children != null) {
                builder.append('(').append(this.children.size()).append(')');
                builder.append(this.children.stream().map(KeyTree.Node::getName).collect(Collectors.joining(", ", "[", "]")));
            } else {
                builder.append("[]");
            }

            return builder.append('}').toString();
        }
    }
}

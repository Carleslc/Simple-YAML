package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

public class YamlKeyTree extends KeyTree {

    public YamlKeyTree(final YamlConfigurationOptions options) {
        super(options);
    }

    @Override
    public YamlConfigurationOptions options() {
        return (YamlConfigurationOptions) this.options;
    }

    @Override
    protected KeyTree.Node createNode(final KeyTree.Node parent, final int indent, final String key) {
        return new YamlCommentNode(parent, indent, key);
    }

    public class YamlCommentNode extends KeyTree.Node {

        YamlCommentNode(final Node parent, final int indent, final String name) {
            super(parent, indent, name);
        }

        @Override
        protected KeyTree.Node add(final String key, final boolean priority) {
            int indent = 0;
            if (this != YamlKeyTree.this.root) {
                indent = this.indent;
                if (this.isList) {
                    indent += YamlKeyTree.this.options().indentList();
                } else {
                    indent += YamlKeyTree.this.options.indent();
                }
            }
            return this.add(indent, key, priority);
        }

        @Override
        public void isList(int listSize) {
            super.isList(listSize);

            if (this.parent != null && this.parent.isList) {
                this.indent = this.parent.indent + YamlKeyTree.this.options().indentList() + 2; // "- " prefix
            }
        }
    }
}

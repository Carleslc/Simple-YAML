package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.utils.StringUtils;

/**
 * {@link YamlCommentFormat#BLANK_LINE} formatter
 */
public class BlankLineYamlCommentFormatter extends YamlCommentFormatter {

    public BlankLineYamlCommentFormatter() {
        super();
        this.stripPrefix(true).trim(false);
        blockFormatter.prefix('\n' + blockFormatter.prefixFirst(), blockFormatter.prefixMultiline());
    }

    @Override
    public String dump(String comment, CommentType type, KeyTree.Node node) {
        if (type == CommentType.SIDE) {
            final String defaultPrefixFirst = sideFormatter.prefixFirst();
            int indentation = node == null ? 0 : node.getIndentation();
            final String blankLineSideFirstPrefix = '\n' + StringUtils.indentation(indentation) + StringUtils.stripIndentation(defaultPrefixFirst);
            sideFormatter.prefix(blankLineSideFirstPrefix, sideFormatter.prefixMultiline());
            final String dump = super.dump(comment, type, node);
            sideFormatter.prefix(defaultPrefixFirst, sideFormatter.prefixMultiline());
            return dump;
        }
        return super.dump(comment, type, node);
    }

}

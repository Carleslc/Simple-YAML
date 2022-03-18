package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;

/**
 * {@link YamlCommentFormat#PRETTY} formatter
 */
public class PrettyYamlCommentFormatter extends YamlCommentFormatter {

    public PrettyYamlCommentFormatter() {
        this(new YamlCommentFormatterConfiguration());
    }

    public PrettyYamlCommentFormatter(final YamlCommentFormatterConfiguration blockFormatter) {
        this(blockFormatter, new YamlSideCommentFormatterConfiguration());
    }

    public PrettyYamlCommentFormatter(final YamlCommentFormatterConfiguration blockFormatter, final YamlSideCommentFormatterConfiguration sideFormatter) {
        super(blockFormatter, sideFormatter);
        this.stripPrefix(true).trim(true);
    }

    @Override
    public String dump(final String comment, final CommentType type, final KeyTree.Node node) {
        if (type == CommentType.BLOCK && node != null && node.getIndentation() == 0 && !node.isFirstNode()) { // Block comment for root keys except the first key
            final YamlCommentFormatterConfiguration blockCommentFormatterConfiguration = this.formatterConfiguration(CommentType.BLOCK);
            final String defaultPrefixFirst = blockCommentFormatterConfiguration.prefixFirst();
            final String defaultPrefixMultiline = blockCommentFormatterConfiguration.prefixMultiline();

            // Prepend default first prefix with a blank line
            blockCommentFormatterConfiguration.prefix('\n' + defaultPrefixFirst, defaultPrefixMultiline);

            final String dump = super.dump(comment, type, node);

            // Reset first prefix to default
            blockCommentFormatterConfiguration.prefix(defaultPrefixFirst, defaultPrefixMultiline);

            return dump;
        }
        return super.dump(comment, type, node);
    }

}

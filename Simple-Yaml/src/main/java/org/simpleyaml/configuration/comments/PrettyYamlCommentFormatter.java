package org.simpleyaml.configuration.comments;

/**
 * {@link YamlCommentFormat#PRETTY} formatter
 */
public class PrettyYamlCommentFormatter extends YamlCommentFormatter {

    public PrettyYamlCommentFormatter() {
        super();
        this.stripPrefix(true).trim(true);
    }

    @Override
    public String dump(String comment, CommentType type, KeyTree.Node node) {
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

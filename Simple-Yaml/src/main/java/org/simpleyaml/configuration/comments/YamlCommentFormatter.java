package org.simpleyaml.configuration.comments;

import org.simpleyaml.utils.StringUtils;

import java.util.Objects;

public class YamlCommentFormatter implements CommentFormatter {

    private final YamlCommentFormatterConfiguration blockFormatter;
    private final YamlSideCommentFormatterConfiguration sideFormatter;

    public YamlCommentFormatter(YamlCommentFormatterConfiguration blockFormatter, YamlSideCommentFormatterConfiguration sideFormatter) {
        this.blockFormatter = blockFormatter;
        this.sideFormatter = sideFormatter;
    }

    public YamlCommentFormatter(YamlCommentFormatterConfiguration blockFormatter) {
        this(blockFormatter, new YamlSideCommentFormatterConfiguration());
        this.stripPrefix(true);
    }

    public YamlCommentFormatter() {
        this(new YamlCommentFormatterConfiguration());
    }

    @Override
    public String parse(String raw, CommentType type, KeyTree.Node node) {
        YamlCommentFormatterConfiguration formatterConfiguration = formatterConfiguration(type);

        String prefixFirst = StringUtils.stripIndentation(formatterConfiguration.prefixFirst());
        String prefixMultiline = StringUtils.stripIndentation(formatterConfiguration.prefixMultiline());

        StringBuilder commentBuilder = new StringBuilder();

        String[] lines = StringUtils.lines(raw);

        boolean strip = formatterConfiguration.stripPrefix();

        if (lines.length > 0) {
            commentBuilder.append(parseCommentLine(lines[0], prefixFirst, strip));
        }

        for (int i = 1; i < lines.length; i++) {
            commentBuilder.append('\n').append(parseCommentLine(lines[i], prefixMultiline, strip));
        }

        final String comment = commentBuilder.toString();

        return formatterConfiguration.trim() ? comment.trim() : comment;
    }

    protected String parseCommentLine(String line, String prefix, boolean strip) {
        String commentLine = StringUtils.stripIndentation(line);
        if (strip) {
            commentLine = StringUtils.stripPrefix(commentLine, prefix, YamlCommentFormatterConfiguration.COMMENT_INDICATOR);
        }
        return commentLine;
    }

    @Override
    public String dump(String comment, CommentType type, KeyTree.Node node) {
        final YamlCommentFormatterConfiguration formatterConfiguration = this.formatterConfiguration(type);

        String prefix = null;
        String prefixMultiline = null;
        int indentation = node.getIndentation();

        if (comment != null) {
            if (StringUtils.allLinesArePrefixedOrBlank(comment, YamlCommentFormatterConfiguration.COMMENT_INDICATOR)) {
                if (type == CommentType.SIDE && !comment.startsWith(" ")) {
                    prefix = " ";
                    prefixMultiline = "";
                }
            } else {
                prefix = formatterConfiguration.prefixFirst();
                prefixMultiline = formatterConfiguration.prefixMultiline();
            }
        }

        return CommentFormatter.format(indentation, prefix, prefixMultiline, comment, type, formatterConfiguration.suffixMultiline(), formatterConfiguration.suffixLast());
    }

    public YamlCommentFormatterConfiguration formatterConfiguration(CommentType type) {
        return type == CommentType.BLOCK ? this.blockFormatter : this.sideFormatter;
    }

    /**
     * Set if stripping the prefix is desired.
     * <p></p>
     * If strip is true then the comment prefix will be stripped away.
     * <p></p>
     * Default is true.
     * @param strip if stripping the prefix is desired
     * @return this object, for chaining
     */
    public YamlCommentFormatter stripPrefix(final boolean strip) {
        this.blockFormatter.stripPrefix(strip);
        this.sideFormatter.stripPrefix(strip);
        return this;
    }

    /**
     * Set if leading and trailing spaces and blank lines at the beginning and end of the comments should be stripped away.
     * <p>
     * This does not affect to every line of a multiline comment. Only to the beginning and end of the whole comment.
     * <p></p>
     * Default is true.
     * @param trim if {@link String#trim} should be applied to comments
     * @return this object, for chaining
     */
    public YamlCommentFormatter trim(final boolean trim) {
        this.blockFormatter.trim(trim);
        this.sideFormatter.trim(trim);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YamlCommentFormatter that = (YamlCommentFormatter) o;
        return Objects.equals(blockFormatter, that.blockFormatter) && Objects.equals(sideFormatter, that.sideFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockFormatter, sideFormatter);
    }
}

package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * Comment formatter to parse and dump YAML comments (using the # prefix).
 * <p>
 * Default format is {@link YamlCommentFormat#DEFAULT}.
 */
public class YamlCommentFormatter implements CommentFormatter {

    protected final YamlCommentFormatterConfiguration blockFormatter;
    protected final YamlSideCommentFormatterConfiguration sideFormatter;

    public YamlCommentFormatter(final YamlCommentFormatterConfiguration blockFormatter, final YamlSideCommentFormatterConfiguration sideFormatter) {
        Validate.notNull(blockFormatter, "blockFormatter configuration cannot be null!");
        Validate.notNull(blockFormatter, "sideFormatter configuration cannot be null!");
        this.blockFormatter = blockFormatter;
        this.sideFormatter = sideFormatter;
    }

    public YamlCommentFormatter(final YamlCommentFormatterConfiguration blockFormatter) {
        this(blockFormatter, new YamlSideCommentFormatterConfiguration());
        this.stripPrefix(true);
    }

    public YamlCommentFormatter() {
        this(new YamlCommentFormatterConfiguration());
    }

    @Override
    public String parse(final Reader raw, final CommentType type, final KeyTree.Node node) throws IOException {
        if (raw == null) {
            return null;
        }

        final YamlCommentFormatterConfiguration formatterConfiguration = this.formatterConfiguration(type);

        // Remove prefix indentation so the comment prefix can be stripped ignoring the indentation
        final String prefixFirst = StringUtils.stripIndentation(formatterConfiguration.prefixFirst());
        final String prefixMultiline = StringUtils.stripIndentation(formatterConfiguration.prefixMultiline());

        try (final BufferedReader reader = raw instanceof BufferedReader ? (BufferedReader) raw : new BufferedReader(raw)) {
            StringBuilder commentBuilder = new StringBuilder();

            boolean strip = formatterConfiguration.stripPrefix();

            final String firstLine = reader.readLine();

            if (firstLine != null) {
                // Append first line without indentation and optional first prefix
                commentBuilder.append(parseCommentLine(firstLine, prefixFirst, strip));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Append remaining lines without indentation and optional multiline prefix
                commentBuilder.append('\n').append(parseCommentLine(line, prefixMultiline, strip));
            }

            final String comment = commentBuilder.toString();

            // If set, trim leading and trailing space and blank lines
            return formatterConfiguration.trim() ? comment.trim() : comment;
        }
    }

    protected String parseCommentLine(final String line, final String prefix, final boolean strip) {
        String commentLine = StringUtils.stripIndentation(line);
        if (strip) {
            // Remove comment prefix or first # character if line do not start with the provided comment prefix
            commentLine = StringUtils.stripPrefix(commentLine, prefix, YamlCommentFormatterConfiguration.COMMENT_INDICATOR);
        }
        return commentLine;
    }

    @Override
    public String dump(final String comment, final CommentType type, final KeyTree.Node node) {
        final YamlCommentFormatterConfiguration formatterConfiguration = this.formatterConfiguration(type);

        String prefix = null;
        String prefixMultiline = null;

        if (comment != null) {
            // If all lines are blank or already prefixed with a comment prefix # then do not add additional formatting
            if (StringUtils.allLinesArePrefixedOrBlank(comment, YamlCommentFormatterConfiguration.COMMENT_INDICATOR)) {
                // Ensure that side comments are prefixed with at least a space (otherwise it would not be a valid yaml comment in plain style)
                if (type == CommentType.SIDE && !comment.startsWith(" ")) {
                    prefix = " ";
                    prefixMultiline = "";
                }
            } else {
                prefix = formatterConfiguration.prefixFirst();
                prefixMultiline = formatterConfiguration.prefixMultiline();
            }
        }

        // Apply the format for every line (indentation, first line prefix, multiline prefix, multiline suffix and last line suffix)
        return CommentFormatter.format(node.getIndentation(),
                prefix, prefixMultiline,
                comment, type,
                formatterConfiguration.suffixMultiline(), formatterConfiguration.suffixLast());
    }

    public final YamlCommentFormatterConfiguration blockFormatter() {
        return this.blockFormatter;
    }

    public final YamlSideCommentFormatterConfiguration sideFormatter() {
        return this.sideFormatter;
    }

    public final YamlCommentFormatterConfiguration formatterConfiguration(final CommentType type) {
        return type == CommentType.BLOCK ? this.blockFormatter : this.sideFormatter;
    }

    /**
     * Set if stripping the prefix is desired.
     * <p>If strip is true then the comment prefix will be stripped away.</p>
     * <p>
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
     * <p>This does not affect to every line of a multiline comment. Only to the beginning and end of the whole comment.</p>
     * <p>
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YamlCommentFormatter that = (YamlCommentFormatter) o;
        return Objects.equals(blockFormatter, that.blockFormatter) && Objects.equals(sideFormatter, that.sideFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockFormatter, sideFormatter);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n" +
                "blockFormatter=" + blockFormatter +
                ",\nsideFormatter=" + sideFormatter +
                "\n}";
    }
}

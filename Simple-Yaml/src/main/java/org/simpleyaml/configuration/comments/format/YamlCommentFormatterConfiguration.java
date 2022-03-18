package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.util.Objects;

public class YamlCommentFormatterConfiguration extends CommentFormatterConfiguration {

    public static final String COMMENT_INDICATOR = "#";
    public static final String DEFAULT_COMMENT_PREFIX = COMMENT_INDICATOR + " ";

    private boolean stripPrefix = false;
    private boolean trim = true;

    public YamlCommentFormatterConfiguration() {
        this(DEFAULT_COMMENT_PREFIX);
    }

    public YamlCommentFormatterConfiguration(final String prefix) {
        this.prefix(prefix);
    }

    public YamlCommentFormatterConfiguration(final String prefix, final String prefixMultiline) {
        this.prefix(prefix, prefixMultiline);
    }

    @Override
    public YamlCommentFormatterConfiguration prefix(final String prefix) {
        checkCommentPrefix(prefix);
        super.prefix(prefix, prefix);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration prefix(final String prefixFirst, final String prefixMultiline) {
        checkCommentPrefix(prefixFirst);
        checkCommentPrefixMultiline(prefixMultiline);
        super.prefix(prefixFirst, prefixMultiline);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration suffix(final String suffixLast) {
        checkCommentSuffix(suffixLast);
        super.suffix(suffixLast);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration suffix(final String suffixLast, final String suffixMultiline) {
        checkCommentSuffix(suffixLast);
        checkCommentSuffixMultiline(suffixMultiline);
        super.suffix(suffixLast, suffixMultiline);
        return this;
    }

    public YamlCommentFormatterConfiguration stripPrefix(final boolean stripPrefix) {
        this.stripPrefix = stripPrefix;
        return this;
    }

    public boolean stripPrefix() {
        return this.stripPrefix;
    }

    public YamlCommentFormatterConfiguration trim(final boolean trim) {
        this.trim = trim;
        return this;
    }

    public boolean trim() {
        return this.trim;
    }

    protected void checkCommentPrefix(final String commentPrefix) {
        Validate.notNull(commentPrefix, "Comment prefix cannot be null");

        final String[] prefixLines = StringUtils.lines(commentPrefix, false);
        final int lastLineIndex = prefixLines.length - 1;

        for (int i = 0; i <= lastLineIndex; i++) {
            final String line = prefixLines[i].trim();
            if (i == lastLineIndex && !line.startsWith(COMMENT_INDICATOR)) {
                throw new IllegalArgumentException("Last prefix line must be optional space followed by a " + COMMENT_INDICATOR);
            } else if (!(line.isEmpty() || line.startsWith(COMMENT_INDICATOR))) {
                throw new IllegalArgumentException("All comment prefix lines must be blank or optional space followed by a " + COMMENT_INDICATOR);
            }
        }
    }

    protected void checkCommentPrefixMultiline(final String commentPrefix) {
        this.checkCommentPrefix(commentPrefix);
    }

    protected void checkCommentSuffix(final String commentSuffix) {
        Validate.notNull(commentSuffix, "Comment suffix cannot be null");
        Validate.isTrue(StringUtils.allLinesArePrefixedOrBlank(StringUtils.afterNewLine(commentSuffix), COMMENT_INDICATOR),
                "All comment suffix lines must be blank or optional space followed by a " + COMMENT_INDICATOR);
    }

    protected void checkCommentSuffixMultiline(final String commentSuffix) {
        this.checkCommentSuffix(commentSuffix);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        YamlCommentFormatterConfiguration that = (YamlCommentFormatterConfiguration) o;
        return stripPrefix == that.stripPrefix && trim == that.trim;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stripPrefix, trim);
    }

    @Override
    public String toString() {
        return "{" +
                "stripPrefix=" + stripPrefix +
                ", trim=" + trim +
                ", " + super.toString() +
                '}';
    }
}

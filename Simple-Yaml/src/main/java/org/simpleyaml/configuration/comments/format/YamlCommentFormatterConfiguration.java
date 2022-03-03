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

    public YamlCommentFormatterConfiguration(String prefix) {
        this.prefix(prefix);
    }

    public YamlCommentFormatterConfiguration(String prefix, String prefixMultiline) {
        this.prefix(prefix, prefixMultiline);
    }

    @Override
    public YamlCommentFormatterConfiguration prefix(String prefix) {
        checkCommentPrefix(prefix);
        super.prefix(prefix, prefix);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration prefix(String prefixFirst, String prefixMultiline) {
        checkCommentPrefix(prefixFirst);
        checkCommentPrefixMultiline(prefixMultiline);
        super.prefix(prefixFirst, prefixMultiline);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration suffix(String suffixLast) {
        checkCommentPrefix(StringUtils.afterNewLine(suffixLast));
        super.suffix(suffixLast);
        return this;
    }

    @Override
    public YamlCommentFormatterConfiguration suffix(String suffixLast, String suffixMultiline) {
        checkCommentPrefix(StringUtils.afterNewLine(suffixLast));
        checkCommentPrefixMultiline(StringUtils.afterNewLine(suffixMultiline));
        super.suffix(suffixLast, suffixMultiline);
        return this;
    }

    public YamlCommentFormatterConfiguration stripPrefix(boolean stripPrefix) {
        this.stripPrefix = stripPrefix;
        return this;
    }

    public boolean stripPrefix() {
        return this.stripPrefix;
    }

    public YamlCommentFormatterConfiguration trim(boolean trim) {
        this.trim = trim;
        return this;
    }

    public boolean trim() {
        return this.trim;
    }

    public void checkCommentPrefix(final String commentPrefix) {
        Validate.isTrue(commentPrefix != null && StringUtils.allLinesArePrefixedOrBlank(commentPrefix, COMMENT_INDICATOR),
                "All comment prefix lines must be blank or optional space followed by a " + COMMENT_INDICATOR);
    }

    public void checkCommentPrefixMultiline(final String commentPrefix) {
        checkCommentPrefix(commentPrefix);
    }

    @Override
    public boolean equals(Object o) {
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

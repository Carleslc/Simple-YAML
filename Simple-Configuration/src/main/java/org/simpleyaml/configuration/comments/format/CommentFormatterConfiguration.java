package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.utils.StringUtils;

import java.util.Objects;

public class CommentFormatterConfiguration {

    private String prefixFirst, prefixMultiline, suffixMultiline, suffixLast;

    public CommentFormatterConfiguration prefix(final String prefix) {
        return prefix(prefix, prefix);
    }

    public CommentFormatterConfiguration prefix(final String prefixFirst, final String prefixMultiline) {
        this.prefixFirst = prefixFirst;
        this.prefixMultiline = prefixMultiline;
        return this;
    }

    public CommentFormatterConfiguration suffix(final String suffixLast) {
        this.suffixLast = suffixLast;
        return this;
    }

    public CommentFormatterConfiguration suffix(final String suffixLast, final String suffixMultiline) {
        this.suffixLast = suffixLast;
        this.suffixMultiline = suffixMultiline;
        return this;
    }

    public String prefixFirst(final String defaultPrefix) {
        return this.prefixFirst != null ? this.prefixFirst : defaultPrefix;
    }

    public String prefixFirst() {
        return prefixFirst("");
    }

    public String prefixMultiline(final String defaultPrefix) {
        return this.prefixMultiline != null ? this.prefixMultiline : prefixFirst(defaultPrefix);
    }

    public String prefixMultiline() {
        return prefixMultiline("");
    }

    public String suffixMultiline(final String defaultSuffix) {
        return this.suffixMultiline != null ? this.suffixMultiline : defaultSuffix;
    }

    public String suffixMultiline() {
        return suffixMultiline("");
    }

    public String suffixLast(final String defaultSuffix) {
        return this.suffixLast != null ? this.suffixLast : defaultSuffix;
    }

    public String suffixLast() {
        return suffixLast("");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentFormatterConfiguration that = (CommentFormatterConfiguration) o;
        return Objects.equals(this.prefixFirst, that.prefixFirst)
                && Objects.equals(this.prefixMultiline, that.prefixMultiline)
                && Objects.equals(this.suffixMultiline, that.suffixMultiline)
                && Objects.equals(this.suffixLast, that.suffixLast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.prefixFirst, this.prefixMultiline, this.suffixMultiline, this.suffixLast);
    }

    @Override
    public String toString() {
        return StringUtils.quoteNewLines("{" +
                "prefixFirst='" + this.prefixFirst + '\'' +
                ", prefixMultiline='" + this.prefixMultiline + '\'' +
                ", suffixMultiline='" + this.suffixMultiline + '\'' +
                ", suffixLast='" + this.suffixLast + '\'' +
                '}');
    }
}

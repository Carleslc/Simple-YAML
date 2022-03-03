package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.utils.StringUtils;

import java.util.Objects;

public class CommentFormatterConfiguration {

    private String prefixFirst, prefixMultiline, suffixMultiline, suffixLast;

    public CommentFormatterConfiguration prefix(String prefix) {
        return prefix(prefix, prefix);
    }

    public CommentFormatterConfiguration prefix(String prefixFirst, String prefixMultiline) {
        this.prefixFirst = prefixFirst;
        this.prefixMultiline = prefixMultiline;
        return this;
    }

    public CommentFormatterConfiguration suffix(String suffixLast) {
        this.suffixLast = suffixLast;
        return this;
    }

    public CommentFormatterConfiguration suffix(String suffixLast, String suffixMultiline) {
        this.suffixLast = suffixLast;
        this.suffixMultiline = suffixMultiline;
        return this;
    }

    public String prefixFirst(String defaultPrefix) {
        return prefixFirst != null ? prefixFirst : defaultPrefix;
    }

    public String prefixFirst() {
        return prefixFirst("");
    }

    public String prefixMultiline(String defaultPrefix) {
        return prefixMultiline != null ? prefixMultiline : prefixFirst(defaultPrefix);
    }

    public String prefixMultiline() {
        return prefixMultiline("");
    }

    public String suffixMultiline(String defaultSuffix) {
        return suffixMultiline != null ? suffixMultiline : defaultSuffix;
    }

    public String suffixMultiline() {
        return suffixMultiline("");
    }

    public String suffixLast(String defaultSuffix) {
        return suffixLast != null ? suffixLast : defaultSuffix;
    }

    public String suffixLast() {
        return suffixLast("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentFormatterConfiguration that = (CommentFormatterConfiguration) o;
        return Objects.equals(prefixFirst, that.prefixFirst)
                && Objects.equals(prefixMultiline, that.prefixMultiline)
                && Objects.equals(suffixMultiline, that.suffixMultiline)
                && Objects.equals(suffixLast, that.suffixLast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefixFirst, prefixMultiline, suffixMultiline, suffixLast);
    }

    @Override
    public String toString() {
        return StringUtils.quoteNewLines("{" +
                "prefixFirst='" + prefixFirst + '\'' +
                ", prefixMultiline='" + prefixMultiline + '\'' +
                ", suffixMultiline='" + suffixMultiline + '\'' +
                ", suffixLast='" + suffixLast + '\'' +
                '}');
    }
}

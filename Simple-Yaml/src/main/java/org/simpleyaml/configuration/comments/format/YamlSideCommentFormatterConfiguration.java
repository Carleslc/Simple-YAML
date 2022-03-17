package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.utils.Validate;

public class YamlSideCommentFormatterConfiguration extends YamlCommentFormatterConfiguration {

    public static final String DEFAULT_SIDE_COMMENT_PREFIX = " " + DEFAULT_COMMENT_PREFIX;

    public YamlSideCommentFormatterConfiguration() {
        this(DEFAULT_SIDE_COMMENT_PREFIX);
    }

    public YamlSideCommentFormatterConfiguration(String sidePrefix) {
        super(sidePrefix);
    }

    public YamlSideCommentFormatterConfiguration(String sidePrefix, String prefixMultiline) {
        super(sidePrefix, prefixMultiline);
    }

    @Override
    public YamlSideCommentFormatterConfiguration prefix(String sidePrefix) {
        super.prefix(sidePrefix, DEFAULT_COMMENT_PREFIX);
        return this;
    }

    @Override
    public YamlSideCommentFormatterConfiguration prefix(String prefixFirst, String prefixMultiline) {
        super.prefix(prefixFirst, prefixMultiline);
        return this;
    }

    @Override
    public YamlSideCommentFormatterConfiguration suffix(String suffixLast) {
        super.suffix(suffixLast);
        return this;
    }

    @Override
    public YamlSideCommentFormatterConfiguration suffix(String suffixLast, String suffixMultiline) {
        super.suffix(suffixLast, suffixMultiline);
        return this;
    }

    @Override
    public YamlSideCommentFormatterConfiguration trim(boolean trim) {
        super.trim(trim);
        return this;
    }

    @Override
    public YamlSideCommentFormatterConfiguration stripPrefix(boolean stripPrefix) {
        super.stripPrefix(stripPrefix);
        return this;
    }

    @Override
    protected void checkCommentPrefix(final String sidePrefix) {
        Validate.isTrue(sidePrefix != null
                        && !sidePrefix.isEmpty()
                        && Character.isWhitespace(sidePrefix.charAt(0)),
                "Side comment prefix must start with space");
        super.checkCommentPrefix(sidePrefix);
    }

    @Override
    protected void checkCommentPrefixMultiline(final String commentPrefix) {
        super.checkCommentPrefix(commentPrefix);
    }
}

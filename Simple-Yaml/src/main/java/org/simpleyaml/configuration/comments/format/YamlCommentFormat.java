package org.simpleyaml.configuration.comments.format;

public enum YamlCommentFormat {

    /**
     * DEFAULT comment format gets comments in a readable way stripping the # comment prefix,
     * without leading and trailing spaces or blank lines and without indentation.
     * <p/>
     * The prefix for setting new comments is:
     * <p>  - BLOCK comments: "# ", i.e. a # followed by a space.
     * <p>  - SIDE comments: " # ", i.e. a space followed by # with a space.
     * <p/>
     * If when setting a comment all lines are blank or already prefixed with a comment prefix # then it does not add additional formatting.
     */
    DEFAULT (YamlCommentFormatter::new),

    /**
     * PRETTY comment format gets comments in a readable way stripping the # comment prefix,
     * without leading and trailing spaces or blank lines and without indentation.
     * <p/>
     * The prefix for setting new comments is:
     * <p>  - BLOCK comments with 0 indentation (root keys) except first key: "\n# ", i.e. a blank line followed by # with a space.
     *        Multiline comments do not add additional blank lines.
     * <p>  - BLOCK comments with some indentation (child keys) or first key: "# ", i.e. a # followed by a space.
     * <p>  - SIDE comments: " # ", i.e. a space followed by # with a space.
     * <p/>
     * If when setting a comment all lines are blank or already prefixed with a comment prefix # then it does not add additional formatting.
     */
    PRETTY (PrettyYamlCommentFormatter::new),

    /**
     * BLANK_LINE comment format gets comments in a readable way stripping the # comment prefix and without indentation,
     * but it keeps trailing spaces and blank lines.
     * <p/>
     * The prefix for setting new comments is:
     * <p>  - BLOCK comments: "\n# ", i.e. a blank line followed by # with a space.
     *        Multiline comments do not add additional blank lines.
     * <p>  - SIDE comments: "\n# ", i.e. a new line followed by # with a space.
     *        This will add the comment below.
     * <p/>
     * If when setting a comment all lines are blank or already prefixed with a comment prefix # then it does not add additional formatting.
     */
    BLANK_LINE (BlankLineYamlCommentFormatter::new),

    /**
     * RAW comment format gets comments as they are in the file configuration,
     * with blank lines and the comment prefix with # character,
     * but without the indentation prefix.
     * <p/>
     * The prefix for setting new comments is:
     * <p>  - BLOCK comments: "# ", i.e. a # followed by a space.
     * <p>  - SIDE comments: " # ", i.e. a space followed by # with a space.
     * <p/>
     * If when setting a comment all lines are blank or already prefixed with a comment prefix # then it does not add additional formatting.
     */
    RAW (() -> new YamlCommentFormatter().stripPrefix(false).trim(false));

    private YamlCommentFormatter yamlCommentFormatter;
    private final YamlCommentFormatterFactory yamlCommentFormatterFactory;

    YamlCommentFormat(final YamlCommentFormatterFactory yamlCommentFormatterFactory) {
        this.yamlCommentFormatterFactory = yamlCommentFormatterFactory;
    }

    public YamlCommentFormatter commentFormatter() {
        if (this.yamlCommentFormatter == null) {
            this.buildCommentFormatter();
        }
        return this.yamlCommentFormatter;
    }

    private void buildCommentFormatter() {
        this.yamlCommentFormatter = this.yamlCommentFormatterFactory.commentFormatter();
    }

    public static void reset() {
        // Rebuild formatters to reset any changes
        for (YamlCommentFormat format : values()) {
            if (format.yamlCommentFormatter != null) {
                format.buildCommentFormatter();
            }
        }
    }

    @FunctionalInterface
    public interface YamlCommentFormatterFactory {
        YamlCommentFormatter commentFormatter();
    }
}

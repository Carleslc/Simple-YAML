package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.comments.format.CommentFormatter;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatter;
import org.simpleyaml.configuration.comments.format.YamlHeaderFormatter;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.utils.Validate;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Various settings for controlling the input and output of a {@link YamlConfiguration}
 *
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlConfigurationOptions.java">Bukkit Source</a>
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {

    /**
     * Additional indentation for list elements.
     */
    private int indentList = 2;

    /**
     * Comment formatter used to format comments.
     */
    private YamlCommentFormatter commentFormatter;

    /**
     * Defines what {@link QuoteStyle} should each type use for its values.
     */
    private final QuoteStyleDefaults quoteStyleDefaults = new QuoteStyleDefaults();

    /**
     * A flag that indicates if the configuration file should parse comments.
     */
    private boolean useComments = false;

    protected YamlConfigurationOptions(final YamlConfiguration configuration) {
        super(configuration);

        this.headerFormatter(new YamlHeaderFormatter());
    }

    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public YamlConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions charset(final Charset charset) {
        super.charset(charset);
        return this;
    }

    @Override
    public YamlConfigurationOptions header(final String header) {
        super.header(header);
        return this;
    }

    @Override
    public YamlConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions headerFormatter(final CommentFormatter headerFormatter) {
        Validate.isTrue(headerFormatter instanceof YamlHeaderFormatter, "The header formatter must inherit YamlHeaderFormatter");
        super.headerFormatter(headerFormatter);
        return this;
    }

    @Override
    public YamlHeaderFormatter headerFormatter() {
        return (YamlHeaderFormatter) super.headerFormatter();
    }

    /**
     * Sets how much spaces should be used to indent each line.
     * <p>
     * The minimum value this may be is 2, and the maximum is 9.
     *
     * @param value New indent
     * @return This object, for chaining
     */
    @Override
    public YamlConfigurationOptions indent(final int value) {
        Validate.isTrue(value >= 2, "Indent must be at least 2 characters");
        Validate.isTrue(value <= 9, "Indent cannot be greater than 9 characters");

        super.indent(value);

        return this;
    }

    /**
     * Gets how much spaces should be used to indent each list element, in addition to the line indent.
     *
     * @return the list elements indentation
     */
    public int indentList() {
        return this.indentList;
    }

    /**
     * Sets how much spaces should be used to indent each list element, in addition to the line indent.
     * <p>
     * The minimum value this may be is 0, and the maximum is the same as the {@link YamlConfigurationOptions#indent()}.
     *
     * @param value New list indentation
     * @return This object, for chaining
     */
    public YamlConfigurationOptions indentList(final int value) {
        Validate.isTrue(value >= 0, "List indent must be at least 0 characters");
        Validate.isTrue(value <= this.indent(), "List indent cannot be greater than the indent");

        this.indentList = value;

        return this;
    }

    /**
     * Gets the comment formatter used to format comments.
     * <p/>
     * The default comment formatter is {@link YamlCommentFormat#DEFAULT}, which comment prefix is "# ", i.e. a # followed by a space.
     *
     * @return the comment formatter
     */
    public YamlCommentFormatter commentFormatter() {
        if (this.commentFormatter == null) {
            this.commentFormatter = YamlCommentFormat.DEFAULT.commentFormatter();
        }
        return this.commentFormatter;
    }

    /**
     * Sets the comment formatter to be used to format comments.
     * <p/>
     * If unset, the default comment formatter prefix is "# ", i.e. a # followed by a space.
     *
     * @param commentFormatter the comment formatter to use
     * @return This object, for chaining
     */
    public YamlConfigurationOptions commentFormatter(final YamlCommentFormatter commentFormatter) {
        this.commentFormatter = commentFormatter;
        return this;
    }

    /**
     * Sets if parsing comments is needed.
     * <p>If you don't use comments in your configuration file keep this disabled to improve parsing performance.</p>
     * Default is false.
     * <p/>
     * With {@link YamlFile} it is updated automatically when you load a file with comments or set new comments programmatically.
     *
     * @param useComments if parsing comments is needed
     * @return This object, for chaining
     */
    public YamlConfigurationOptions useComments(final boolean useComments) {
        this.useComments = useComments;
        return this;
    }

    /**
     * Indicates if parsing comments is enabled.
     * <p>If you don't use comments in your configuration file keep this disabled to improve parsing performance.</p>
     * Default is false.
     * <p/>
     * With {@link YamlFile} it is updated automatically when you load a file with comments or set new comments programmatically.
     *
     * @return This object, for chaining
     * @see #useComments(boolean)
     */
    public boolean useComments() {
        return this.useComments;
    }

    /**
     * Get the quote style default options.
     * <p/>
     * You can change the default quote style globally or for specific value types.
     * @see QuoteStyle#PLAIN
     * @see QuoteStyle#SINGLE
     * @see QuoteStyle#DOUBLE
     * @return the quote style default options
     */
    public QuoteStyleDefaults quoteStyleDefaults() {
        return this.quoteStyleDefaults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        YamlConfigurationOptions that = (YamlConfigurationOptions) o;
        return indentList == that.indentList && Objects.equals(commentFormatter, that.commentFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indentList, commentFormatter);
    }

    /**
     * Options to configure the default {@link QuoteStyle} for each type.
     */
    public static final class QuoteStyleDefaults {

        private final Map<Class<?>, QuoteStyle> typeQuoteStyles = new HashMap<>();

        private QuoteStyle defaultQuoteStyle = QuoteStyleDefaults.defaultQuoteStyle();

        private QuoteStyleDefaults() {}

        /**
         * Get the default quote style.
         * This style is applied to all values.
         * @return the default quote style
         */
        public QuoteStyle getDefaultQuoteStyle() {
            return this.defaultQuoteStyle;
        }

        /**
         * Set the default quote style.
         * This style is applied to all values.
         * @return This object, for chaining
         */
        public QuoteStyleDefaults setDefaultQuoteStyle(QuoteStyle defaultQuoteStyle) {
            if (defaultQuoteStyle == null) {
                defaultQuoteStyle = QuoteStyleDefaults.defaultQuoteStyle();
            }
            this.defaultQuoteStyle = defaultQuoteStyle;
            return this;
        }

        /**
         * Set the default quote style for a specific type.
         * <p>
         * This style is applied to values which class is the specified class or is a child of that class.
         * <p/>
         * Example:
         * <p><pre>
         * options.setQuoteStyle(String.class, QuoteStyle.DOUBLE);
         * yamlConfig.set("key", "This string will be set with double quote style");
         * </pre>
         * <p/>
         * Set quoteStyle to null to set new values again with the default quote style.
         * @param valueClass the specific class to override default quote style
         * @param quoteStyle the quote style to apply
         * @return This object, for chaining
         */
        public QuoteStyleDefaults setQuoteStyle(final Class<?> valueClass, final QuoteStyle quoteStyle) {
            if (quoteStyle == null) {
                this.typeQuoteStyles.remove(valueClass);
            } else {
                this.typeQuoteStyles.put(valueClass, quoteStyle);
            }
            return this;
        }

        /**
         * Get the quote style to apply to a specific type.
         * <p/>
         * If it was not explicitly set using the {@link #setQuoteStyle(Class, QuoteStyle)} method
         * then the {@link #getDefaultQuoteStyle()} is returned.
         * @param valueClass the type class
         * @return the quote style to apply to the specified class
         */
        public QuoteStyle getQuoteStyle(final Class<?> valueClass) {
            final QuoteStyle quoteStyle = this.getExplicitQuoteStyleInstanceOf(valueClass);
            return quoteStyle != null ? quoteStyle : this.getDefaultQuoteStyle();
        }

        /**
         * Get the overriden quote styles for every specific type set.
         * @return the quote styles to apply to every specified class
         */
        public Map<Class<?>, QuoteStyle> getQuoteStyles() {
            return this.typeQuoteStyles;
        }

        /**
         * Get the specific quote style explicitly set to apply to a specific type,
         * or to an inherited class of that type.
         * <p/>
         * If neither the valueClass nor a superclass of that type was explicitly set
         * using the {@link #setQuoteStyle(Class, QuoteStyle)} method then null is returned.
         * @param valueClass the specific class to override default quote style
         * @return the quote style to apply to the specified class
         */
        QuoteStyle getExplicitQuoteStyleInstanceOf(final Class<?> valueClass) {
            QuoteStyle quoteStyle = this.typeQuoteStyles.get(valueClass);
            if (quoteStyle == null && valueClass != null) {
                for (Class<?> superClass : this.typeQuoteStyles.keySet()) {
                    if (superClass.isAssignableFrom(valueClass)) {
                        return this.typeQuoteStyles.get(superClass);
                    }
                }
            }
            return quoteStyle;
        }

        private static QuoteStyle defaultQuoteStyle() {
            return QuoteStyle.PLAIN;
        }
    }
}

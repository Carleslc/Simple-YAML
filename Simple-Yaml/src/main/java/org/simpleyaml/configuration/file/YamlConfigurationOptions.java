package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.comments.CommentFormatter;
import org.simpleyaml.configuration.comments.YamlCommentFormat;
import org.simpleyaml.configuration.comments.YamlHeaderFormatter;
import org.simpleyaml.configuration.comments.YamlCommentFormatter;
import org.simpleyaml.utils.Validate;

import java.util.Objects;

/**
 * Various settings for controlling the input and output of a {@link YamlConfiguration}
 *
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlConfigurationOptions.java">Bukkit Source</a>
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {

    private int indentList = 2;

    private YamlCommentFormatter commentFormatter;

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
     * <p></p>
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
     * <p></p>
     * If unset, the default comment formatter prefix is "# ", i.e. a # followed by a space.
     *
     * @param commentFormatter the comment formatter to use
     * @return This object, for chaining
     */
    public YamlConfigurationOptions commentFormatter(final YamlCommentFormatter commentFormatter) {
        this.commentFormatter = commentFormatter;
        return this;
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
}

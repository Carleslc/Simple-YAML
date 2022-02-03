package org.simpleyaml.configuration.file;

import org.simpleyaml.utils.Validate;

import java.util.Objects;

/**
 * Various settings for controlling the input and output of a {@link YamlConfiguration}
 *
 * @author Bukkit
 * @author Carlos Lazaro Costa (added indentList option)
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlConfigurationOptions.java">Bukkit Source</a>
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {

    private int indentList = 2;

    protected YamlConfigurationOptions(final YamlConfiguration configuration) {
        super(configuration);
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
    public YamlConfigurationOptions header(final String value) {
        super.header(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        YamlConfigurationOptions that = (YamlConfigurationOptions) o;
        return indentList == that.indentList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), indentList);
    }
}

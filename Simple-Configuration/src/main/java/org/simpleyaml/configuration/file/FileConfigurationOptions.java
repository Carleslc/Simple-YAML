package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.MemoryConfiguration;
import org.simpleyaml.configuration.MemoryConfigurationOptions;
import org.simpleyaml.configuration.comments.format.CommentFormatter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Various settings for controlling the input and output of a {@link FileConfiguration}
 *
 * @author Bukkit
 * @author Carlos Lazaro Costa
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/FileConfigurationOptions.java">Bukkit Source</a>
 */
public class FileConfigurationOptions extends MemoryConfigurationOptions {

    private Charset charset = StandardCharsets.UTF_8;

    private String header = null;
    private boolean copyHeader = true;
    private CommentFormatter headerFormatter;

    protected FileConfigurationOptions(final MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public FileConfiguration configuration() {
        return (FileConfiguration) super.configuration();
    }

    @Override
    public FileConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public FileConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    public Charset charset() {
        return this.charset;
    }

    public FileConfigurationOptions charset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public boolean isUnicode() {
        return this.charset.name().startsWith("UTF");
    }

    /**
     * Gets the header that will be applied to the top of the saved output.
     * <p>
     * This header will be commented out.
     * <p>
     * Null is a valid value which will indicate that no header is to be
     * applied. The default value is null.
     *
     * @return Header
     */
    public String header() {
        return this.header;
    }

    /**
     * Sets the header that will be applied to the top of the saved output.
     * <p>
     * This header will be commented out and applied directly at the top of
     * the generated output of the {@link FileConfiguration}. It is not
     * required to include a newline at the end of the header as it will
     * automatically be applied, but you may include one if you wish for extra
     * spacing.
     * <p>
     * Null is a valid header which will indicate that no header is to be applied.
     *
     * @param header New header
     * @return This object, for chaining
     */
    public FileConfigurationOptions header(final String header) {
        this.header = header;
        return this;
    }

    /**
     * Gets whether or not the header should be copied from a default source.
     * <p>
     * If this is true, if a default {@link FileConfiguration} is passed to
     * {@link
     * FileConfiguration#setDefaults(Configuration)}
     * then upon saving it will use the header from that config, instead of
     * the one provided here.
     * <p>
     * If no default is set on the configuration, or the default is not of
     * type FileConfiguration, or that config has no header ({@link #header()}
     * returns null) then the header specified in this configuration will be
     * used.
     * <p>
     * Defaults to true.
     *
     * @return Whether or not to copy the header
     */
    public boolean copyHeader() {
        return this.copyHeader;
    }

    /**
     * Sets whether or not the header should be copied from a default source.
     * <p>
     * If this is true, if a default {@link FileConfiguration} is passed to
     * {@link
     * FileConfiguration#setDefaults(Configuration)}
     * then upon saving it will use the header from that config, instead of
     * the one provided here.
     * <p>
     * If no default is set on the configuration, or the default is not of
     * type FileConfiguration, or that config has no header ({@link #header()}
     * returns null) then the header specified in this configuration will be
     * used.
     * <p>
     * Defaults to true.
     *
     * @param value Whether or not to copy the header
     * @return This object, for chaining
     */
    public FileConfigurationOptions copyHeader(final boolean value) {
        this.copyHeader = value;
        return this;
    }

    /**
     * Gets the header format used for parsing and dumping the header.
     *
     * @return The header formatter
     */
    public CommentFormatter headerFormatter() {
        return this.headerFormatter;
    }

    /**
     * Sets the header format used for parsing and dumping the header.
     *
     * @param headerFormatter The header formatter
     * @return This object, for chaining
     */
    public FileConfigurationOptions headerFormatter(final CommentFormatter headerFormatter) {
        this.headerFormatter = headerFormatter;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        FileConfigurationOptions that = (FileConfigurationOptions) o;
        return copyHeader == that.copyHeader &&
                Objects.equals(charset, that.charset) &&
                Objects.equals(header, that.header) &&
                Objects.equals(headerFormatter, that.headerFormatter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), charset, header, copyHeader, headerFormatter);
    }
}

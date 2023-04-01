package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlHeaderFormatter;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.SupplierIO;
import org.simpleyaml.utils.Validate;

import java.io.*;
import java.nio.file.Files;

/**
 * An implementation of {@link Configuration} which saves the configuration in Yaml.
 * Note that this implementation is not synchronized.
 *
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlConfiguration.java">Bukkit Source</a>
 */
public class YamlConfiguration extends FileConfiguration {

    protected YamlImplementation yamlImplementation;

    /**
     * Creates an empty {@link YamlConfiguration}.
     */
    public YamlConfiguration() {
        this((Configuration) null);
    }

    /**
     * Creates an empty {@link YamlConfiguration} using the specified
     * {@link Configuration} as a source for all default values.
     *
     * @param defaults default values
     */
    public YamlConfiguration(final Configuration defaults) {
        this(defaults, new SnakeYamlImplementation());
    }

    public YamlConfiguration(final YamlImplementation yamlImplementation) {
        this(null, yamlImplementation);
    }

    public YamlConfiguration(final Configuration defaults, final YamlImplementation yamlImplementation) {
        super(defaults);
        this.setImplementation(yamlImplementation);
    }

    public YamlImplementation getImplementation() {
        return this.yamlImplementation;
    }

    public void setImplementation(final YamlImplementation yamlImplementation) {
        Validate.notNull(yamlImplementation, "YAML implementation cannot be null!");
        this.yamlImplementation = yamlImplementation;
        this.yamlImplementation.configure(this.options());
    }

    /**
     * Save the configuration values including the header to a string.
     * @throws IOException when the contents cannot be written for any reason
     */
    @Override
    public String saveToString() throws IOException {
        final StringWriter stringWriter = new StringWriter();

        this.save(stringWriter);

        return StringUtils.stripCarriage(stringWriter.toString());
    }

    /**
     * Save the configuration values including the header.
     * @param writer where to save this configuration
     * @throws IOException when the contents cannot be written for any reason
     * @see #saveToString()
     */
    @Override
    public void save(final Writer writer) throws IOException {
        Validate.notNull(writer, "Writer cannot be null");
        try {
            writer.write(this.buildHeader());
            this.dump(writer);
        } finally {
            writer.close();
        }
    }

    /**
     * Dump the configuration values without the header to a string.
     * @throws IOException when the contents cannot be written for any reason
     * @see #saveToString()
     */
    public String dump() throws IOException {
        return this.yamlImplementation.dump(this);
    }

    /**
     * Dump the configuration values without the header.
     * @param writer where to save this configuration
     * @throws IOException when the contents cannot be written for any reason
     * @see #save(Writer)
     */
    public void dump(final Writer writer) throws IOException {
        Validate.notNull(writer, "Writer cannot be null");
        this.yamlImplementation.dump(writer, this);
    }

    /**
     * Loads this {@link YamlConfiguration} from the specified reader.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param  readerSupplier                a function providing the reader to load from (new instance)
     * @throws IOException                   Thrown when underlying reader throws an IOException.
     * @throws InvalidConfigurationException Thrown when the reader does not represent a valid Configuration.
     * @throws IllegalArgumentException      Thrown when reader is null.
     */
    public void load(final SupplierIO.Reader readerSupplier) throws IOException, InvalidConfigurationException {
        Validate.notNull(readerSupplier, "Reader supplier cannot be null");

        this.loadHeader(readerSupplier.get());

        this.yamlImplementation.load(readerSupplier, this);
    }

    protected void loadHeader(final Reader reader) throws IOException {
        final YamlConfigurationOptions options = this.options();
        final YamlHeaderFormatter headerFormatter = options.headerFormatter();
        boolean customStripPrefix = headerFormatter.stripPrefix();

        headerFormatter.stripPrefix(false);
        options.header(headerFormatter.parse(reader)); // save header with prefix to dump it as is

        headerFormatter.stripPrefix(customStripPrefix); // restore the custom strip prefix for the following calls to parse
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given reader.
     *
     * @param readerSupplier a function providing the reader to load from (new instance)
     * @return resulting configuration
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final SupplierIO.Reader readerSupplier) throws IOException {
        Validate.notNull(readerSupplier, "Reader supplier cannot be null");
        return YamlConfiguration.load(config -> config.load(readerSupplier));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given file.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param file Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException if file is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        return YamlConfiguration.load(config -> config.load(file));
    }

    /**
     * Loads this configuration from the specified file.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given file.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be
     * thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param file File to load from.
     * @throws FileNotFoundException         Thrown when the given file cannot be opened.
     * @throws IOException                   Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     * @throws IllegalArgumentException      Thrown when file is null.
     */
    @Override
    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        load(() -> Files.newInputStream(file.toPath()));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the specified string contents.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param contents                       the contents to load from
     * @throws IOException                   Thrown when underlying reader throws an IOException.
     * @throws InvalidConfigurationException Thrown when the contents does not represent a valid Configuration.
     * @throws IllegalArgumentException      Thrown when contents is null.
     */
    public static YamlConfiguration loadConfigurationFromString(final String contents) throws IOException {
        return YamlConfiguration.load(config -> config.loadFromString(contents));
    }

    /**
     * Loads this {@link YamlConfiguration} from the specified string.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param contents                       the contents to load from
     * @throws IOException                   Thrown when underlying reader throws an IOException.
     * @throws InvalidConfigurationException Thrown when the contents does not represent a valid Configuration.
     * @throws IllegalArgumentException      Thrown when contents is null.
     */
    @Override
    public void loadFromString(final String contents) throws IOException {
        Validate.notNull(contents, "Contents cannot be null");
        this.load(() -> new StringReader(contents));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given input stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param streamSupplier a function providing the stream to load from (new instance)
     * @return resulting configuration
     * @throws InvalidConfigurationException if input stream is not a valid Configuration.
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final SupplierIO.InputStream streamSupplier) throws IOException {
        Validate.notNull(streamSupplier, "Reader supplier cannot be null");
        return YamlConfiguration.load(config -> config.load(streamSupplier));
    }

    /**
     * Loads this configuration from the specified stream.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param streamSupplier                 a function providing the stream to load from (new instance)
     * @throws IOException                   Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     * @throws IllegalArgumentException      Thrown when stream is null.
     * @see #load(SupplierIO.Reader)
     */
    public void load(final SupplierIO.InputStream streamSupplier) throws IOException, InvalidConfigurationException {
        Validate.notNull(streamSupplier, "Stream supplier cannot be null");
        load(() -> new InputStreamReader(streamSupplier.get(), this.options().charset()));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given stream.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param stream Input stream
     * @return Resulting configuration
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     * @see #loadConfiguration(SupplierIO.InputStream)
     * @see #loadConfiguration(SupplierIO.Reader)
     * @deprecated this method loads the entire file into memory, for larger files please use {@link #load(SupplierIO.InputStream)}
     */
    @Deprecated
    public static YamlConfiguration loadConfiguration(final InputStream stream) throws IOException {
        Validate.notNull(stream, "Stream cannot be null");
        return YamlConfiguration.load(config -> config.load(stream));
    }

    /**
     * @deprecated this method loads the entire file into memory, for larger files please use {@link #load(SupplierIO.InputStream)}
     * @see #loadConfiguration(SupplierIO.InputStream)
     */
    @Override
    @Deprecated
    @SuppressWarnings("DuplicateThrows")
    public void load(final InputStream stream) throws IOException, InvalidConfigurationException {
        super.load(stream);
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given reader.
     *
     * @param reader input reader
     * @return resulting configuration
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     * @deprecated this method loads the entire file into memory, for larger files please use {@link #load(SupplierIO.Reader)}
     * @see #loadConfiguration(SupplierIO.Reader)
     * @see #loadConfiguration(SupplierIO.InputStream)
     */
    @Deprecated
    public static YamlConfiguration loadConfiguration(final Reader reader) throws IOException {
        Validate.notNull(reader, "Reader cannot be null");
        return YamlConfiguration.load(config -> config.load(reader));
    }

    /**
     * @deprecated this method loads the entire file into memory, for larger files please use {@link #load(SupplierIO.Reader)}
     * @see #loadConfiguration(SupplierIO.Reader)
     */
    @Override
    @Deprecated
    @SuppressWarnings("DuplicateThrows")
    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        super.load(reader);
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * The value will be represented with the specified quote style in the configuration file.
     * <p/>
     * Any existing entry will be replaced, regardless of what the new value is.
     * <p/>
     * Null value is valid and will not remove the key, this is different to {@link #set(String, Object)}.
     * Instead, a null value will be written as a yaml empty null value.
     * <p/>
     * Some implementations may have limitations on what you may store. See
     * their individual javadocs for details. No implementations should allow
     * you to store {@link Configuration}s or {@link ConfigurationSection}s,
     * please use {@link #createSection(String)} for that.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to.
     * @param quoteStyle The quote style to use.
     */
    public void set(final String path, final Object value, final QuoteStyle quoteStyle) {
        this.set(path, new QuoteValue<>(value, quoteStyle));
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     * <p>
     * Some implementations may have limitations on what you may store. See
     * their individual javadocs for details. No implementations should allow
     * you to store {@link Configuration}s or {@link ConfigurationSection}s,
     * please use {@link #createSection(String)} for that.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to.
     */
    @Override
    public void set(final String path, final Object value) {
        if (value != null && !(value instanceof QuoteValue)) {
            final QuoteStyle quoteStyle = this.options().quoteStyleDefaults().getExplicitQuoteStyleInstanceOf(value.getClass());
            if (quoteStyle != null) {
                this.set(path, value, quoteStyle);
                return;
            }
        }
        super.set(path, value);
    }

    @Override
    public Object get(final String path, final Object def) {
        Object object = super.get(path, def);

        if (object instanceof QuoteValue) {
            object = ((QuoteValue<?>) object).getValue();
        }

        return object;
    }

    @Override
    public YamlConfigurationOptions options() {
        if (this.options == null) {
            this.options = new YamlConfigurationOptions(this);
        }
        return (YamlConfigurationOptions) this.options;
    }

    private static YamlConfiguration load(final YamlConfigurationLoader loader) throws IOException {
        final YamlConfiguration config = new YamlConfiguration();

        loader.load(config);

        return config;
    }

    @FunctionalInterface
    private interface YamlConfigurationLoader {
        void load(YamlConfiguration config) throws IOException;
    }
}

package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.YamlHeaderFormatter;
import org.simpleyaml.configuration.implementation.SnakeYamlImplementation;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

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
     * Creates a new {@link YamlConfiguration}, loading from the given file.
     * <p>
     * If the specified input is not a valid config, a blank config will be returned.
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
     * Creates a new {@link YamlConfiguration}, loading from the given stream.
     * <p>
     * If the specified input is not a valid config, a blank config will be returned.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param stream Input stream
     * @return Resulting configuration
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     * @see #load(InputStream)
     * @see #loadConfiguration(Reader)
     */
    public static YamlConfiguration loadConfiguration(final InputStream stream) throws IOException {
        Validate.notNull(stream, "Stream cannot be null");
        return YamlConfiguration.load(config -> config.load(stream));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given reader.
     * <p>
     * If the specified input is not a valid config, a blank config will be returned.
     *
     * @param reader input
     * @return resulting configuration
     * @throws IllegalArgumentException if stream is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final Reader reader) throws IOException {
        Validate.notNull(reader, "Reader cannot be null");
        return YamlConfiguration.load(config -> config.load(reader));
    }

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
    }

    @Override
    public String saveToString() throws IOException {
        return this.buildHeader() + this.dump();
    }

    protected String dump() {
        return this.yamlImplementation.dump(this.getValues(false), this.options());
    }

    /**
     * Loads this configuration from the specified string.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given string.
     * <p>
     * If the string is invalid in any way, an exception will be thrown.
     *
     * @param contents Contents of a Configuration to load.
     * @throws InvalidConfigurationException if the specified string is invalid.
     * @throws IllegalArgumentException      if contents is null.
     */
    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        final Map<?, ?> input = this.yamlImplementation.load(contents);

        this.loadHeader(contents);

        if (input != null) {
            this.convertMapsToSections(input, this);
        }
    }

    protected void loadHeader(final String contents) {
        final YamlConfigurationOptions options = this.options();
        final YamlHeaderFormatter headerFormatter = options.headerFormatter();
        boolean customStripPrefix = headerFormatter.stripPrefix();

        headerFormatter.stripPrefix(false);
        options.header(headerFormatter.parse(contents)); // save header with prefix to dump it as is

        headerFormatter.stripPrefix(customStripPrefix); // restore the custom strip prefix for the following calls to parse
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * The value will be represented with the specified quote style in the configuration file.
     * <p></p>
     * Any existing entry will be replaced, regardless of what the new value is.
     * <p></p>
     * Null value is valid and will not remove the key, this is different to {@link #set(String, Object)}.
     * Instead, a null value will be written as a yaml empty null value.
     * <p></p>
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
        super.set(path, this.yamlImplementation.quoteValue(value, quoteStyle));
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
        if (value != null) {
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

    protected void convertMapsToSections(final Map<?, ?> input, final ConfigurationSection section) {
        for (final Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();

            if (value instanceof Map) {
                this.convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
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

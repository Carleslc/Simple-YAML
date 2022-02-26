package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.YamlHeaderFormatter;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 *
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/YamlConfiguration.java">Bukkit Source</a>
 */
public class YamlConfiguration extends FileConfiguration {

    protected static final String BLANK_CONFIG = "{}\n";

    private final DumperOptions yamlOptions = new DumperOptions();

    private final Representer yamlRepresenter = new YamlRepresenter();

    private final Yaml yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given file.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param file Input file
     * @return Resulting configuration
     * @throws IllegalArgumentException Thrown if file is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        return YamlConfiguration.load(config -> config.load(file));
    }

    /**
     * Creates a new {@link YamlConfiguration}, loading from the given stream.
     * <p>
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
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
     * Any errors loading the Configuration will be logged and then ignored.
     * If the specified input is not a valid config, a blank config will be
     * returned.
     *
     * @param reader input
     * @return resulting configuration
     * @throws IllegalArgumentException Thrown if stream is null
     * @throws IOException if cannot load configuration
     */
    public static YamlConfiguration loadConfiguration(final Reader reader) throws IOException {
        Validate.notNull(reader, "Reader cannot be null");
        return YamlConfiguration.load(config -> config.load(reader));
    }

    @Override
    public String saveToString() throws IOException {
        return this.buildHeader() + this.dump();
    }

    protected String dump() {
        this.yamlOptions.setAllowUnicode(this.options().isUnicode());

        this.yamlOptions.setIndent(this.options().indent());
        this.yamlOptions.setIndicatorIndent(this.options().indentList());
        this.yamlOptions.setIndentWithIndicator(true);

        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yamlRepresenter.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        String dump = this.yaml.dump(this.getValues(false));

        if (dump.equals(YamlConfiguration.BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        final Map<?, ?> input;
        try {
            input = this.yaml.load(contents);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

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

    private interface YamlConfigurationLoader {

        void load(YamlConfiguration config) throws IOException;

    }

}

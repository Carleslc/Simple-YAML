package org.simpleyaml.configuration.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.Commentable;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 *
 * @author Bukkit
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
     */
    public static YamlConfiguration loadConfiguration(final File file) {
        Validate.notNull(file, "File cannot be null");
        return YamlConfiguration.run(config -> config.load(file));
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
     * @throws IllegalArgumentException Thrown if stream is null
     * @see #load(InputStream)
     * @see #loadConfiguration(Reader)
     */
    public static YamlConfiguration loadConfiguration(final InputStream stream) {
        Validate.notNull(stream, "Stream cannot be null");
        return YamlConfiguration.run(config -> config.load(stream));
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
     */
    public static YamlConfiguration loadConfiguration(final Reader reader) {
        Validate.notNull(reader, "Reader cannot be null");
        return YamlConfiguration.run(config -> config.load(reader));
    }

    protected static String parseHeader(final String input) {
        final String[] lines = input.split("\r?\n", -1);
        final StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for (int lineindex = 0; lineindex < lines.length && readingHeader; lineindex++) {
            final String line = lines[lineindex];

            if (line.startsWith(Commentable.COMMENT_PREFIX)) {
                if (lineindex > 0) {
                    result.append('\n');
                }

                if (line.length() > Commentable.COMMENT_PREFIX.length()) {
                    result.append(line.substring(Commentable.COMMENT_PREFIX.length()));
                }

                foundHeader = true;
            } else if (foundHeader && line.isEmpty()) {
                result.append('\n');
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    private static YamlConfiguration run(final YamlConfiguration.YamlRunnable runnable) {
        final YamlConfiguration config = new YamlConfiguration();

        try {
            runnable.run(config);
        } catch (final IOException | InvalidConfigurationException ex) {
            Logger.getLogger(YamlConfiguration.class.getName()).log(Level.SEVERE, "Cannot load configuration", ex);
        }

        return config;
    }

    @Override
    public String saveToString() {
        this.yamlOptions.setIndent(this.options().indent());
        this.yamlOptions.setAllowUnicode(this.options().isUnicode());
        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final String header = this.buildHeader();
        String dump = this.yaml.dump(this.getValues(false));

        if (dump.equals(YamlConfiguration.BLANK_CONFIG)) {
            dump = "";
        }

        return header + dump;
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

        final String header = YamlConfiguration.parseHeader(contents);
        if (!header.isEmpty()) {
            this.options().header(header);
        }

        if (input != null) {
            this.convertMapsToSections(input, this);
        }
    }

    @Override
    public YamlConfigurationOptions options() {
        if (this.options == null) {
            this.options = new YamlConfigurationOptions(this);
        }
        return (YamlConfigurationOptions) this.options;
    }

    @Override
    protected String buildHeader() {
        final String header = this.options().header();

        if (this.options().copyHeader()) {
            final Configuration def = this.getDefaults();

            if (def instanceof FileConfiguration) {
                final FileConfiguration filedefaults = (FileConfiguration) def;
                final String defaultsHeader = filedefaults.buildHeader();

                if (defaultsHeader != null && defaultsHeader.length() > 0) {
                    return defaultsHeader;
                }
            }
        }

        if (header == null) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        final String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if (startedHeader || lines[i].length() != 0) {
                builder.insert(0, lines[i]);
                builder.insert(0, Commentable.COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
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

    private interface YamlRunnable {

        void run(YamlConfiguration config) throws IOException, InvalidConfigurationException;

    }

}

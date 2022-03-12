package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.LoadableConfiguration;
import org.simpleyaml.configuration.MemoryConfiguration;
import org.simpleyaml.configuration.comments.format.CommentFormatter;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;

import java.io.*;

/**
 * This is a base class for all File based implementations of {@link Configuration}
 *
 * @author Bukkit
 * @author Carleslc
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/file/FileConfiguration.java">Bukkit Source</a>
 */
public abstract class FileConfiguration extends MemoryConfiguration implements LoadableConfiguration {

    /**
     * Creates an empty {@link FileConfiguration} with no default values.
     */
    public FileConfiguration() {
        super();
    }

    /**
     * Creates an empty {@link FileConfiguration} using the specified
     * {@link Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     */
    public FileConfiguration(final Configuration defaults) {
        super(defaults);
    }

    /**
     * Saves this {@link FileConfiguration} to the specified location.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an
     * exception will be thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param file File to save to.
     * @throws IOException              Thrown when the given file cannot be written to for any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(final File file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        final File parents = file.getParentFile();

        if (parents != null && !parents.exists() && !parents.mkdirs()) {
            throw new IOException("Cannot create successfully all needed parent directories!");
        }

        this.save(new OutputStreamWriter(new FileOutputStream(file), this.options().charset()));
    }

    /**
     * Saves this {@link FileConfiguration} to the specified location.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an
     * exception will be thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param file File to save to.
     * @throws IOException              Thrown when the given file cannot be written to for any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(final String file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        this.save(new File(file));
    }

    @Override
    public void save(final Writer writer) throws IOException {
        Validate.notNull(writer, "Writer cannot be null");
        try {
            writer.write(this.saveToString());
        } finally {
            writer.close();
        }
    }

    /**
     * Loads this configuration from the specified file path.
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
    public void load(final String file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        this.load(new File(file));
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
    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        load(new FileInputStream(file));
    }

    /**
     * Loads this configuration from the specified stream.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be
     * thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @param stream Stream to load from
     * @throws IOException                   Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     * @throws IllegalArgumentException      Thrown when stream is null.
     * @see #load(Reader)
     */
    public void load(final InputStream stream) throws IOException, InvalidConfigurationException {
        Validate.notNull(stream, "Stream cannot be null");
        load(new InputStreamReader(stream, this.options().charset()));
    }

    /**
     * Loads this configuration from the specified reader.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     * <p>
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param reader the reader to load from
     * @throws IOException                   Thrown when underlying reader throws an IOException.
     * @throws InvalidConfigurationException Thrown when the reader does not represent a valid Configuration.
     * @throws IllegalArgumentException      Thrown when reader is null.
     */
    @Override
    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        Validate.notNull(reader, "Reader cannot be null");

        try (final BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            final StringBuilder builder = new StringBuilder();
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            this.loadFromString(builder.toString());
        }
    }

    @Override
    public FileConfigurationOptions options() {
        if (this.options == null) {
            this.options = new FileConfigurationOptions(this);
        }
        return (FileConfigurationOptions) this.options;
    }

    /**
     * Compiles the header for this {@link FileConfiguration} and returns the
     * result.
     * <p>
     * This will use the header from {@link #options()} {@link FileConfigurationOptions#header()},
     * respecting the rules of {@link FileConfigurationOptions#copyHeader()}
     * and {@link FileConfigurationOptions#headerFormatter()} if set.
     *
     * @return Compiled header
     */
    public String buildHeader() {
        final FileConfigurationOptions options = this.options();

        if (!options.copyHeader()) {
            return "";
        }

        final Configuration def = this.getDefaults();

        if (def instanceof FileConfiguration) {
            final FileConfiguration defaults = (FileConfiguration) def;
            final String defaultsHeader = defaults.buildHeader();

            if (defaultsHeader != null && !defaultsHeader.isEmpty()) {
                return defaultsHeader;
            }
        }

        final String header = options.header();
        final CommentFormatter headerFormatter = options.headerFormatter();

        if (headerFormatter != null) {
            final String headerDump = headerFormatter.dump(header);
            return headerDump != null ? headerDump : "";
        }

        return header != null && !header.isEmpty() ? header + '\n' : "";
    }

}

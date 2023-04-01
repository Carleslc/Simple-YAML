package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.YamlCommentDumper;
import org.simpleyaml.configuration.comments.YamlCommentMapper;
import org.simpleyaml.configuration.comments.YamlCommentParser;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlConstructor;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlRepresenter;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.SectionUtils;
import org.simpleyaml.utils.SupplierIO;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;

/**
 * Default YAML implementation using snakeyaml high-level API and a custom comment parser / dumper.
 */
public class SimpleYamlImplementation extends SnakeYamlImplementation {

    public SimpleYamlImplementation() {
        super();
    }

    public SimpleYamlImplementation(final LoaderOptions loaderOptions, final DumperOptions dumperOptions) {
        super(loaderOptions, dumperOptions);
    }

    public SimpleYamlImplementation(final SnakeYamlRepresenter yamlRepresenter) {
        super(yamlRepresenter);
    }

    public SimpleYamlImplementation(final SnakeYamlConstructor yamlConstructor,
                                   final SnakeYamlRepresenter yamlRepresenter) {
        super(yamlConstructor, yamlRepresenter);
    }

    public SimpleYamlImplementation(final SnakeYamlConstructor yamlConstructor,
                                   final SnakeYamlRepresenter yamlRepresenter,
                                   final Resolver resolver) {
        super(yamlConstructor, yamlRepresenter, resolver);
    }

    @Override
    public void setComment(final String path, final String comment, final CommentType type) {
        if (this.yamlCommentMapper == null) {
            this.options.useComments(true);
            this.yamlCommentMapper = new YamlCommentMapper(this.options);
        }
        this.yamlCommentMapper.setComment(path, comment, type);
    }

    @Override
    @SuppressWarnings("DuplicateThrows")
    public void load(final SupplierIO.Reader readerSupplier, final ConfigurationSection section) throws IOException, InvalidConfigurationException {
        if (readerSupplier != null) {
            this.load(readerSupplier.get(), section);

            if (this.options.useComments()) {
                this.parseComments(readerSupplier.get());
            }
        }
    }

    @Override
    @SuppressWarnings("DuplicateThrows")
    public void load(final Reader reader, final ConfigurationSection section) throws IOException, InvalidConfigurationException {
        this.configure(this.options);

        if (reader != null && section != null) {
            try {
                final Map<?, ?> values = this.getYaml().load(reader);

                if (values != null) {
                    SectionUtils.convertMapsToSections(values, section);
                }
            } catch (final YAMLException e) {
                throw new InvalidConfigurationException(e);
            } catch (final ClassCastException e) {
                throw new InvalidConfigurationException("Top level is not a Map.");
            } finally {
                reader.close();
            }
        }
    }

    @Override
    public void dump(final Writer writer, final ConfigurationSection section) throws IOException {
        this.configure(this.options);

        if (this.hasContent(writer, section)) {
            if (this.options.useComments()) {
                final YamlCommentDumper commentDumper = new YamlCommentDumper(
                        this.parseComments(),
                        dumper -> super.dumpYaml(dumper, section),
                        writer
                );
                commentDumper.dump();
            } else {
                super.dumpYaml(writer, section);
            }
        }
    }

    /**
     * Parse comments from the current file configuration.
     *
     * @return a comment mapper with comments parsed
     * @throws IOException if it hasn't been possible to parse the comments
     */
    private YamlCommentMapper parseComments() throws IOException {
        if (this.yamlCommentMapper != null) {
            return this.yamlCommentMapper;
        }
        final YamlConfiguration config = this.options.configuration();
        Reader reader = null;
        if (config instanceof YamlFile) {
            final File configFile = ((YamlFile) config).getConfigurationFile();
            if (configFile != null) {
                reader = configFile.exists() ? Files.newBufferedReader(configFile.toPath(), this.options.charset()) : null;
            }
        }
        return this.parseComments(reader);
    }

    /**
     * Parse comments from a reader.
     *
     * @param reader Reader of a Configuration to parse.
     * @return a comment mapper with comments parsed
     * @throws InvalidConfigurationException if it hasn't been possible to read the contents
     */
    public YamlCommentMapper parseComments(final Reader reader) throws InvalidConfigurationException {
        try {
            if (reader != null) {
                this.yamlCommentMapper = new YamlCommentParser(this.options, reader);
                ((YamlCommentParser) this.yamlCommentMapper).parse();
            } else {
                this.yamlCommentMapper = new YamlCommentMapper(this.options);
            }
            return this.yamlCommentMapper;
        } catch (IOException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public void configure(final YamlConfigurationOptions options) {
        super.configure(options);

        // Use custom comment processor
        this.loaderOptions.setProcessComments(false);
        this.dumperOptions.setProcessComments(false);
    }

}

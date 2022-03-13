package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.YamlCommentDumper;
import org.simpleyaml.configuration.comments.YamlCommentMapper;
import org.simpleyaml.configuration.comments.YamlCommentParser;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.SupplierIO;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;

public class SimpleYamlImplementation extends SnakeYamlImplementation {

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
    public Map<String, Object> load(final SupplierIO.Reader readerSupplier) throws IOException, InvalidConfigurationException {
        final Map<String, Object> values = super.load(readerSupplier);

        if (this.options.useComments()) {
            this.parseComments(readerSupplier.get());
        }

        return values;
    }

    @Override
    public void dump(final Writer writer, final Map<String, Object> values) throws IOException {
        this.configure(this.options);

        if (this.hasContent(writer, values)) {
            if (this.options.useComments()) {
                final YamlCommentDumper commentDumper = new YamlCommentDumper(
                        this.parseComments(),
                        dumper -> super.dump(dumper, values),
                        writer
                );
                commentDumper.dump();
            } else {
                this.dumpYaml(writer, values);
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
        this.getLoaderOptions().setProcessComments(false);
        this.getDumperOptions().setProcessComments(false);
    }

}

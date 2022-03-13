package org.simpleyaml.configuration.implementation.api;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.SupplierIO;

import java.io.*;
import java.util.Map;

public interface YamlImplementation {

    /**
     * Load Yaml to a map.
     * @param reader a Yaml reader with contents to load
     * @return the contents loaded as a map
     * @throws IOException if cannot read contents.
     * @throws InvalidConfigurationException if contents is not a valid Yaml configuration
     */
    Map<String, Object> load(final Reader reader) throws IOException, InvalidConfigurationException;

    /**
     * Load Yaml to a map.
     * @param readerSupplier a function providing a Yaml reader with contents to load
     * @return the contents loaded as a map
     * @throws IOException if cannot read contents.
     * @throws InvalidConfigurationException if contents is not a valid Yaml configuration
     */
    default Map<String, Object> load(final SupplierIO.Reader readerSupplier) throws IOException, InvalidConfigurationException {
        return this.load(readerSupplier.get());
    }

    /**
     * Load Yaml to a map.
     * @param contents a Yaml string with contents to load
     * @return the contents loaded as a map
     * @throws IOException if cannot read contents.
     * @throws InvalidConfigurationException if contents is not a valid Yaml string
     */
    default Map<String, Object> load(final String contents) throws IOException, InvalidConfigurationException {
        return this.load(new StringReader(contents));
    }

    /**
     * Dump values to Yaml.
     * @param writer writer to dump values
     * @param values values to dump
     */
    void dump(final Writer writer, final Map<String, Object> values) throws IOException;

    /**
     * Dump values to Yaml.
     * @param values values to dump
     * @return the values as a valid Yaml string
     */
    default String dump(final Map<String, Object> values) throws IOException {
        final StringWriter stringWriter = new StringWriter();

        this.dump(stringWriter, values);

        return stringWriter.toString();
    }

    /**
     * Apply the configuration options to this implementation.
     * @param options yaml options
     */
    void configure(final YamlConfigurationOptions options);

}

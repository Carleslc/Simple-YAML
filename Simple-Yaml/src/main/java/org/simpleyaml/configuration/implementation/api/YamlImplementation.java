package org.simpleyaml.configuration.implementation.api;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.exceptions.InvalidConfigurationException;

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
     * @param contents a Yaml string with contents to load
     * @return the contents loaded as a map
     * @throws IOException if cannot read contents.
     * @throws InvalidConfigurationException if contents is not a valid Yaml string
     */
    default Map<String, Object> load(final String contents) throws IOException, InvalidConfigurationException {
        return this.load(new BufferedReader(new StringReader(contents)));
    }

    /**
     * Dump values to Yaml.
     * @param writer writer to dump values
     * @param values values to dump
     * @param options dumping options
     */
    void dump(final Writer writer, final Map<String, Object> values, final YamlConfigurationOptions options) throws IOException;

    /**
     * Dump values to Yaml.
     * @param values values to dump
     * @param options dumping options
     * @return the values as a valid Yaml string
     */
    String dump(final Map<String, Object> values, final YamlConfigurationOptions options) throws IOException;

    /**
     * Apply the configuration options to this implementation.
     * @param options yaml options
     */
    void configure(final YamlConfigurationOptions options);

    /**
     * Get a representation of a value following the specified quote style.
     * @param value the value to be dumped
     * @param quoteStyle the quote style to use
     * @param <T> the value type
     * @return a representation of the value to serialize it using the specified quote style
     */
    default <T> QuoteValue<T> quoteValue(final T value, final QuoteStyle quoteStyle) {
        return new QuoteValue<>(value, quoteStyle);
    }

}

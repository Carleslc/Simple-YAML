package org.simpleyaml.configuration.implementation.api;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.util.Map;

public interface YamlImplementation {

    /**
     * Load Yaml to a map.
     * @param contents a Yaml string with contents to load
     * @return the contents loaded as a map
     * @throws InvalidConfigurationException if contents is not a valid Yaml string
     */
    Map<String, Object> load(final String contents) throws InvalidConfigurationException;

    /**
     * Dump values to Yaml.
     * @param values values to dump
     * @param options dumping options
     * @return the values as a valid Yaml string
     */
    String dump(final Map<String, Object> values, final YamlConfigurationOptions options);

    /**
     * Apply the configuration options to this implementation.
     * @param options yaml options
     */
    void configure(final YamlConfigurationOptions options);

    /**
     * Get a representation of a value following the specified quote style.
     * @param value the value to be dumped
     * @param quoteStyle the quote style to use
     * @return a representation of the value to serialize it using the specified quote style
     */
    default <T> QuoteValue<T> quoteValue(final T value, final QuoteStyle quoteStyle) {
        return new QuoteValue<>(value, quoteStyle);
    }

}

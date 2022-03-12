package org.simpleyaml.configuration;

import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface LoadableConfiguration {

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
     * @throws IOException if cannot read contents.
     * @throws InvalidConfigurationException if the specified string is invalid.
     * @throws IllegalArgumentException      if contents is null.
     */
    void loadFromString(final String contents) throws IOException, InvalidConfigurationException;

    /**
     * Saves this configuration to a string, and returns it.
     *
     * @throws IOException when the contents cannot be written for any reason.
     * @return a String containing this configuration.
     */
    String saveToString() throws IOException;

    /**
     * Loads this configuration from the specified reader.
     * <p>
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given string.
     * <p>
     * If the contents are invalid in any way, an exception will be thrown.
     *
     * @param reader Reader of a Configuration to load.
     * @throws IOException if reader throws an IOException.
     * @throws InvalidConfigurationException if the specified configuration is invalid.
     * @throws IllegalArgumentException      if reader is null.
     */
    void load(final Reader reader) throws IOException, InvalidConfigurationException;

    /**
     * Saves this configuration to a writer.
     *
     * @param writer where to save this configuration
     * @throws IOException when the contents cannot be written for any reason.
     */
    void save(final Writer writer) throws IOException;

}

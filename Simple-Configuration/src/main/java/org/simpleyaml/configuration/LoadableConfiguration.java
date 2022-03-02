package org.simpleyaml.configuration;

import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.IOException;

public interface LoadableConfiguration {

    /**
     * Saves this configuration to a string, and returns it.
     *
     * @throws IOException when the contents cannot be written for any reason.
     * @return a String containing this configuration.
     */
    String saveToString() throws IOException;

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
     */
    void loadFromString(String contents) throws InvalidConfigurationException;

}

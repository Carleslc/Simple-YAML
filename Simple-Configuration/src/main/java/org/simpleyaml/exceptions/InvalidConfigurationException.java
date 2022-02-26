package org.simpleyaml.exceptions;

import org.simpleyaml.configuration.Configuration;

import java.io.IOException;

/**
 * Exception thrown when attempting to load an invalid {@link Configuration}
 *
 * @author Bukkit
 * @see <a href="https://github.com/Bukkit/Bukkit/tree/master/src/main/java/org/bukkit/configuration/InvalidConfigurationException.java">Bukkit Source</a>
 */
@SuppressWarnings("serial")
public class InvalidConfigurationException extends IOException {

    /**
     * Creates a new instance of InvalidConfigurationException without a
     * message or cause.
     */
    public InvalidConfigurationException() {
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified message.
     *
     * @param msg The details of the exception.
     */
    public InvalidConfigurationException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified cause.
     *
     * @param cause The cause of the exception.
     */
    public InvalidConfigurationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified message and cause.
     *
     * @param cause The cause of the exception.
     * @param msg   The details of the exception.
     */
    public InvalidConfigurationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}

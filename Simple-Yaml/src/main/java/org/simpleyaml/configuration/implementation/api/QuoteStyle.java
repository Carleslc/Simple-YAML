package org.simpleyaml.configuration.implementation.api;

public enum QuoteStyle {
    /**
     * Wrap values with single quotes.
     */
    SINGLE,
    /**
     * Wrap values with double quotes.
     */
    DOUBLE,
    /**
     * Default style, without quotes when possible.
     * <p>If value have characters that must be escaped then {@link #SINGLE} quote style is used.
     */
    PLAIN
}

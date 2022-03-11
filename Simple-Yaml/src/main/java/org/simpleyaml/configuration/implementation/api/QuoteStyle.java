package org.simpleyaml.configuration.implementation.api;

public enum QuoteStyle {
    /**
     * Wrap values with single quotes.
     * <p/>
     * <pre>single: 'value'</pre>
     */
    SINGLE,
    /**
     * Wrap values with double quotes.
     * <p/>
     * <pre>double: "value"</pre>
     */
    DOUBLE,
    /**
     * Default style, without quotes when possible.
     * <p/>
     * <pre>plain: value</pre>
     * <p/>
     * If value have characters that must be escaped then {@link #SINGLE} quote style is used.
     */
    PLAIN,
    /**
     * <pre>
     * literal: |-
     *   Each line
     *   is literal
     *   and are joined with new lines
     * </pre>
     */
    LITERAL,
    /**
     * <pre>
     * folded: {@code >-}
     *   Each line
     *   is literal
     *   and are joined with spaces
     * </pre>
     */
    FOLDED
}

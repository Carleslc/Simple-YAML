package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.utils.StringUtils;
import org.yaml.snakeyaml.DumperOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SnakeYamlQuoteValue<T> implements QuoteValue<T> {

    private static final Map<QuoteStyle, DumperOptions.ScalarStyle> QUOTE_SCALAR_STYLES = mapQuoteScalarStyles();

    private final T value;
    private final QuoteStyle quoteStyle;

    public SnakeYamlQuoteValue(T value, QuoteStyle quoteStyle) {
        this.value = value;
        this.quoteStyle = quoteStyle;
    }

    public T getValue() {
        return this.value;
    }

    public QuoteStyle getQuoteStyle() {
        return this.quoteStyle;
    }

    public static DumperOptions.ScalarStyle getQuoteScalarStyle(final QuoteStyle quoteStyle) {
        return QUOTE_SCALAR_STYLES.get(quoteStyle);
    }

    private static Map<QuoteStyle, DumperOptions.ScalarStyle> mapQuoteScalarStyles() {
        final Map<QuoteStyle, DumperOptions.ScalarStyle> map = new HashMap<>();
        map.put(QuoteStyle.DOUBLE, DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        map.put(QuoteStyle.SINGLE, DumperOptions.ScalarStyle.SINGLE_QUOTED);
        map.put(QuoteStyle.PLAIN, DumperOptions.ScalarStyle.PLAIN);
        map.put(null, DumperOptions.ScalarStyle.PLAIN);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return this.quoteStyle.toString() + "=" + (this.value == null ? "!!null" : StringUtils.quoteNewLines(this.value.toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnakeYamlQuoteValue<?> that = (SnakeYamlQuoteValue<?>) o;
        return Objects.equals(value, that.value) && quoteStyle == that.quoteStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, quoteStyle);
    }
}

package org.simpleyaml.configuration.implementation.api;

import org.simpleyaml.utils.StringUtils;

import java.util.Objects;

public class QuoteValue<T> {

    protected final T value;
    protected final QuoteStyle quoteStyle;

    public QuoteValue(T value, QuoteStyle quoteStyle) {
        this.value = value;
        this.quoteStyle = quoteStyle;
    }

    public T getValue() {
        return this.value;
    }

    public QuoteStyle getQuoteStyle() {
        return this.quoteStyle;
    }

    @Override
    public String toString() {
        return this.quoteStyle.toString() + "=" + (this.value == null ? "!!null" : StringUtils.quoteNewLines(this.value.toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteValue<?> that = (QuoteValue<?>) o;
        return Objects.equals(value, that.value) && quoteStyle == that.quoteStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, quoteStyle);
    }

}

package org.simpleyaml.configuration.implementation;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

public class SnakeYamlImplementation implements YamlImplementation {

    protected static final String BLANK_CONFIG = "{}\n";

    private SnakeYamlConstructor yamlConstructor;
    private SnakeYamlRepresenter yamlRepresenter;
    private DumperOptions yamlOptions;
    private Yaml yaml;

    public SnakeYamlImplementation() {
        this(new SnakeYamlRepresenter());
    }

    public SnakeYamlImplementation(final SnakeYamlRepresenter yamlRepresenter) {
        this(new SnakeYamlConstructor(), yamlRepresenter, new DumperOptions());
    }

    public SnakeYamlImplementation(final SnakeYamlConstructor yamlConstructor, final SnakeYamlRepresenter yamlRepresenter, final DumperOptions yamlOptions) {
        this.setYaml(yamlConstructor, yamlRepresenter, yamlOptions);
    }

    protected final void setYaml(final SnakeYamlConstructor yamlConstructor, final SnakeYamlRepresenter yamlRepresenter, final DumperOptions yamlOptions) {
        this.yamlConstructor = yamlConstructor;
        this.yamlRepresenter = yamlRepresenter;
        this.yamlOptions = yamlOptions;
        this.yaml = new Yaml(this.yamlConstructor, this.yamlRepresenter, this.yamlOptions);
    }

    public Yaml getYaml() {
        return this.yaml;
    }

    public SnakeYamlConstructor getConstructor() {
        return this.yamlConstructor;
    }

    public SnakeYamlRepresenter getRepresenter() {
        return this.yamlRepresenter;
    }

    public DumperOptions getDumperOptions() {
        return this.yamlOptions;
    }

    @Override
    public String dump(final Map<String, Object> values, final YamlConfigurationOptions options) {
        this.configure(options);

        String dump = this.yaml.dump(values);

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }

    @Override
    public Map<String, Object> load(final String contents) throws InvalidConfigurationException {
        final Map<String, Object> values;
        try {
            values = this.yaml.load(contents);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }
        return values;
    }

    @Override
    public void configure(final YamlConfigurationOptions options) {
        this.yamlOptions.setAllowUnicode(options.isUnicode());

        this.yamlOptions.setIndent(options.indent());
        this.yamlOptions.setIndicatorIndent(options.indentList());
        this.yamlOptions.setIndentWithIndicator(true);

        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yamlRepresenter.setDefaultScalarStyle(
                SnakeYamlQuoteValue.getQuoteScalarStyle(options.quoteStyleDefaults().getDefaultQuoteStyle()));
    }

    @Override
    public <T> QuoteValue<T> quoteValue(final T value, final QuoteStyle quoteStyle) {
        return new SnakeYamlQuoteValue<>(value, quoteStyle);
    }

}

package org.simpleyaml.configuration.implementation.snakeyaml;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.api.QuoteValue;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class SnakeYamlImplementation implements YamlImplementation {

    protected static final String BLANK_CONFIG = "{}\n";

    private SnakeYamlConstructor yamlConstructor;
    private SnakeYamlRepresenter yamlRepresenter;
    private DumperOptions yamlOptions;
    private LoaderOptions loaderOptions;
    private Resolver resolver;
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
        this.setYaml(yamlConstructor, yamlRepresenter, yamlOptions, new LoaderOptions(), new Resolver());
    }

    protected final void setYaml(final SnakeYamlConstructor yamlConstructor,
                                 final SnakeYamlRepresenter yamlRepresenter,
                                 final DumperOptions yamlOptions,
                                 final LoaderOptions loaderOptions,
                                 final Resolver resolver) {
        this.yamlConstructor = yamlConstructor;
        this.yamlRepresenter = yamlRepresenter;
        this.yamlOptions = yamlOptions;
        this.loaderOptions = loaderOptions;
        this.resolver = resolver;
        this.yaml = new Yaml(this.yamlConstructor, this.yamlRepresenter, this.yamlOptions, this.loaderOptions, this.resolver);
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

    public LoaderOptions getLoaderOptions() {
        return this.loaderOptions;
    }

    public Resolver getResolver() {
        return this.resolver;
    }

    @Override
    public String dump(final Map<String, Object> values, final YamlConfigurationOptions options) throws IOException {
        final StringWriter stringWriter = new StringWriter();

        this.dump(stringWriter, values, options);

        String dump = stringWriter.toString();

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }

    @Override
    public void dump(final Writer writer, final Map<String, Object> values, final YamlConfigurationOptions options) throws IOException {
        this.configure(options);

        try {
            this.yaml.dump(values, writer);
        } catch (YAMLException e) {
            throw new IOException(e);
        }
    }

    @Override
    @SuppressWarnings("DuplicateThrows")
    public Map<String, Object> load(final Reader reader) throws IOException, InvalidConfigurationException {
        if (reader == null) {
            return new LinkedHashMap<>();
        }
        final Map<String, Object> values;
        try {
            values = this.yaml.load(reader);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        } finally {
            reader.close();
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

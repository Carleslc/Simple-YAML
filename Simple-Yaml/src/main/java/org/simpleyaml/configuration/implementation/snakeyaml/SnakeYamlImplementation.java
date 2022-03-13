package org.simpleyaml.configuration.implementation.snakeyaml;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.implementation.api.YamlImplementationCommentable;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class SnakeYamlImplementation extends YamlImplementationCommentable {

    private SnakeYamlConstructor yamlConstructor;
    private SnakeYamlRepresenter yamlRepresenter;
    private DumperOptions dumperOptions;
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
                                 final DumperOptions dumperOptions,
                                 final LoaderOptions loaderOptions,
                                 final Resolver resolver) {
        this.yamlConstructor = yamlConstructor;
        this.yamlRepresenter = yamlRepresenter;
        this.dumperOptions = dumperOptions;
        this.loaderOptions = loaderOptions;
        this.resolver = resolver;
        this.yaml = new Yaml(this.yamlConstructor, this.yamlRepresenter, this.dumperOptions, this.loaderOptions, this.resolver);
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
        return this.dumperOptions;
    }

    public LoaderOptions getLoaderOptions() {
        return this.loaderOptions;
    }

    public Resolver getResolver() {
        return this.resolver;
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
    public void dump(final Writer writer, final Map<String, Object> values) throws IOException {
        this.configure(this.options);

        if (this.hasContent(writer, values)) {
            this.dumpYaml(writer, values);
        }
    }

    protected void dumpYaml(final Writer writer, final Map<String, Object> values) throws IOException {
        try {
            this.yaml.dump(values, writer);
        } catch (YAMLException e) {
            throw new IOException(e);
        }
    }

    protected boolean hasContent(final Writer writer, final Map<String, Object> values) throws IOException {
        if (values == null || values.isEmpty()) {
            if (writer != null) {
                writer.write("");
            }
            return false;
        }
        return true;
    }

    @Override
    public void configure(final YamlConfigurationOptions options) {
        super.configure(options);

        this.dumperOptions.setAllowUnicode(options.isUnicode());

        this.dumperOptions.setIndent(options.indent());
        this.dumperOptions.setIndicatorIndent(options.indentList());
        this.dumperOptions.setIndentWithIndicator(true);

        this.dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yamlRepresenter.setDefaultScalarStyle(
                SnakeYamlQuoteValue.getQuoteScalarStyle(options.quoteStyleDefaults().getDefaultQuoteStyle()));

        final boolean useComments = options.useComments();
        this.loaderOptions.setProcessComments(useComments);
        this.dumperOptions.setProcessComments(useComments);
    }
}

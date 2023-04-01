package org.simpleyaml.configuration.implementation.snakeyaml;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.implementation.api.YamlImplementationCommentable;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * YAML implementation using snakeyaml low-level API and snakeyaml comment processing available if {@link YamlConfigurationOptions#useComments()} is enabled.
 */
public class SnakeYamlImplementation extends YamlImplementationCommentable {

    protected final SnakeYamlConstructor yamlConstructor;
    protected final SnakeYamlRepresenter yamlRepresenter;
    protected final DumperOptions dumperOptions;
    protected final LoaderOptions loaderOptions;
    protected final Resolver resolver;
    protected final Yaml yaml;

    public SnakeYamlImplementation() {
        this(new LoaderOptions(), new DumperOptions());
    }

    public SnakeYamlImplementation(final LoaderOptions loaderOptions, final DumperOptions dumperOptions) {
        this(new SnakeYamlConstructor(loaderOptions), new SnakeYamlRepresenter(dumperOptions));
    }

    public SnakeYamlImplementation(final SnakeYamlRepresenter yamlRepresenter) {
        this(new SnakeYamlConstructor(new LoaderOptions()), yamlRepresenter);
    }

    public SnakeYamlImplementation(final SnakeYamlConstructor yamlConstructor,
                                   final SnakeYamlRepresenter yamlRepresenter) {
        this(yamlConstructor, yamlRepresenter, new Resolver());
    }

    public SnakeYamlImplementation(final SnakeYamlConstructor yamlConstructor,
                                 final SnakeYamlRepresenter yamlRepresenter,
                                 final Resolver resolver) {
        this.yamlConstructor = yamlConstructor;
        this.yamlRepresenter = yamlRepresenter;
        this.loaderOptions = yamlConstructor.getLoadingConfig();
        this.dumperOptions = yamlRepresenter.getDumperOptions();
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
    public void load(final Reader reader, final ConfigurationSection section) throws IOException, InvalidConfigurationException {
        this.configure(this.options);

        if (reader != null && section != null) {
            try {
                SnakeYamlCommentMapper yamlCommentMapper = null;
                KeyTree.Node node = null;

                if (this.options.useComments()) {
                    this.yamlCommentMapper = yamlCommentMapper = new SnakeYamlCommentMapper(this.options);

                    node = yamlCommentMapper.getKeyTree().getRoot();
                }

                final MappingNode root = (MappingNode) this.yaml.compose(reader);

                this.trackMapping(root, section, node, yamlCommentMapper);

                if (this.yamlCommentMapper != null) {
                    ((SnakeYamlCommentMapper) this.yamlCommentMapper).trackFooter(root);
                }
            } catch (final YAMLException e) {
                throw new InvalidConfigurationException(e);
            } catch (final ClassCastException e) {
                throw new InvalidConfigurationException("Top level is not a Map.");
            } finally {
                reader.close();
            }
        }
    }

    @Override
    public void dump(final Writer writer, final ConfigurationSection section) throws IOException {
        this.configure(this.options);

        if (this.hasContent(writer, section)) {
            try {
                SnakeYamlCommentMapper yamlCommentMapper = null;
                KeyTree.Node node = null;

                if (this.yamlCommentMapper != null && this.options.useComments()) {
                    yamlCommentMapper = (SnakeYamlCommentMapper) this.yamlCommentMapper;

                    if (section.getParent() == null) {
                        node = yamlCommentMapper.getKeyTree().getRoot();
                    } else {
                        node = yamlCommentMapper.getNode(section.getCurrentPath());
                    }
                }

                final MappingNode mappingNode = this.sectionToMapping(section, node, yamlCommentMapper);

                if (yamlCommentMapper != null) {
                    yamlCommentMapper.setFooter(mappingNode);
                }

                this.yaml.serialize(mappingNode, writer);
            } catch (final YAMLException e) {
                throw new IOException(e);
            } finally {
                writer.close();
            }
        }
    }

    protected void dumpYaml(final Writer writer, final ConfigurationSection section) throws IOException {
        try {
            this.yaml.dump(section, writer);
        } catch (YAMLException e) {
            throw new IOException(e);
        }
    }

    protected boolean hasContent(final Writer writer, final ConfigurationSection section) throws IOException {
        if (writer == null) {
            return false;
        }

        boolean empty = false;

        if (section == null) {
            empty = true;
        } else if (section.isEmpty()) {
            final ConfigurationSection defaultsSection = section.getDefaultSection();
            empty = defaultsSection == null || defaultsSection.isEmpty();
        }

        if (empty) {
            writer.write("");
        }

        return !empty;
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

    @Override
    public void setComment(final String path, final String comment, final CommentType type) {
        if (this.yamlCommentMapper == null) {
            this.options.useComments(true);
            this.yamlCommentMapper = new SnakeYamlCommentMapper(this.options);
        }
        this.yamlCommentMapper.setComment(path, comment, type);
    }

    protected void trackMapping(final MappingNode node, final ConfigurationSection section, final KeyTree.Node parent, final SnakeYamlCommentMapper yamlCommentMapper) {
        if (node != null) {
            this.yamlConstructor.flattenMapping(node);

            boolean useComments = yamlCommentMapper != null;

            for (final NodeTuple nodeTuple : node.getValue()) {
                Node key = nodeTuple.getKeyNode();
                Node value = resolveAnchor(nodeTuple.getValueNode());

                final String name = this.getName(key, true);

                boolean isSerializable = value instanceof MappingNode && this.yamlConstructor.hasSerializedTypeKey((MappingNode) value);

                KeyTree.Node childNode = null;

                if (useComments) {
                    childNode = yamlCommentMapper.track(parent, name, key, value);

                    if (value instanceof SequenceNode) {
                        this.trackSequence((SequenceNode) value, childNode, yamlCommentMapper);
                    } else if (isSerializable) {
                        this.trackMapping((MappingNode) value, null, childNode, yamlCommentMapper);
                    }
                }

                if (section != null) {
                    if (value instanceof MappingNode && !isSerializable) {
                        this.trackMapping((MappingNode) value, section.createSection(name), childNode, yamlCommentMapper);
                    } else {
                        section.set(name, this.yamlConstructor.construct(value));
                    }
                }
            }

            if (useComments) {
                yamlCommentMapper.clearCurrentNodeIfNoComments();
            }
        }
    }

    protected void trackSequence(final SequenceNode node, final KeyTree.Node parent, final SnakeYamlCommentMapper yamlCommentMapper) {
        int i = 0;

        for (Node element : node.getValue()) {
            element = resolveAnchor(element);

            final KeyTree.Node elementNode = yamlCommentMapper.trackElement(parent, this.getName(element, false), element, i);

            if (element instanceof SequenceNode) {
                this.trackSequence((SequenceNode) element, elementNode, yamlCommentMapper);
            } else if (element instanceof MappingNode) {
                this.trackMapping((MappingNode) element, null, elementNode, yamlCommentMapper);
            }

            i++;
        }
    }

    protected MappingNode sectionToMapping(final ConfigurationSection section, final KeyTree.Node node, final SnakeYamlCommentMapper yamlCommentMapper) {
        List<NodeTuple> nodes = new ArrayList<>();

        boolean useComments = yamlCommentMapper != null && node != null;

        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            Node key = this.yamlRepresenter.represent(entry.getKey());
            Node value;

            if (entry.getValue() instanceof ConfigurationSection) {
                final ConfigurationSection childSection = (ConfigurationSection) entry.getValue();
                final KeyTree.Node childNode = useComments ? node.getPriority(childSection.getName()) : null;
                value = sectionToMapping(childSection, childNode, yamlCommentMapper);
            } else {
                value = this.yamlRepresenter.represent(entry.getValue());
            }

            if (useComments) {
                final String name = this.getName(key, true);

                final KeyTree.Node childNode = node.getPriority(name);

                yamlCommentMapper.setComments(childNode, key, value);

                if (value instanceof SequenceNode) {
                    this.setCommentsSequence((SequenceNode) value, childNode, yamlCommentMapper);
                } else if (value instanceof MappingNode) {
                    this.setCommentsMapping((MappingNode) value, childNode, yamlCommentMapper);
                }
            }

            nodes.add(new NodeTuple(key, value));
        }

        return new MappingNode(Tag.MAP, nodes, this.dumperOptions.getDefaultFlowStyle());
    }

    protected void setCommentsSequence(final SequenceNode sequence, final KeyTree.Node node, final SnakeYamlCommentMapper yamlCommentMapper) {
        if (node != null && node.isList()) {
            int i = 0;

            for (Node element : sequence.getValue()) {
                element = resolveAnchor(element);

                KeyTree.Node elementNode = node.getElement(i);

                if (elementNode == null) {
                    final String name = this.getName(element, false);

                    if (name != null) {
                        elementNode = node.getPriority(name);
                    }
                }

                yamlCommentMapper.setComments(elementNode, element, null);

                if (element instanceof SequenceNode) {
                    this.setCommentsSequence((SequenceNode) element, elementNode, yamlCommentMapper);
                } else if (element instanceof MappingNode) {
                    this.setCommentsMapping((MappingNode) element, elementNode, yamlCommentMapper);
                }

                i++;
            }
        }
    }

    protected void setCommentsMapping(final MappingNode mapping, final KeyTree.Node node, final SnakeYamlCommentMapper yamlCommentMapper) {
        if (node != null) {
            this.yamlConstructor.flattenMapping(mapping);

            for (final NodeTuple nodeTuple : mapping.getValue()) {
                final Node key = nodeTuple.getKeyNode();
                final Node value = resolveAnchor(nodeTuple.getValueNode());

                final String name = this.getName(key, true);

                final KeyTree.Node childNode = node.getPriority(name);

                yamlCommentMapper.setComments(childNode, key, value);

                if (value instanceof SequenceNode) {
                    this.setCommentsSequence((SequenceNode) value, childNode, yamlCommentMapper);
                } else if (value instanceof MappingNode) {
                    this.setCommentsMapping((MappingNode) value, childNode, yamlCommentMapper);
                }
            }
        }
    }

    protected String getName(final Node node, boolean key) {
        String name = null;

        final Object value = this.yamlConstructor.construct(node);

        if (key || value instanceof String || value instanceof Number || value instanceof Boolean) {
            name = String.valueOf(value);
        }

        return name;
    }

    protected static Node resolveAnchor(Node node) {
        while (node instanceof AnchorNode) {
            node = ((AnchorNode) node).getRealNode();
        }
        return node;
    }
}

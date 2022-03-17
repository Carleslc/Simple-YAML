package org.simpleyaml.configuration.implementation.api;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.Commentable;
import org.simpleyaml.configuration.comments.YamlCommentMapper;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;

/**
 * A YAML implementation capable of processing comments.
 */
public abstract class YamlImplementationCommentable implements YamlImplementation, Commentable {

    /**
     * A comment mapper to add comments to sections or values
     **/
    protected YamlCommentMapper yamlCommentMapper;

    /**
     * Configuration options for loading and dumping Yaml.
     */
    protected YamlConfigurationOptions options;

    @Override
    public void setComment(final String path, final String comment, final CommentType type) {
        if (this.yamlCommentMapper != null) {
            this.yamlCommentMapper.setComment(path, comment, type);
        }
    }

    @Override
    public String getComment(final String path, final CommentType type) {
        if (this.yamlCommentMapper == null) {
            return null;
        }
        return this.yamlCommentMapper.getComment(path, type);
    }

    /**
     * Get the comment mapper to get or set comments.
     * @return the comment mapper or null if parsing comments is not enabled
     */
    public YamlCommentMapper getCommentMapper() {
        return this.yamlCommentMapper;
    }

    @Override
    public void configure(final YamlConfigurationOptions options) {
        this.options = options;
    }
}

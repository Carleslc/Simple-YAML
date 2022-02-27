package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.ConfigurationWrapper;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.YamlCommentFormat;
import org.simpleyaml.configuration.comments.YamlCommentFormatter;

public class YamlFileWrapper extends ConfigurationWrapper<YamlFile> {

    protected YamlFileWrapper(final YamlFile configuration, final String path, final YamlFileWrapper parent) {
        super(configuration, path, parent);
    }

    public YamlFileWrapper(final YamlFile configuration, final String path) {
        super(configuration, path);
    }

    /**
     * Set a block comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of {@link YamlFile#options()} {@link YamlConfigurationOptions#commentFormatter()}.
     *
     * @param comment the block comment to add, # character is not needed
     * @return this object, for chaining.
     */
    public YamlFileWrapper comment(final String comment) {
        this.configuration.setComment(this.path, comment, CommentType.BLOCK);
        return this;
    }

    /**
     * Set a block comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of the provided {@link YamlCommentFormatter}.
     *
     * @param comment the block comment to add, # prefix is not needed
     * @param yamlCommentFormatter the comment formatter to use
     * @return this object, for chaining.
     */
    public YamlFileWrapper comment(final String comment, final YamlCommentFormatter yamlCommentFormatter) {
        this.configuration.setComment(this.path, comment, CommentType.BLOCK, yamlCommentFormatter);
        return this;
    }

    /**
     * Set a block comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of the provided {@link YamlCommentFormat}.
     *
     * @param comment the block comment to add, # prefix is not needed
     * @param yamlCommentFormat the comment format to use
     * @return this object, for chaining.
     */
    public YamlFileWrapper comment(final String comment, final YamlCommentFormat yamlCommentFormat) {
        this.configuration.setComment(this.path, comment, CommentType.BLOCK, yamlCommentFormat);
        return this;
    }

    /**
     * Set a side comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of {@link YamlFile#options()} {@link YamlConfigurationOptions#commentFormatter()}.
     *
     * @param comment the side comment to add, # symbol is not needed
     * @return this object, for chaining.
     */
    public YamlFileWrapper commentSide(final String comment) {
        this.configuration.setComment(this.path, comment, CommentType.SIDE);
        return this;
    }

    /**
     * Set a side comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of the provided {@link YamlCommentFormatter}.
     *
     * @param comment the side comment to add, # prefix is not needed
     * @param yamlCommentFormatter the comment formatter to use
     * @return this object, for chaining.
     */
    public YamlFileWrapper commentSide(final String comment, final YamlCommentFormatter yamlCommentFormatter) {
        this.configuration.setComment(this.path, comment, CommentType.SIDE, yamlCommentFormatter);
        return this;
    }

    /**
     * Set a side comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p></p>
     * Comment format will follow the rules of the provided {@link YamlCommentFormat}.
     *
     * @param comment the side to add, # prefix is not needed
     * @param yamlCommentFormat the comment format to use
     * @return this object, for chaining.
     */
    public YamlFileWrapper commentSide(final String comment, final YamlCommentFormat yamlCommentFormat) {
        this.configuration.setComment(this.path, comment, CommentType.SIDE, yamlCommentFormat);
        return this;
    }

    /**
     * Set a blank line at the beginning of the block comment.
     * If currently there is no block comment for this path then it sets "\n" as the block comment.
     *
     * @return this object, for chaining.
     */
    public YamlFileWrapper blankLine() {
        apply(configuration::setBlankLine);
        return this;
    }

    @Override
    public YamlFileWrapper path(final String path) {
        return new YamlFileWrapper(configuration, path, this);
    }

    @Override
    public YamlFileWrapper set(final Object value) {
        super.set(value);
        return this;
    }

    @Override
    public YamlFileWrapper set(final String path, final Object value) {
        super.set(path, value);
        return this;
    }

    @Override
    public YamlFileWrapper addDefault(final Object value) {
        super.addDefault(value);
        return this;
    }

    @Override
    public YamlFileWrapper addDefault(final String path, final Object value) {
        super.addDefault(path, value);
        return this;
    }

    @Override
    public YamlFileWrapper createSection() {
        super.createSection();
        return this;
    }

    @Override
    public YamlFileWrapper createSection(final String path) {
        super.createSection(path);
        return this;
    }

    @Override
    public YamlFileWrapper parent() {
        if (this.parent == null && this.path != null) {
            int lastSectionIndex = this.path.lastIndexOf(this.configuration.options().pathSeparator());

            if (lastSectionIndex >= 0) {
                return new YamlFileWrapper(this.configuration, this.path.substring(0, lastSectionIndex));
            }
        }
        return (YamlFileWrapper) this.parent;
    }

}

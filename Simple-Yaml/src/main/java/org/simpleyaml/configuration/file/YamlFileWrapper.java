package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.ConfigurationWrapper;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatter;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;

/**
 * An alternative API to set values along with comments for a path.
 * <pre>
 * {@code
 * yamlFile.path("test.hello")
 *         .set("Hello")
 *         .comment("Block comment")
 *         .commentSide("Side comment");
 * }
 * </pre>
 * @see YamlFile#path(String)
 */
public class YamlFileWrapper extends ConfigurationWrapper<YamlFile> {

    public YamlFileWrapper(final YamlFile configuration, final String path) {
        super(configuration, path);
    }

    protected YamlFileWrapper(final YamlFile configuration, final String path, final YamlFileWrapper parent) {
        super(configuration, path, parent);
    }

    /**
     * Set a block comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * <p/>
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
    public YamlFileWrapper setChild(final String child, final Object value) {
        super.setChild(child, value);
        return this;
    }

    /**
     * Set the given value to this path.
     * <p>
     * The value will be represented with the specified quote style in the configuration file.
     * <p/>
     * Any existing entry will be replaced, regardless of what the new value is.
     * <p/>
     * Null value is valid and will not remove the key, this is different to {@link #set(Object)}.
     * Instead, a null value will be written as a yaml empty null value.
     *
     * @param value new value to set the path to.
     * @param quoteStyle the quote style to use.
     * @return this object, for chaining.
     */
    public YamlFileWrapper set(final Object value, final QuoteStyle quoteStyle) {
        this.configuration.set(this.path, value, quoteStyle);
        return this;
    }

    /**
     * Set the given value to the provided child path.
     * <p>
     * The value will be represented with the specified quote style in the configuration file.
     * <p/>
     * Any existing entry will be replaced, regardless of what the new value is.
     * <p/>
     * Null value is valid and will not remove the key, this is different to {@link #setChild(String, Object)}.
     * Instead, a null value will be written as a yaml empty null value.
     *
     * @param child the child path of this section path.
     * @param value new value to set the path to.
     * @param quoteStyle the quote style to use.
     * @return this object, for chaining.
     */
    public YamlFileWrapper setChild(final String child, final Object value, final QuoteStyle quoteStyle) {
        this.configuration.set(this.childPath(child), value, quoteStyle);
        return this;
    }

    @Override
    public YamlFileWrapper addDefault(final Object value) {
        super.addDefault(value);
        return this;
    }

    @Override
    public YamlFileWrapper addDefault(final String child, final Object value) {
        super.addDefault(child, value);
        return this;
    }

    @Override
    public YamlFileWrapper createSection() {
        super.createSection();
        return this;
    }

    @Override
    public YamlFileWrapper createSection(final String child) {
        super.createSection(child);
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

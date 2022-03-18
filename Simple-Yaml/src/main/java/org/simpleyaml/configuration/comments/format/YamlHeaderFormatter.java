package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

import static org.simpleyaml.configuration.comments.format.YamlCommentFormatterConfiguration.DEFAULT_COMMENT_PREFIX;
import static org.simpleyaml.utils.StringUtils.BLANK_LINE;

/**
 * The header is the first block comment at the top of the configuration file.
 * <p/>
 * It ends with a blank line or with the end of the file.
 */
public class YamlHeaderFormatter implements CommentFormatter {

    protected YamlCommentFormatterConfiguration configuration;

    protected YamlHeaderFormatter(final YamlCommentFormatterConfiguration configuration) {
        Validate.notNull(configuration);
        this.configuration = configuration;
    }

    public YamlHeaderFormatter(final String commentPrefix, final boolean strip) {
        this(new YamlCommentFormatterConfiguration().prefix(commentPrefix).stripPrefix(strip).suffix(BLANK_LINE));
    }

    public YamlHeaderFormatter() {
        this(DEFAULT_COMMENT_PREFIX, false);
    }

    /**
     * Parse the header of a contents string, like a file contents.
     * <p/>
     * The header is the first block comment at the top of the configuration file.
     * It ends with a blank line or with the end of the file.
     * <p/>
     * If no blank line is found after the first block comment and it is not the end of the file then null is returned
     * (the comment will be attached to the first key, so that comment is not considered a header).
     * <p/>
     * The blank line suffix will be stripped away, i.e. the result string will not have a blank line at the end.
     * <p/>
     * If {@link #stripPrefix} is true then {@link String#trim()} will be applied to every line of the header
     * and the {@link #commentPrefix()} will be stripped away.
     *
     * @param raw the string to parse
     * @return the header
     */
    @Override
    public String parse(final String raw, final CommentType type, final KeyTree.Node node) throws IOException {
        if (raw == null) {
            return null;
        }
        return parse(new StringReader(raw), type, node);
    }

    @Override
    public String parse(final Reader raw, final CommentType type, final KeyTree.Node node) throws IOException {
        if (raw == null) {
            return null;
        }
        try (BufferedReader reader = raw instanceof BufferedReader ? (BufferedReader) raw : new BufferedReader(raw)) {
            final StringBuilder headerBuilder = new StringBuilder();
            boolean headerFound = false;
            String line, trim;
            while ((line = reader.readLine()) != null) {
                trim = line.trim();
                if (trim.isEmpty()) {
                    if (headerFound) {
                        break; // stop at the first blank line after header comment
                    }
                    headerBuilder.append('\n');
                } else if (trim.startsWith(YamlCommentFormatterConfiguration.COMMENT_INDICATOR)) {
                    if (this.stripPrefix()) {
                        // append comment without comment prefix and ignore leading and trailing spaces
                        line = StringUtils.stripPrefix(trim, this.commentPrefix(), YamlCommentFormatterConfiguration.COMMENT_INDICATOR);
                    }
                    if (headerFound) {
                        headerBuilder.append('\n'); // there was a previous comment, adding a new line before this line
                        // this achieves that no \n is added to the end of the whole comment
                    } else {
                        headerFound = true;
                    }
                    headerBuilder.append(line);
                } else {
                    // key found before blank line, so the comment is considered to be the first key block comment, not the header
                    headerFound = false;
                    break;
                }
            }
            if (headerFound) {
                return headerBuilder.toString();
            }
        }
        return null;
    }

    /**
     * Given the header (for instance the one returned by {@link #parse}),
     * returns the final string formatted to be dumped somewhere like a file.
     * <p/>
     * The header ends with a blank line and every line is prefixed with the {@link #commentPrefix()}.
     * <p>
     * If all header lines are already prefixed with a # then the {@link #commentPrefix()} is not applied.
     * @param header the header to dump
     * @return the final string to be dumped
     */
    @Override
    public String dump(final String header, final CommentType type, final KeyTree.Node node) {
        if (header == null) {
            return null;
        }
        String prefixFirst = null, prefixMultiline = null;
        String suffixLast = null, suffixMultiline = null;
        // All header lines must be prefixed with #, with no blank lines
        if (!StringUtils.allLinesArePrefixed(header, YamlCommentFormatterConfiguration.COMMENT_INDICATOR)) {
            prefixMultiline = this.commentPrefix();
            prefixFirst = this.configuration.prefixFirst(prefixMultiline);
            suffixMultiline = this.configuration.suffixMultiline();

            // Ensure the first line has the prefix multiline
            if (!prefixFirst.equals(prefixMultiline)) {
                final String prefixFirstSuffix = '\n' + prefixMultiline;
                if (!prefixFirst.endsWith(prefixFirstSuffix)) {
                    prefixFirst += prefixFirstSuffix;
                }
            }
        }
        // Header must end with the configuration suffix (a blank line by default)
        if (!header.endsWith(configuration.suffixLast())) {
            suffixLast = configuration.suffixLast();
        }
        // Ensure the last line has the suffix multiline
        if (suffixLast != null && suffixMultiline != null && !suffixMultiline.isEmpty()) {
            final String suffixLastPrefix = suffixMultiline + '\n';
            if (!suffixLast.startsWith(suffixLastPrefix)) {
                suffixLast = suffixLastPrefix + suffixLast;
            }
        }
        return CommentFormatter.format(prefixFirst, prefixMultiline, header, suffixMultiline, suffixLast);
    }

    /**
     * If strip is true then {@link String#trim()} will be applied to every line of the header
     * and the {@link #commentPrefix()} will be stripped away.
     * <p/>
     * Default is false.
     * @return if stripping the prefix is desired
     */
    public boolean stripPrefix() {
        return this.configuration.stripPrefix();
    }

    /**
     * Set if stripping the prefix is desired.
     * <p/>
     * If strip is true then {@link String#trim()} will be applied to every line of the header
     * and the {@link #commentPrefix()} will be stripped away.
     * <p/>
     * Default is false.
     * @param stripPrefix if stripping the prefix is desired
     * @return this object, for chaining
     */
    public YamlHeaderFormatter stripPrefix(final boolean stripPrefix) {
        this.configuration.stripPrefix(stripPrefix);
        return this;
    }

    /**
     * Gets the comment prefix to apply to every line of the header.
     * <p/>
     * By default is "# ", i.e. a # followed by a space.
     * @return the comment prefix
     */
    public String commentPrefix() {
        return this.configuration.prefixMultiline(DEFAULT_COMMENT_PREFIX);
    }

    /**
     * Sets the comment prefix to apply to every line of the header.
     * <p/>
     * By default is "# ", i.e. a # followed by a space.
     * @param commentPrefix the comment prefix
     * @return this object, for chaining
     */
    public YamlHeaderFormatter commentPrefix(final String commentPrefix) {
        String prefixFirst = this.configuration.prefixFirst(DEFAULT_COMMENT_PREFIX);
        if (prefixFirst.equals(DEFAULT_COMMENT_PREFIX)) {
            prefixFirst = commentPrefix;
        }
        this.configuration.prefix(prefixFirst, commentPrefix);
        return this;
    }

    /**
     * Sets the comment prefix to apply to the beginning of the header.
     * @param commentPrefixFirst the comment prefix to apply to the beginning of the header
     * @return this object, for chaining
     */
    public YamlHeaderFormatter prefixFirst(final String commentPrefixFirst) {
        this.configuration.prefix(commentPrefixFirst, this.commentPrefix());
        return this;
    }

    /**
     * Set the comment suffix to append to every line of the header.
     * @param suffixMultiline the suffix to append to every line of the header
     * @return this object, for chaining
     */
    public YamlHeaderFormatter commentSuffix(final String suffixMultiline) {
        this.configuration.suffix(this.configuration.suffixLast(BLANK_LINE), suffixMultiline);
        return this;
    }

    /**
     * Set the suffix to append to the end of the header.
     * If not included, a blank line \n\n will be added to the suffix.
     * <p/>
     * Default is a blank line.
     * @param suffixLast the suffix to append to the end of the header
     * @return this object, for chaining
     */
    public YamlHeaderFormatter suffixLast(String suffixLast) {
        if (suffixLast == null) {
            suffixLast = BLANK_LINE;
        } else if (!suffixLast.endsWith(BLANK_LINE)) {
            suffixLast += BLANK_LINE;
        }
        this.configuration.suffix(suffixLast);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YamlHeaderFormatter that = (YamlHeaderFormatter) o;
        return Objects.equals(configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration);
    }
}

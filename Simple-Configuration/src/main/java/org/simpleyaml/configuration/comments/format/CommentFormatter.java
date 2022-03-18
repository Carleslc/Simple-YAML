package org.simpleyaml.configuration.comments.format;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.utils.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a comment between a raw-format meant to be read by computers
 * to a human-format meant to be read by humans and vice versa.
 */
public interface CommentFormatter {

    /**
     * Parse the comment from a reader that may contain a raw-formatted comment (for instance from a configuration file)
     * to a human-friendly contentful representation of that comment.
     *
     * @param raw the comment to parse that may contain special format characters with leading and trailing space
     * @param type the comment type
     * @param node the comment node
     * @return the comment in the expected format to be read by humans
     * @throws IOException if comment cannot be parsed
     */
    String parse(final Reader raw, final CommentType type, final KeyTree.Node node) throws IOException;

    /**
     * Given a comment returns the raw-formatted string to be dumped somewhere like a file.
     *
     * @param comment the comment to be dumped
     * @param type the comment type
     * @param node the comment node
     * @return the raw-formatted comment string to be dumped, for instance to a configuration file
     */
    String dump(final String comment, final CommentType type, final KeyTree.Node node);

    /**
     * Parse the comment from a string that may contain a raw-formatted comment (for instance from a configuration file)
     * to a human-friendly contentful representation of that comment.
     *
     * @param raw the comment to parse that may contain special format characters with leading and trailing space
     * @param type the comment type
     * @param node the comment node
     * @return the comment in the expected format to be read by humans
     */
    default String parse(final String raw, final CommentType type, final KeyTree.Node node) throws IOException {
        return parse(new StringReader(raw), type, node);
    }

    default String parse(final Reader raw, final CommentType type) throws IOException {
        return parse(raw, type, null);
    }

    default String parse(final String raw, final CommentType type) throws IOException {
        return parse(raw, type, null);
    }

    default String parse(final Reader raw) throws IOException {
        return parse(raw, CommentType.BLOCK);
    }

    default String parse(final String raw) throws IOException {
        return parse(raw, CommentType.BLOCK);
    }

    default String dump(final String comment, final CommentType type) {
        return dump(comment, type, null);
    }

    default String dump(final String comment) {
        return dump(comment, CommentType.BLOCK);
    }

    static String format(final int indent,
                         String prefixFirst, String prefixMultiline,
                         final String comment,
                         final CommentType type,
                         final String suffixMultiline, String suffixLast) {

        if (comment == null) {
            return "";
        }

        Stream<String> stream = Arrays.stream(StringUtils.lines(comment, comment.trim().isEmpty()));

        final String indentation = StringUtils.indentation(indent);
        final String indentLine = "\n" + indentation;

        String delimiter;

        if (suffixMultiline == null) {
            delimiter = indentLine;
        } else {
            delimiter = String.join(indentLine, StringUtils.lines(suffixMultiline, false)) + indentLine;
        }

        if (prefixFirst == null) {
            prefixFirst = "";
        } else {
            prefixFirst = String.join(indentLine, StringUtils.lines(prefixFirst, false));
        }

        if (prefixMultiline == null) {
            prefixMultiline = prefixFirst;
        } else {
            prefixMultiline = String.join(indentLine, StringUtils.lines(prefixMultiline, false));
        }

        if (type == CommentType.BLOCK) {
            prefixFirst = indentation + prefixFirst;
        }

        delimiter += prefixMultiline;

        if (suffixLast == null) {
            suffixLast = "";
        } else {
            suffixLast = String.join(indentLine, StringUtils.lines(suffixLast, false));
        }

        return stream.collect(Collectors.joining(delimiter, prefixFirst, suffixLast));
    }

    static String format(final String prefixFirst, final String prefixMultiline,
                         final String comment,
                         final String suffixMultiline, final String suffixLast) {
        return format(0, prefixFirst, prefixMultiline, comment, CommentType.BLOCK, suffixMultiline, suffixLast);
    }

    static String format(final int indent, final String comment, final CommentType type, final CommentFormatterConfiguration configuration) {
        return format(
                indent,
                configuration.prefixFirst(), configuration.prefixMultiline(),
                comment,
                type,
                configuration.suffixMultiline(), configuration.suffixLast()
        );
    }
}

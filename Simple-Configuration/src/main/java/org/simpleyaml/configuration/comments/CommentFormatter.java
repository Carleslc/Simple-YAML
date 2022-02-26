package org.simpleyaml.configuration.comments;

import org.simpleyaml.utils.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a comment between a raw-format meant to be read by computers
 * to a human-format meant to be read by humans and vice versa.
 */
public interface CommentFormatter {

    /**
     * Parse the comment from a string that may contain a raw-formatted comment (for instance from a configuration file)
     * to a human-friendly contentful representation of that comment.
     *
     * @param raw the comment to parse that may contain special format characters with leading and trailing space
     * @param type the comment type
     * @param node the comment node
     * @return the comment in the expected format to be read by humans
     */
    String parse(String raw, CommentType type, KeyTree.Node node);

    /**
     * Given a comment returns the raw-formatted string to be dumped somewhere like a file.
     *
     * @param comment the comment to be dumped
     * @param type the comment type
     * @param node the comment node
     * @return the raw-formatted comment string to be dumped, for instance to a configuration file
     */
    String dump(String comment, CommentType type, KeyTree.Node node);

    default String parse(String raw, CommentType type) {
        return parse(raw, type, null);
    }

    default String parse(String raw) {
        return parse(raw, CommentType.BLOCK);
    }

    default String dump(String comment, CommentType type) {
        return dump(comment, type, null);
    }

    default String dump(String comment) {
        return dump(comment, CommentType.BLOCK);
    }

    static String format(int indent,
                         String prefixFirst, String prefixMultiline,
                         String comment,
                         CommentType type,
                         String suffixMultiline, String suffixLast) {

        if (comment == null) {
            return "";
        }

        Stream<String> stream = Arrays.stream(StringUtils.lines(comment));

        final String indentation = StringUtils.indentation(indent);

        String delimiter = "\n" + indentation;

        if (suffixMultiline != null) {
            delimiter = suffixMultiline + delimiter;
        }

        if (prefixFirst == null) {
            prefixFirst = "";
        }

        if (prefixMultiline == null) {
            prefixMultiline = prefixFirst;
        }

        if (type == CommentType.BLOCK) {
            prefixFirst = indentation + prefixFirst;
        }

        delimiter += prefixMultiline;

        if (suffixLast == null) {
            suffixLast = "";
        }

        return stream.collect(Collectors.joining(delimiter, prefixFirst, suffixLast));
    }

    static String format(String prefixFirst, String prefixMultiline,
                         String comment,
                         String suffixMultiline, String suffixLast) {
        return format(0, prefixFirst, prefixMultiline, comment, CommentType.BLOCK, suffixMultiline, suffixLast);
    }

    static String format(int indent, String comment, CommentType type, CommentFormatterConfiguration configuration) {
        return format(
                indent,
                configuration.prefixFirst(), configuration.prefixMultiline(),
                comment,
                type,
                configuration.suffixMultiline(), configuration.suffixLast()
        );
    }
}

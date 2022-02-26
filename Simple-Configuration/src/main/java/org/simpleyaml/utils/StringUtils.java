package org.simpleyaml.utils;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class StringUtils {

    public static final String BLANK_LINE = "\n\n";

    private static final Pattern NEW_LINE = Pattern.compile("\r?\n");
    private static final Pattern INDENTATION = Pattern.compile("^[^\\S\n]+", Pattern.MULTILINE); // all leading whitespace except new line

    public static String[] lines(final String content) {
        return NEW_LINE.split(content);
    }

    public static String indentation(final int n) {
        return padding(n, ' ');
    }

    public static String padding(final int n, final char pad) {
        if (n <= 0) {
            return "";
        }
        char[] padding = new char[n];
        for (int i = 0; i < n; i++) {
            padding[i] = pad;
        }
        return new String(padding);
    }

    public static String stripIndentation(String s) {
        if (s == null) {
            return null;
        }
        return INDENTATION.matcher(s).replaceAll("");
    }

    public static String stripPrefix(String s, String prefix) {
        return stripPrefix(s, prefix, null);
    }

    public static String stripPrefix(String s, String prefix, String defaultPrefix) {
        if (s == null) {
            return null;
        }
        int skip = 0;
        if (prefix != null && s.startsWith(prefix)) {
            skip = prefix.length();
        } else if (defaultPrefix != null && s.startsWith(defaultPrefix)) {
            skip = defaultPrefix.length();
        }
        return s.substring(skip);
    }

    public static String afterNewLine(String s) {
        if (s == null) {
            return null;
        }
        int nl = s.indexOf('\n');
        return nl >= 0 ? s.substring(nl + 1) : "";
    }

    public static boolean allLinesArePrefixed(final String comment, final String prefix) {
        return Arrays.stream(lines(comment)).allMatch(line -> line.trim().startsWith(prefix));
    }

    public static boolean allLinesArePrefixedOrBlank(final String comment, final String prefix) {
        return Arrays.stream(lines(comment)).map(String::trim).allMatch(line -> line.isEmpty() || line.startsWith(prefix));
    }

}

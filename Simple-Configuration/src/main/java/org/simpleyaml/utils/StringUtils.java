package org.simpleyaml.utils;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class StringUtils {

    public static final String BLANK_LINE = "\n\n";

    public static final Pattern NEW_LINE = Pattern.compile("\\R"); // like "\r?\n" but also with other unicode line separators
    public static final Pattern INDENTATION = Pattern.compile("^[^\\S\n]+", Pattern.MULTILINE); // all leading whitespace except new line

    public static final Pattern LIST_INDEX = Pattern.compile("^(.*)\\[(-?\\d+)]$", Pattern.DOTALL); // for list indexing: list[0]

    public static final char ESCAPE_CHAR = '\\';

    public static String[] splitNewLines(final String s, int limit) {
        return NEW_LINE.split(s, limit);
    }

    public static String[] lines(final String content, boolean stripTrailingNewLines) {
        return splitNewLines(content, stripTrailingNewLines ? 0 : -1);
    }

    public static String[] lines(final String content) {
        return lines(content, true);
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

    public static String[] splitTrailingNewLines(String s) {
        if (s == null) {
            return null;
        }
        final String[] parts = new String[2];
        int i = s.length() - 1;
        while (i >= 0 && s.charAt(i) == '\n') {
            i--;
        }
        parts[0] = i >= 0 ? s.substring(0, i + 1) : "";
        parts[1] = s.substring(i + 1);
        return parts;
    }

    public static int lastSeparatorIndex(final String path, final char sep, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        boolean escape = false;
        int len = path.length();
        int idx = -1;
        for (int i = fromIndex; i < len; i++) {
            char c = path.charAt(i);
            if (c == ESCAPE_CHAR) { // escape separator with \
                escape = !escape;
            } else {
                if (c == sep && !escape) {
                    idx = i;
                }
                escape = false;
            }
        }
        return idx;
    }

    public static int lastSeparatorIndex(final String path, final char sep) {
        return lastSeparatorIndex(path, sep, 0);
    }

    public static int firstSeparatorIndex(String path, final char sep, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        boolean escape = false;
        int len = path.length();
        for (int i = fromIndex; i < len; i++) {
            char c = path.charAt(i);
            if (c == ESCAPE_CHAR) { // escape separator with \
                escape = !escape;
            } else if (c == sep && !escape) {
                return i;
            } else {
                escape = false;
            }
        }
        return -1;
    }

    public static int firstSeparatorIndex(String path, final char sep) {
        return firstSeparatorIndex(path, sep, 0);
    }

    public static boolean allLinesArePrefixed(final String comment, final String prefix) {
        return Arrays.stream(lines(comment, false)).allMatch(line -> line.trim().startsWith(prefix));
    }

    public static boolean allLinesArePrefixedOrBlank(final String comment, final String prefix) {
        return Arrays.stream(lines(comment)).map(String::trim).allMatch(line -> line.isEmpty() || line.startsWith(prefix));
    }

    public static String quoteNewLines(final String s) {
        return NEW_LINE.matcher(s).replaceAll("\\\\n");
    }

    public static String stripCarriage(final String s) {
        return s != null ? s.replace("\r", "") : null;
    }

    public static String wrap(final String value) {
        return value == null ? "" : '\'' + value + '\'';
    }

    private static String SEPARATOR = ".";
    private static String ESCAPE_SEPARATOR = ESCAPE_CHAR + SEPARATOR;

    public static void setSeparator(final char separator) {
        SEPARATOR = String.valueOf(separator);
        ESCAPE_SEPARATOR = ESCAPE_CHAR + SEPARATOR;
    }

    public static String escape(final String s) {
        return s != null ? s.replace(SEPARATOR, ESCAPE_SEPARATOR) : null;
    }
}

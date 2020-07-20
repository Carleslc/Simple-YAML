package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.utils.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentReader extends CommentMapper {

    private static final Pattern KEY_REGEX = Pattern.compile("^([ \\t-]*)([^#]*?)[ \\t]*:.*");

    private static final Pattern ELEMENT_REGEX = Pattern.compile("^([ \\t-]*)([^#\\n]*[^#\\s-]+).*");

    private static final Pattern OTHER_REGEX = Pattern.compile("^([ \\t-]*).*");

    protected BufferedReader reader;

    protected String currentLine;

    protected String trim;

    protected CommentReader(YamlConfigurationOptions options, Reader reader) {
        super(options);
        Validate.notNull(reader, "Reader is null!");
        this.reader = new BufferedReader(reader);
    }

    protected boolean isBlank() {
        return trim.isEmpty();
    }

    protected boolean isComment() {
        return trim.startsWith("#");
    }

    protected boolean nextLine() throws IOException {
        currentLine = reader.readLine();
        if (currentLine != null) {
            trim = currentLine.trim();
            return true;
        }
        return false;
    }

    protected KeyTree.Node track() {
        int indent = 0;
        String key = null;
        if (currentLine != null) {
            MatchResult groups = match(currentLine);
            indent = groups.group(1).length();
            if (groups.groupCount() > 1) {
                key = groups.group(2);
            }
        }
        KeyTree.Node parent = keyTree.findParent(indent);
        return parent.add(indent, key);
    }

    private MatchResult match(String s) {
        Matcher matcher = KEY_REGEX.matcher(s); // for comments of section keys
        if (matcher.matches()) {
            return matcher.toMatchResult();
        }
        matcher = ELEMENT_REGEX.matcher(s); // for comments of section values
        if (matcher.matches()) {
            return matcher.toMatchResult();
        }
        matcher = OTHER_REGEX.matcher(s); // for anything else
        if (matcher.matches()) {
            return matcher.toMatchResult();
        }
        throw new IllegalStateException(s + " cannot be matched");
    }
}

package org.simpleyaml.configuration.comments;

import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommentDumper extends CommentReader {

    private final CommentMapper commentMapper;

    private StringBuilder builder;

    public CommentDumper(YamlConfigurationOptions options, CommentMapper commentMapper, Reader reader) {
        super(options, reader);
        this.commentMapper = commentMapper;
    }

    /**
     * Merge comments from the comment mapper with lines from the reader.
     *
     * @return the resulting String
     * @throws IOException if any problem while reading arise
     */
    public String dump() throws IOException {
        if (commentMapper == null) {
            return reader.lines().collect(Collectors.joining("\n"));
        }

        builder = new StringBuilder();

        while (nextLine()) {
            if (!isComment()) { // Avoid duplicating header
                String path = track().getPath();
                KeyTree.Node node = getNode(path);
                append(node, KeyTree.Node::getComment);
                builder.append(currentLine);
                append(node, KeyTree.Node::getSideComment);
                builder.append('\n');
            }
        }

        // Append end of file comment (null path), if found
        append(getNode(null), KeyTree.Node::getComment);

        reader.close();

        return builder.toString();
    }

    private void append(KeyTree.Node node, Function<KeyTree.Node, String> getter) {
        if (node != null) {
            String s = getter.apply(node);
            if (s != null) {
                builder.append(s);
            }
        }
    }

    @Override
    protected KeyTree.Node getNode(String path) {
        return commentMapper.getNode(path);
    }

}

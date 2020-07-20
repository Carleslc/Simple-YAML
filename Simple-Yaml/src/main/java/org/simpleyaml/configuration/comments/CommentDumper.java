package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;

public class CommentDumper extends CommentReader {

    private final CommentMapper commentMapper;

    private StringBuilder builder;

    public CommentDumper(final YamlConfigurationOptions options, final CommentMapper commentMapper, final Reader reader) {
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
        if (this.commentMapper == null) {
            return this.reader.lines().collect(Collectors.joining("\n"));
        }

        this.builder = new StringBuilder();

        while (this.nextLine()) {
            if (!this.isComment()) { // Avoid duplicating header
                final String path = this.track().getPath();
                final KeyTree.Node node = this.getNode(path);
                this.append(node, KeyTree.Node::getComment);
                this.builder.append(this.currentLine);
                this.append(node, KeyTree.Node::getSideComment);
                this.builder.append('\n');
            }
        }

        // Append end of file comment (null path), if found
        this.append(this.getNode(null), KeyTree.Node::getComment);

        this.reader.close();

        return this.builder.toString();
    }

    @Override
    protected KeyTree.Node getNode(final String path) {
        return this.commentMapper.getNode(path);
    }

    private void append(final KeyTree.Node node, final Function<KeyTree.Node, String> getter) {
        if (node != null) {
            final String s = getter.apply(node);
            if (s != null) {
                this.builder.append(s);
            }
        }
    }

}

package org.simpleyaml.configuration.comments;

import org.simpleyaml.utils.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.stream.Collectors;

public class YamlCommentDumper extends YamlCommentReader {

    protected final YamlCommentMapper yamlCommentMapper;

    protected StringBuilder builder;
    protected StringBuilder explicitBlock;

    protected KeyTree.Node firstListMapElement;

    public YamlCommentDumper(final YamlCommentMapper yamlCommentMapper, final Reader reader) {
        super(yamlCommentMapper.options(), reader);
        this.yamlCommentMapper = yamlCommentMapper;
    }

    /**
     * Merge comments from the comment mapper with lines from the reader.
     *
     * @return the resulting String
     * @throws IOException if any problem while reading arise
     */
    public String dump() throws IOException {
        String result;

        if (this.yamlCommentMapper == null) {
            result = this.reader.lines().collect(Collectors.joining("\n"));
        } else {
            this.builder = new StringBuilder();

            while (this.nextLine()) {
                this.processLine();
                this.builder.append('\n');
            }

            // Append end of file (footer) comment (null path), if found
            this.appendBlockComment(this.getNode(null));

            result = this.builder.toString();
        }

        this.close();

        return result;
    }

    @Override
    protected void processLine() throws IOException {
        final KeyTree.Node readerNode = this.track();
        final KeyTree.Node commentNode = getCommentNode(readerNode);
        this.appendBlockComment(commentNode);
        this.builder.append(this.currentLine);
        this.appendSideComment(commentNode);
    }

    public KeyTree.Node getCommentNode(final KeyTree.Node readerNode) {
        this.firstListMapElement = null;
        KeyTree.Node commentNode = null;
        if (readerNode != null) {
            commentNode = this.getNode(readerNode.getPath());

            if (commentNode != null) {
                final KeyTree.Node parent = commentNode.getParent();
                if (parent != null && parent.isList() && commentNode.size() == 1) {
                    this.checkFirstListMapElement(commentNode, readerNode);
                }
            } else if (readerNode.getName() != null) {
                commentNode = this.getNode(readerNode.getPathWithName());
            }
        }
        return commentNode;
    }

    protected void checkFirstListMapElement(final KeyTree.Node commentNode, final KeyTree.Node readerNode) {
        // First list map element block and side comment can be either the list[0] or first list[0].element node
        final KeyTree.Node child = commentNode.getFirst();
        final Integer elementIndex = child.getElementIndex();
        if (elementIndex == null) {
            final String childName = child.getName();
            if (childName != null && childName.equals(readerNode.getName())) {
                this.firstListMapElement = child;
            }
        } else if (elementIndex == 0) {
            this.firstListMapElement = child;
        }
    }

    @Override
    public KeyTree.Node getNode(final String path) {
        return this.yamlCommentMapper.getNode(path);
    }

    protected void appendBlockComment(final KeyTree.Node node) {
        if (node != null) {
            this.appendBlockComment(node.getComment());

            if (this.firstListMapElement != null) {
                this.appendBlockComment(this.firstListMapElement.getComment());
            }
        }

        if (this.explicitBlock != null) {
            this.builder.append(this.explicitBlock);
            this.builder.append('\n');
            this.explicitBlock = null;
        }
    }

    protected void appendBlockComment(final String comment) {
        if (comment != null) {
            this.builder.append(comment);

            if (!comment.endsWith("\n")) {
                this.builder.append('\n');
            }
        }
    }

    protected void appendSideComment(final KeyTree.Node node) throws IOException {
        String sideComment = null;

        if (node != null) {
            sideComment = node.getSideComment();

            if (sideComment == null && this.firstListMapElement != null) {
                sideComment = this.firstListMapElement.getSideComment(); // comment on first list map element
            }
        }

        this.readValue();

        if (sideComment != null && !sideComment.isEmpty()) {
            if (this.isLiteral) {
                this.appendSideCommentLiteral(sideComment);
            } else {
                this.builder.append(sideComment);
            }
        }
    }

    protected void appendSideCommentLiteral(final String sideComment) throws IOException {
        final String[] sideCommentParts = StringUtils.splitNewLines(sideComment, 2);

        // Append first side comment line
        this.builder.append(sideCommentParts[0]);

        // Read multiline block literal
        if (sideCommentParts.length > 1 && this.nextLine()) {
            this.builder.append('\n');
            this.builder.append(this.currentLine);

            while (this.nextLine() && this.isLiteral) { // is still literal after reading next line
                this.builder.append('\n').append(this.currentLine);
            }

            // Append side comment below
            this.builder.append('\n').append(sideCommentParts[1]);

            if (this.stage != ReaderStage.END_OF_FILE) {
                // Track last line read that is not literal
                this.builder.append('\n');
                this.processLine();
            }
        }
    }

    @Override
    protected void readValue() throws IOException {
        if (this.hasChar()) {
            this.stage = ReaderStage.VALUE;

            if (this.isInQuote() && !this.isLiteral) {
                // Could be a multi line value
                this.skipMultiline();
            } else {
                // Skip to the end of the line (there are no comments in the dump source)
                this.skipToEnd();
            }
        }
    }

    protected void skipMultiline() throws IOException {
        boolean hasChar = this.hasChar() && this.nextChar();

        while (hasChar) {
            hasChar = this.nextChar();
        }

        if (this.isMultiline()) {
            this.readValueMultiline();
        }
    }

    @Override
    protected void processMultiline(boolean inQuoteBlock) {
        StringBuilder builder;
        if (this.isExplicit()) {
            if (this.explicitBlock == null) {
                this.explicitBlock = new StringBuilder();
            }
            builder = this.explicitBlock;
        } else {
            builder = this.builder;
        }
        if (this.isLiteral && this.quoteNotation != ReadingQuoteStyle.LITERAL) {
            builder.append(this.currentLine); // ? | # comment
        } else {
            builder.append('\n');

            if (inQuoteBlock) {
                builder.append(this.currentLine);
            }
        }
    }

}

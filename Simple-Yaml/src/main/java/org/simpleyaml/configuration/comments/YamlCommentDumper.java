package org.simpleyaml.configuration.comments;

import org.simpleyaml.utils.DumperBus;
import org.simpleyaml.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class YamlCommentDumper extends YamlCommentReader {

    protected final YamlCommentMapper yamlCommentMapper;

    protected final DumperBus bus;
    protected BufferedWriter writer;
    protected StringWriter explicitBlock;

    protected KeyTree.Node firstListMapElement;

    public YamlCommentDumper(final YamlCommentMapper yamlCommentMapper, final DumperBus.Dumper source, final Writer writer) {
        super(yamlCommentMapper.options());
        this.yamlCommentMapper = yamlCommentMapper;
        this.writer = writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
        this.bus = new DumperBus(source);
    }

    /**
     * Merge comments from the comment mapper with lines from the source.
     * <p>The result is written to the writer.</p>
     *
     * @throws IOException if any problem arise while reading or writing
     */
    public void dump() throws IOException {
        this.bus.dump();

        while (this.nextLine()) {
            this.processLine();
            this.writer.newLine();
        }

        // Append end of file (footer) comment (null path), if found
        this.clearSection();
        this.appendBlockComment(this.getNode(null));

        this.close();
    }

    @Override
    protected String readLine() throws IOException {
        return this.bus.await();
    }

    @Override
    protected void processLine() throws IOException {
        this.clearSection();
        final KeyTree.Node readerNode = this.track();
        final KeyTree.Node commentNode = this.getCommentNode(readerNode);
        this.appendBlockComment(commentNode);
        this.writer.write(this.currentLine);
        this.appendSideComment(commentNode);
    }

    protected void clearSection() {
        if (this.isSectionEnd()) {
            this.clearCurrentNode();
        }
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

    protected void appendBlockComment(final KeyTree.Node node) throws IOException {
        if (node != null) {
            this.appendBlockComment(node.getComment());

            if (this.firstListMapElement != null) {
                this.appendBlockComment(this.firstListMapElement.getComment());
            }
        }

        if (this.explicitBlock != null) {
            this.writer.write(this.explicitBlock.toString());
            this.writer.newLine();
            this.explicitBlock = null;
        }
    }

    protected void appendBlockComment(final String comment) throws IOException {
        if (comment != null) {
            this.writer.write(comment);

            if (!comment.endsWith("\n")) {
                this.writer.newLine();
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
                this.writer.write(sideComment);
            }
        }
    }

    protected void appendSideCommentLiteral(final String sideComment) throws IOException {
        final String[] sideCommentParts = StringUtils.splitNewLines(sideComment, 2);

        // Append first side comment line
        this.writer.write(sideCommentParts[0]);

        // Read multiline block literal
        if (sideCommentParts.length > 1 && this.nextLine()) {
            this.writer.newLine();
            this.writer.write(this.currentLine);

            while (this.nextLine() && this.isLiteral) { // is still literal after reading next line
                this.writer.newLine();
                this.writer.write(this.currentLine);
            }

            // Append side comment below
            this.writer.newLine();
            this.writer.write(sideCommentParts[1]);

            if (this.stage != ReaderStage.END_OF_FILE) {
                // Track last line read that is not literal
                this.writer.newLine();
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
    protected void processMultiline(boolean inQuoteBlock) throws IOException {
        Writer writer;
        if (this.isExplicit()) {
            if (this.explicitBlock == null) {
                this.explicitBlock = new StringWriter();
            }
            writer = this.explicitBlock;
        } else {
            writer = this.writer;
        }
        if (this.isLiteral && this.quoteNotation != ReadingQuoteStyle.LITERAL) {
            writer.write(this.currentLine); // ? | # comment
        } else {
            writer.write('\n');

            if (inQuoteBlock) {
                writer.write(this.currentLine);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }
}

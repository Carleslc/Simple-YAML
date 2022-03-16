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

    protected KeyTree.Node commentNode, commentNodeFallback, firstListMapElement;

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
        this.commentNode = this.getNode(null);
        this.appendBlockComment();

        this.close();
    }

    @Override
    protected String readLine() throws IOException {
        return this.bus.await();
    }

    @Override
    protected void processLine() throws IOException {
        this.clearSection();
        this.getCommentNode(this.track());
        this.appendBlockComment();
        this.writer.write(this.currentLine);
        this.appendSideComment();
    }

    protected void clearSection() {
        this.commentNode = this.commentNodeFallback = this.firstListMapElement = null;
        if (this.isSectionEnd()) {
            this.clearCurrentNode();
        }
    }

    public void getCommentNode(final KeyTree.Node readerNode) {
        if (readerNode != null) {
            this.commentNode = this.getNode(readerNode.getPath()); // key or list element by index

            if (this.commentNode != null) {
                if (this.commentNode.parent != null && this.commentNode.parent.isList && this.commentNode.size() == 1) {
                    this.checkFirstListMapElement(this.commentNode, readerNode); // first key for list maps
                }
            }

            if (this.commentNode == null ||
                    (readerNode.name != null && (this.commentNode.comment == null || this.commentNode.sideComment == null))) {
                if (readerNode.parent != null && readerNode.parent.isList && readerNode.elementIndex != null) {
                    this.commentNodeFallback = this.getNode(readerNode.getPathWithName()); // list element by name
                }
            }
        }
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
        return this.yamlCommentMapper.getPriorityNode(path);
    }

    protected void appendBlockComment() throws IOException {
        String blockComment = null;

        if (this.commentNode != null) {
            blockComment = this.commentNode.getComment();
        }

        if (blockComment == null && this.commentNodeFallback != null) {
            blockComment = this.commentNodeFallback.getComment();
        }

        this.appendBlockComment(blockComment);

        if (this.firstListMapElement != null) {
            this.appendBlockComment(this.firstListMapElement.getComment());
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

    protected void appendSideComment() throws IOException {
        String sideComment = null;

        if (this.commentNode != null) {
            sideComment = this.commentNode.getSideComment();
        }

        if (sideComment == null && this.firstListMapElement != null) {
            sideComment = this.firstListMapElement.getSideComment();
        }

        if (sideComment == null && this.commentNodeFallback != null) {
            sideComment = this.commentNodeFallback.getSideComment();
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
            writer.write(System.lineSeparator());

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

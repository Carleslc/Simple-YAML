package org.simpleyaml.configuration.implementation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;
import org.simpleyaml.utils.TestResources;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class YamlImplementationTest {

    @Test
    void defaultImplementation() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test.yml"));

        MatcherAssert.assertThat(
                "Default implementation changed",
                yamlFile.getImplementation(),
                new IsInstanceOf(SnakeYamlImplementation.class)
        );

        final SnakeYamlImplementation implementation = (SnakeYamlImplementation) yamlFile.getImplementation();
        final Yaml yaml = implementation.getYaml();

        implementation.configure(yamlFile.options());

        try (Writer writer = new StringWriter()) { // to print on console, use new PrintWriter(System.out)
            final Iterator<Event> events = yaml.parse(new UnicodeReader(new FileInputStream(yamlFile.getConfigurationFile()))).iterator();
            final Emitter emitter = new Emitter(writer, implementation.getDumperOptions());
            while (events.hasNext()) {
                emitter.emit(events.next());
            }

            MatcherAssert.assertThat(
                    "Wrong implementation parse!",
                    writer.toString(),
                    new IsEqual<>(TestResources.testContent())
            );
        }

        final Map<String, Object> values = implementation.load(yamlFile.fileToString());

        MatcherAssert.assertThat(
                "Wrong implementation load/dump!",
                implementation.dump(values, yamlFile.options()),
                new IsEqual<>(TestResources.testContent())
        );

        try (Writer writer = new StringWriter()) {
            yaml.serialize(implementation.getRepresenter().represent(values), writer);

            MatcherAssert.assertThat(
                    "Wrong implementation serialize!",
                    writer.toString(),
                    new IsEqual<>(TestResources.testContent())
            );
        }
    }

    @Test
    void customImplementationDefaultScalarStyle() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final YamlImplementation customYamlImplementationSingle = new SnakeYamlImplementation() {
            @Override
            public void configure(final YamlConfigurationOptions options) {
                super.configure(options);
                getRepresenter().setDefaultScalarStyle(DumperOptions.ScalarStyle.SINGLE_QUOTED);
            }
        };

        yamlFile.setImplementation(customYamlImplementationSingle);

        MatcherAssert.assertThat(
                "Wrong custom implementation output!",
                yamlFile.saveToString(),
                new IsEqual<>(TestResources.testCommentsSingle())
        );

        final YamlImplementation yamlImplementationLiteral = new SnakeYamlImplementation() {
            @Override
            public void configure(final YamlConfigurationOptions options) {
                super.configure(options);
                getRepresenter().setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
            }
        };

        yamlFile.setImplementation(yamlImplementationLiteral);

        final String literalOutput = yamlFile.saveToString();

        MatcherAssert.assertThat(
                "Wrong custom implementation output!",
                literalOutput,
                new IsEqual<>(TestResources.testCommentsLiteral())
        );

        yamlFile.loadFromString(literalOutput);

        MatcherAssert.assertThat(
                "Wrong custom implementation parse!",
                yamlFile.saveToString(),
                new IsEqual<>(literalOutput)
        );

        final YamlImplementation yamlImplementationFolded = new SnakeYamlImplementation() {
            @Override
            public void configure(final YamlConfigurationOptions options) {
                super.configure(options);
                getRepresenter().setDefaultScalarStyle(DumperOptions.ScalarStyle.FOLDED);
            }
        };

        yamlFile.setImplementation(yamlImplementationFolded);

        final String foldedOutput = yamlFile.saveToString();

        MatcherAssert.assertThat(
                "Wrong custom implementation output!",
                foldedOutput,
                new IsEqual<>(TestResources.testCommentsFolded())
        );

        yamlFile.loadFromString(foldedOutput);

        MatcherAssert.assertThat(
                "Wrong custom implementation parse!",
                yamlFile.saveToString(),
                new IsEqual<>(foldedOutput)
        );
    }

    /**
     <code>snakeyaml</code> can now process comments since version 1.29
     <p></p>This test shows how to use that with Simple-YAML.
     <p></p>The output though is not exactly the same as current Simple-YAML comment parser/dumper,
     check {@link #testCommentsSnakeYaml} method below for some example differences.
     <p></p>Setting and getting comments using {@link YamlFile#setComment(String, String, CommentType)}
     or {@link YamlFile#getComment(String, CommentType)} will not modify the snakeyaml {@link org.yaml.snakeyaml.Yaml} comments,
     so you will have to use the implementation snakeyaml methods like {@link org.yaml.snakeyaml.nodes.Node#setBlockComments(List)}
     and {@link org.yaml.snakeyaml.nodes.Node#getBlockComments()}.
    */
    /*
     TODO maybe we can provide this implementation as an alternative (or as default) in the public API
     we would need to:
     - use KeyTree or similar interface decoupled from snakeyaml in YamlImplementation load/dump instead Map<String, Object>
     - add useComments(bool) to YamlConfigurationOptions & remove useComments from YamlFile (set/get from options)
     - add setProcessComments to loader and dumper options in SnakeYamlImplementation configure
     - use yaml.compose to load the Node instead yaml.load
     - use yaml.serialize(node, StringWriter) to dump on saveToString instead yaml.dump
     - use yaml.serialize(node, FileWriter) to dump on save
     - track snakeyaml Node in KeyTree/YamlCommentMapper
     - add comments from KeyTree/YamlCommentMapper to snakeyaml Node setBlockComments/setInLineComments/setEndComments
     - remove header from first snakeyaml Node blockComments
     - modify YamlCommentReader/Parser/Dumper accordingly
    */
    @Test
    void processComments() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));

        // Get snakeyaml implementation Yaml
        final SnakeYamlImplementation implementation = (SnakeYamlImplementation) yamlFile.getImplementation();
        final Yaml yaml = implementation.getYaml();

        // Apply default Simple-YAML configuration
        implementation.configure(yamlFile.options());

        // Set snakeyaml to process comments
        implementation.getLoaderOptions().setProcessComments(true);
        implementation.getDumperOptions().setProcessComments(true);

        MatcherAssert.assertThat(
                implementation.getDumperOptions().isProcessComments() &&
                        implementation.getLoaderOptions().isProcessComments(),
                new IsTrue()
        );

        // Load file using snakeyaml
        final MappingNode node = (MappingNode) yaml.compose(new InputStreamReader(
                new FileInputStream(yamlFile.getConfigurationFile()), yamlFile.options().charset()));

        // Dump values
        try (Writer writer = new StringWriter()) {
            yaml.serialize(node, writer);

            MatcherAssert.assertThat(
                    writer.toString(),
                    new IsEqual<>(testCommentsSnakeYaml())
            );
        }
    }

    public static String testCommentsSnakeYaml() {
        return TestResources.testHeader() +
                "# Test comments\n" +
                "test:\n" +
                "  number: 5\n" +
                "  # Hello!\n" +
                "  string: Hello world\n" +
                "  boolean: true\n" +
                "  # List of words\n" +
                "  list:\n" +
                "    - Each\n" +
                "    - word\n" +
                "    - will\n" +
                "    - be\n" +
                "    - in\n" +
                "    - a\n" +
                "    - separated\n" +
                "    - # Comment on a list item\n" + // with the current comment implementation the - is on the next line
                "      entry # :)\n" +
                "  # This is a\n" +
                "  # multiline comment\n" +
                "  wrap: '# this is not a comment'\n" +
                "  \n" + // with the comment current implementation there is no indentation added, because there is no indentation in the original file
                "  blank: ''\n" +
                "\n" +
                "# Wonderful numbers\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                // with the current comment implementation it is a side comment below the math.pi key
                // with snakeyaml instead it is set as part of the block comment of the next key timestamp, removing the indentation in the original file
                "# Side comment below\n" +
                "\n" +
                "# Some timestamps\n" +
                "timestamp:\n" +
                "  # ISO\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  # Date/Time with format\n" +
                "  formattedDate: 04/07/2020 15:18:04 # dd/MM/yyyy HH:mm:ss\n" +
                "\n" +
                "# End\n";
        // with test-comments4 it keeps current key/value quote styles
        // but there is some indentation removed or added that seems buggy whereas with the current comment implementation works as expected
    }
}

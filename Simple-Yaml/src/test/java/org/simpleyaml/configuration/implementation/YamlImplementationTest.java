package org.simpleyaml.configuration.implementation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;
import org.simpleyaml.utils.TestResources;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

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

        implementation.load(yamlFile.fileToString(), yamlFile);

        MatcherAssert.assertThat(
                "Wrong implementation load/dump!",
                implementation.dump(yamlFile),
                new IsEqual<>(TestResources.testContent())
        );

        try (Writer writer = new StringWriter()) {
            yaml.serialize(implementation.getRepresenter().represent(yamlFile), writer);

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

        final YamlImplementation customYamlImplementationSingle = new SimpleYamlImplementation() {
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

        final YamlImplementation yamlImplementationLiteral = new SimpleYamlImplementation() {
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

        final YamlImplementation yamlImplementationFolded = new SimpleYamlImplementation() {
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
     <p/>This test shows how to use that with Simple-YAML, using {@link SnakeYamlImplementation}.
     <p/>The output though is not exactly the same as {@link SimpleYamlImplementation} comment parser/dumper,
     check {@link #testCommentsSnakeYaml} method below for some example differences.
    */
    @Test
    void processCommentsWithSnakeYaml() throws Exception {
        // Create the YamlFile using the SnakeYaml implementation
        final YamlFile yamlFile = new YamlFile(new SnakeYamlImplementation());

        // Set the file to load
        yamlFile.setConfigurationFile(TestResources.getResourceURI("test-comments.yml"));

        /* Option 1: Using loadWithComments / save */

        // Load file with comments
        yamlFile.loadWithComments();

        // Dump values
        MatcherAssert.assertThat(
                yamlFile.saveToString(),
                new IsEqual<>(testCommentsSnakeYaml())
        );

        /* Option 2: Using the snakeyaml low-level API directly */

        // Get snakeyaml Yaml
        final SnakeYamlImplementation implementation = (SnakeYamlImplementation) yamlFile.getImplementation();
        final Yaml yaml = implementation.getYaml();

        // Set snakeyaml to process comments
        yamlFile.options().useComments(true);
        implementation.configure(yamlFile.options());

        MatcherAssert.assertThat(
                implementation.getDumperOptions().isProcessComments() &&
                        implementation.getLoaderOptions().isProcessComments(),
                new IsTrue()
        );

        // Load file using snakeyaml
        final Node node = yaml.compose(new InputStreamReader(
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
        // Compare this output with the original file test-comments.yml
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
                "    - # Comment on a list item\n" + // with SimpleYamlImplementation the - is on the next line
                "      entry # :)\n" +
                "  # This is a\n" +
                "  # multiline comment\n" +
                "  wrap: '# this is not a comment'\n" +
                "  \n" + // with SimpleYamlImplementation there is no indentation added, because there is no indentation in the original file
                "  blank: ''\n" +
                "\n" +
                "# Wonderful numbers\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                // with SimpleYamlImplementation this is considered a side comment below the math.pi key (it is at the same level of indentation in the original file)
                // with SnakeYamlImplementation instead it is considered as part of the block comment of the next key timestamp, removing the indentation in the original file
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
        // Differences in other test files:
        // [test-comments2.yml] side comments extra indentation is removed
        // [test-comments3.yml] additional indentation is added to some comments
        // [test-comments4.yml] explicit style is not supported by default
    }
}

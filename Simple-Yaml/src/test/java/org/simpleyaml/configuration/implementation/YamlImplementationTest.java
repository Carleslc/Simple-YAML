package org.simpleyaml.configuration.implementation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.YamlImplementation;
import org.simpleyaml.obj.TestResources;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
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

        Writer writer = new StringWriter(); // to print on console, use new PrintWriter(System.out)

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

        final Map<String, Object> values = implementation.load(yamlFile.fileToString());

        MatcherAssert.assertThat(
                "Wrong implementation load/dump!",
                implementation.dump(values, yamlFile.options()),
                new IsEqual<>(TestResources.testContent())
        );

        writer = new StringWriter();
        yaml.serialize(implementation.getRepresenter().represent(values), writer);

        MatcherAssert.assertThat(
                "Wrong implementation serialize!",
                writer.toString(),
                new IsEqual<>(TestResources.testContent())
        );
    }

    @Test
    void customImplementation() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final YamlImplementation customYamlImplementation = new SnakeYamlImplementation() {
            @Override
            public void configure(final YamlConfigurationOptions options) {
                super.configure(options);
                getRepresenter().setDefaultScalarStyle(DumperOptions.ScalarStyle.SINGLE_QUOTED);
            }
        };

        yamlFile.setImplementation(customYamlImplementation);

        MatcherAssert.assertThat(
                "Wrong custom implementation output!",
                yamlFile.saveToString(),
                new IsEqual<>(TestResources.testCommentsSingle())
        );
    }
}

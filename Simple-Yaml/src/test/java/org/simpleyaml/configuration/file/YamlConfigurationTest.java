package org.simpleyaml.configuration.file;

import org.cactoos.io.InputStreamOf;
import org.cactoos.io.ResourceOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.HasValues;

class YamlConfigurationTest {

    @Test
    void loadConfiguration() {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);

        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getInt("test.number"),
            new IsEqual<>(5)
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getString("test.string"),
            new IsEqual<>("Hello world")
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getBoolean("test.boolean"),
            new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getStringList("test.list"),
            new HasValues<>(
                "Each", "word", "will", "be", "in",
                "a", "separated", "entry"
            )
        );
    }

    @Test
    void saveToString() {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);
        final String content = "test:\n" +
            "  number: 5\n" +
            "  string: Hello world\n" +
            "  boolean: true\n" +
            "  list:\n" +
            "  - Each\n" +
            "  - word\n" +
            "  - will\n" +
            "  - be\n" +
            "  - in\n" +
            "  - a\n" +
            "  - separated\n" +
            "  - entry\n" +
            "math:\n" +
            "  pi: 3.141592653589793\n" +
            "timestamp:\n" +
            "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
            "  formattedDate: 04/07/2020 15:18:04\n";

        MatcherAssert.assertThat(
            "Couldn't get the content of the file!",
            configuration.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void loadFromString() {
    }

    @Test
    void buildHeader() {
    }

    @Test
    void options() {
    }

    @Test
    void convertMapsToSections() {
    }

    @Test
    void parseHeader() {
    }

}
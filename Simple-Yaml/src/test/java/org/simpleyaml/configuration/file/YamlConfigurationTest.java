package org.simpleyaml.configuration.file;

import java.util.regex.Matcher;
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
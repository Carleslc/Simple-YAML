package org.simpleyaml.configuration.file;

import java.util.HashMap;
import java.util.Map;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.ResourceOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.HasValues;
import org.llorllale.cactoos.matchers.IsBlank;
import org.simpleyaml.configuration.MemoryConfiguration;
import org.simpleyaml.exceptions.InvalidConfigurationException;

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
            new IsEqual<>(content));
    }

    @Test
    void loadFromString() throws InvalidConfigurationException {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);
        final String newcontent = "test:\n" +
            "  number: 10\n" +
            "  string: Hello world!\n" +
            "  boolean: false\n" +
            "  list:\n" +
            "  - Eachs\n" +
            "  - words\n" +
            "  - wills\n" +
            "  - bes\n" +
            "  - ins\n" +
            "  - as\n" +
            "  - separateds\n" +
            "  - entrys\n" +
            "math:\n" +
            "  pi: 3.141592653589793\n" +
            "timestamp:\n" +
            "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
            "  formattedDate: 04/07/2020 15:18:04\n";
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
        configuration.loadFromString(newcontent);
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getInt("test.number"),
            new IsEqual<>(10)
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getString("test.string"),
            new IsEqual<>("Hello world!")
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getBoolean("test.boolean"),
            new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
            "Couldn't load the input stream!",
            configuration.getStringList("test.list"),
            new HasValues<>(
                "Eachs", "words", "wills", "bes", "ins",
                "as", "separateds", "entrys"
            )
        );
    }

    @Test
    void buildHeader() {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);

        MatcherAssert.assertThat(
            "Couldn't build the header!",
            configuration.buildHeader(),
            new IsBlank()
        );
    }

    @Test
    void options() {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);

        MatcherAssert.assertThat(
            "Couldn't create yaml options!",
            configuration.options(),
            new IsInstanceOf(YamlConfigurationOptions.class)
        );
    }

    @Test
    void convertMapsToSections() {
        final InputStreamOf stream = new InputStreamOf(
            new ResourceOf("test.yml"));
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(stream);
        final Map<String, Object> map = new HashMap<>();
        map.put("test", "hello");
        map.put("test-2", false);
        map.put("test-3", 123);
        final MemoryConfiguration section = new MemoryConfiguration();
        configuration.convertMapsToSections(map, section);

        MatcherAssert.assertThat(
            "Couldn't load section from the map!",
            section.getString("test"),
            new IsEqual<>("hello")
        );
        MatcherAssert.assertThat(
            "Couldn't load section from the map!",
            section.getBoolean("test-2"),
            new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
            "Couldn't load section from the map!",
            section.getInt("test-3"),
            new IsEqual<>(123)
        );
    }

    @Test
    void parseHeader() {
        final String content = "#test123\n" +
            "test:\n" +
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
            "Couldn't parse the header of the content!",
            YamlConfiguration.parseHeader(content),
            new IsEqual<>("test123")
        );
    }

}

package org.simpleyaml.configuration.file;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.HasValues;
import org.llorllale.cactoos.matchers.IsBlank;
import org.simpleyaml.configuration.MemoryConfiguration;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.utils.SectionUtils;
import org.simpleyaml.utils.TestResources;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class YamlConfigurationTest {

    @Test
    void loadConfiguration() throws IOException {
        final YamlConfiguration configuration = resourceLoadYamlConfiguration("test.yml");

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
    void saveToString() throws IOException {
        final YamlConfiguration configuration = resourceLoadYamlConfiguration("test.yml");
        final String content = TestResources.testContent();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file!",
            configuration.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void primitiveGetters() throws IOException {
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString("b: 5\ns: 300\nf: 1.5\nc: A\nn: 65\n");

        // Values loaded from a YAML file are parsed as Integer, Long, Double or String,
        // so the getters convert them to the requested primitive type
        MatcherAssert.assertThat("Wrong byte!", configuration.getByte("b"), new IsEqual<>((byte) 5));
        MatcherAssert.assertThat("Wrong short!", configuration.getShort("s"), new IsEqual<>((short) 300));
        MatcherAssert.assertThat("Wrong float!", configuration.getFloat("f"), new IsEqual<>(1.5f));
        MatcherAssert.assertThat("Wrong char!", configuration.getCharacter("c"), new IsEqual<>('A'));
        MatcherAssert.assertThat("Wrong char from code point!", configuration.getCharacter("n"), new IsEqual<>('A'));
        MatcherAssert.assertThat("Wrong byte default!", configuration.getByte("missing", (byte) 7), new IsEqual<>((byte) 7));

        // is* checks are false for loaded values because they are not parsed with the exact primitive wrapper type
        MatcherAssert.assertThat("Loaded value is not a Byte!", configuration.isByte("b"), new IsEqual<>(false));
        MatcherAssert.assertThat("Loaded value is not a Short!", configuration.isShort("s"), new IsEqual<>(false));
        MatcherAssert.assertThat("Loaded value is not a Float!", configuration.isFloat("f"), new IsEqual<>(false));
        MatcherAssert.assertThat("Loaded value is not a Character!", configuration.isCharacter("c"), new IsEqual<>(false));

        // is* checks are true for values set programmatically with the exact primitive wrapper type
        configuration.set("mb", (byte) 5);
        configuration.set("ms", (short) 300);
        configuration.set("mf", 1.5f);
        configuration.set("mc", 'A');
        MatcherAssert.assertThat("Set value is a Byte!", configuration.isByte("mb"), new IsEqual<>(true));
        MatcherAssert.assertThat("Set value is a Short!", configuration.isShort("ms"), new IsEqual<>(true));
        MatcherAssert.assertThat("Set value is a Float!", configuration.isFloat("mf"), new IsEqual<>(true));
        MatcherAssert.assertThat("Set value is a Character!", configuration.isCharacter("mc"), new IsEqual<>(true));
    }

    @Test
    void loadFromString() throws IOException {
        final YamlConfiguration configuration = resourceLoadYamlConfiguration("test.yml");
        final String newContent = "test:\n" +
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
        configuration.loadFromString(newContent);
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
    void buildHeader() throws IOException {
        final YamlConfiguration configuration = resourceLoadYamlConfiguration("test.yml");

        MatcherAssert.assertThat(
            "Wrong header!",
            configuration.buildHeader(),
            new IsBlank()
        );

        configuration.load(() -> TestResources.getResourceInputStream("test-comments.yml"));

        MatcherAssert.assertThat(
            "Couldn't build the header!",
            configuration.buildHeader(),
            new IsEqual<>(TestResources.testHeader())
        );

        MatcherAssert.assertThat(
            "Couldn't build the header!",
            configuration.buildHeader().trim(),
            new IsEqual<>(configuration.options().header())
        );

        final String customPrefix = "#";
        configuration.options().headerFormatter().commentPrefix(customPrefix);
        configuration.options().header("test header");

        MatcherAssert.assertThat(
                "Custom prefix is not correctly applied!",
                configuration.buildHeader(),
                new IsEqual<>(customPrefix + "test header" + "\n\n")
        );

        YamlConfiguration defaults = new YamlConfiguration();
        final String defaultHeader = "default header";
        defaults.options().header(defaultHeader);
        configuration.options().copyDefaults(true);
        configuration.setDefaults(defaults);

        MatcherAssert.assertThat(
                "Header is not default!",
                configuration.buildHeader(),
                new IsEqual<>("# " + defaultHeader + "\n\n")
        );

        configuration.options().copyHeader(false);

        MatcherAssert.assertThat(
                "Header has been copied!",
                configuration.buildHeader(),
                new IsEqual<>("")
        );
    }

    @Test
    void options() throws IOException {
        final YamlConfiguration configuration = resourceLoadYamlConfiguration("test.yml");

        MatcherAssert.assertThat(
            "Couldn't create yaml options!",
            configuration.options(),
            new IsInstanceOf(YamlConfigurationOptions.class)
        );

        final YamlConfigurationOptions defaultOptions = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
            "Options are not default!",
            configuration.options(),
            new IsEqual<>(defaultOptions)
        );
    }

    @Test
    void convertMapsToSections() {
        final Map<String, Object> map = new HashMap<>();
        map.put("test", "hello");
        map.put("test-2", false);
        map.put("test-3", 123);
        final MemoryConfiguration section = new MemoryConfiguration();
        SectionUtils.convertMapsToSections(map, section);

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
    void setQuoteStyle() throws IOException {
        final YamlConfiguration configuration = new YamlConfiguration();

        configuration.set("test", "test", QuoteStyle.PLAIN);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: test\n"));

        configuration.set("test", "test", QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: \"test\"\n"));

        configuration.set("test", "te\\s\"t", QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: \"te\\\\s\\\"t\"\n"));

        configuration.set("test", "te'\\st", QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: 'te''\\st'\n"));

        configuration.set("test", "test", QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: 'test'\n"));

        configuration.set("test", "test", null);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: test\n"));

        configuration.set("test", 1, QuoteStyle.PLAIN);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: 1\n"));

        configuration.set("test", 1, QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!int '1'\n"));

        configuration.set("test", 1, QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!int \"1\"\n"));

        YamlConfiguration newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.getInt("test"),
                new IsEqual<>(1));

        //noinspection UnnecessaryBoxing
        configuration.set("test", Integer.valueOf(1), QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!int \"1\"\n"));

        configuration.set("test", BigInteger.valueOf(1), QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!int \"1\"\n"));

        configuration.set("test", null, null);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test:\n"));

        configuration.set("test", null, QuoteStyle.PLAIN);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test:\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.get("test"),
                new IsNull<>());

        configuration.set("test", null, QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!null ''\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.get("test"),
                new IsNull<>());

        configuration.set("test", true, QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test: !!bool 'true'\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.getBoolean("test"),
                new IsEqual<>(true));

        final List<Integer> integerList = Arrays.asList(1, 2, 3);

        configuration.set("test.n", "0", QuoteStyle.DOUBLE);
        configuration.set("test.list", integerList, QuoteStyle.DOUBLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test:\n  n: \"0\"\n  list:\n    - !!int \"1\"\n    - !!int \"2\"\n    - !!int \"3\"\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.getIntegerList("test.list"),
                new IsEqual<>(integerList));

        List<String> stringList = Arrays.asList("1", "2", "3");

        configuration.set("test", stringList, QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test:\n  - '1'\n  - '2'\n  - '3'\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.getStringList("test"),
                new IsEqual<>(stringList));

        final Map<String, Integer> map = new HashMap<String, Integer>() {{ put("a", 1); put("b", 2); }};

        configuration.set("test", map, QuoteStyle.SINGLE);

        MatcherAssert.assertThat(
                "Wrong value!",
                configuration.saveToString(),
                new IsEqual<>("test:\n  'a': !!int '1'\n  'b': !!int '2'\n"));

        newConfig = new YamlConfiguration();
        newConfig.loadFromString(configuration.saveToString());

        MatcherAssert.assertThat(
                "Wrong value!",
                newConfig.getConfigurationSection("test").getValues(false),
                new IsEqual<>(map));
    }

    @SuppressWarnings("SameParameterValue")
    private YamlConfiguration resourceLoadYamlConfiguration(final String file) throws IOException {
        return YamlConfiguration.loadConfiguration(() -> TestResources.getResourceInputStream(file));
    }

}

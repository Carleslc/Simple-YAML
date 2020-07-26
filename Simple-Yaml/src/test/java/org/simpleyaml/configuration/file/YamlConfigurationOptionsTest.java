package org.simpleyaml.configuration.file;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;

import java.nio.charset.StandardCharsets;

class YamlConfigurationOptionsTest {

    @Test
    void configuration() {
    }

    @Test
    void copyDefaults() {
    }

    @Test
    void pathSeparator() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
            "Default path separator is not a dot!",
            options.pathSeparator(),
            new IsEqual<>('.')
        );

        options.pathSeparator('/');

        MatcherAssert.assertThat(
                "Path separator has not changed!",
                options.pathSeparator(),
                new IsEqual<>('/')
        );
    }

    @Test
    void header() {
    }

    @Test
    void copyHeader() {
    }

    @Test
    void indent() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
            "Default indent is not 2!",
            options.indent(),
            new IsEqual<>(2)
        );

        options.indent(4);

        MatcherAssert.assertThat(
            "Indent has not changed!",
            options.indent(),
            new IsEqual<>(4)
        );
    }

    @Test
    void charset() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
                "Default charset is not UTF-8!",
                options.charset(),
                new IsEqual<>(StandardCharsets.UTF_8)
        );

        options.charset(StandardCharsets.US_ASCII);

        MatcherAssert.assertThat(
                "Charset has not changed!",
                options.charset(),
                new IsEqual<>(StandardCharsets.US_ASCII)
        );
    }

    @Test
    void isUnicode() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
                "Default charset is not Unicode!",
                options.isUnicode(),
                new IsTrue()
        );

        options.charset(StandardCharsets.US_ASCII);

        MatcherAssert.assertThat(
                "ASCII must not be Unicode!",
                options.isUnicode(),
                new IsNot<>(new IsTrue())
        );
    }

}

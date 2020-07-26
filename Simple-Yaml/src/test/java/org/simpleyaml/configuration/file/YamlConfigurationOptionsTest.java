package org.simpleyaml.configuration.file;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

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

}

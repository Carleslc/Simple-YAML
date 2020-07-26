package org.simpleyaml.configuration.file;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.MemoryConfiguration;

import java.nio.charset.StandardCharsets;

class FileConfigurationOptionsTest {

    @Test
    void charset() {
        MemoryConfiguration configuration = new MemoryConfiguration();
        FileConfigurationOptions options = new FileConfigurationOptions(configuration);

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
        MemoryConfiguration configuration = new MemoryConfiguration();
        FileConfigurationOptions options = new FileConfigurationOptions(configuration);

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

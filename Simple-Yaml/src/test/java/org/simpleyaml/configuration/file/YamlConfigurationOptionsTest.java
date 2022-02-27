package org.simpleyaml.configuration.file;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.llorllale.cactoos.matchers.Throws;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.YamlCommentFormat;
import org.simpleyaml.configuration.comments.YamlCommentFormatterConfiguration;

import java.nio.charset.StandardCharsets;

class YamlConfigurationOptionsTest {

    @Test
    void configuration() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
            "The configuration is different!",
            options.configuration(),
            new IsEqual<>(configuration)
        );
    }

    @Test
    void copyDefaults() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        options.copyDefaults(true);
        MatcherAssert.assertThat(
            "Couldn't set copy default!",
            options.copyDefaults(),
            new IsTrue()
        );
        options.copyDefaults(false);
        MatcherAssert.assertThat(
            "Couldn't set copy default!",
            options.copyDefaults(),
            new IsNot<>(new IsTrue())
        );
    }

    @Test
    void pathSeparator() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

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
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);
        final String header = "Test header!";
        options.header(header);

        MatcherAssert.assertThat(
            "Couldn't set header!",
            options.header(),
            new IsEqual<>(header)
        );
    }

    @Test
    void copyHeader() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        options.copyHeader(true);
        MatcherAssert.assertThat(
            "Couldn't set copy header!",
            options.copyHeader(),
            new IsTrue()
        );
        options.copyHeader(false);
        MatcherAssert.assertThat(
            "Couldn't set copy header!",
            options.copyHeader(),
            new IsNot<>(new IsTrue())
        );
    }

    @Test
    void indent() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

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
    void indentList() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        MatcherAssert.assertThat(
                "Default indentList is not 2!",
                options.indentList(),
                new IsEqual<>(2)
        );

        options.indentList(0);

        MatcherAssert.assertThat(
                "List indent has not changed!",
                options.indentList(),
                new IsEqual<>(0)
        );
    }

    @Test
    void commentFormatter() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        final YamlCommentFormatterConfiguration blockFormatterConfiguration = options.commentFormatter().formatterConfiguration(CommentType.BLOCK);

        MatcherAssert.assertThat(
                "Default comment prefix is not '# '!",
                blockFormatterConfiguration.prefixFirst(),
                new IsEqual<>("# ")
        );

        blockFormatterConfiguration.prefix("#");

        MatcherAssert.assertThat(
                "Comment prefix has not changed!",
                blockFormatterConfiguration.prefixFirst(),
                new IsEqual<>("#")
        );

        blockFormatterConfiguration.prefix("\n# ");

        MatcherAssert.assertThat(
                "Comment prefix has not changed!",
                blockFormatterConfiguration.prefixFirst(),
                new IsEqual<>("\n# ")
        );

        final YamlCommentFormatterConfiguration sideFormatterConfiguration = options.commentFormatter().formatterConfiguration(CommentType.SIDE);

        MatcherAssert.assertThat(
                "Default side comment prefix is not ' # '!",
                sideFormatterConfiguration.prefixFirst(),
                new IsEqual<>(" # ")
        );

        MatcherAssert.assertThat(
                "Side comment must start with space",
                () -> sideFormatterConfiguration.prefix("#"),
                new Throws<>(IllegalArgumentException.class)
        );

        MatcherAssert.assertThat(
                "Side comment prefix is invalid!",
                sideFormatterConfiguration.prefixFirst(),
                new IsNot<>(new IsEqual<>("#"))
        );

        MatcherAssert.assertThat(
                "Side below comments should not require to start with a space",
                () -> sideFormatterConfiguration.prefix("\n# "),
                new IsNot<>(new Throws<>(IllegalArgumentException.class))
        );

        MatcherAssert.assertThat(
                "Side comment prefix has not changed!",
                sideFormatterConfiguration.prefixFirst(),
                new IsEqual<>("\n# ")
        );

        YamlCommentFormat.reset();
    }

    @Test
    void charset() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

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
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

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

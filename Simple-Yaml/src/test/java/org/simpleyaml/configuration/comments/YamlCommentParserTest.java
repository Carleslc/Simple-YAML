package org.simpleyaml.configuration.comments;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;

import java.io.IOException;
import java.io.StringReader;

final class YamlCommentParserTest {

    @Test
    void parse() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = configuration.options();

        final YamlCommentParser parser = new YamlCommentParser(options, reader);
        parser.parse();

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("te'st"),
                new IsEqual<>("test comment")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section"),
                new IsEqual<>("test-section # comment # character\n  - multiline comment # character")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section", CommentType.SIDE),
                new IsEqual<>("comment # character")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section.test", CommentType.SIDE),
                new IsEqual<>("comment # character")
        );

        options.commentFormatter().stripPrefix(false);

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section"),
                new IsEqual<>("# test-section # comment # character\n#   - multiline comment # character")
        );

        options.commentFormatter().trim(false);

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section"),
                new IsEqual<>("\n# test-section # comment # character\n#   - multiline comment # character ")
        );

        options.commentFormatter().stripPrefix(true);

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section"),
                new IsEqual<>("\ntest-section # comment # character\n  - multiline comment # character ")
        );

        YamlCommentFormat.reset();
    }

    @Test
    void parseTag() throws IOException {
        final StringReader reader = new StringReader("tag: !!comment ' # not a comment'\n");
        final YamlConfiguration configuration = new YamlConfiguration();

        final YamlCommentParser parser = new YamlCommentParser(configuration.options(), reader);
        parser.parse();

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("tag", CommentType.SIDE),
                new IsNull<>()
        );
    }

}

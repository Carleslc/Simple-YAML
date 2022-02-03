package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.StringReader;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

final class YamlCommentParserTest {

    @Test
    void parse() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);

        final YamlCommentParser parser = new YamlCommentParser(options, reader);
        parser.parse();

        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test"),
                new IsEqual<>("test comment")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section"),
                new IsEqual<>("test-section # comment # hashtag")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section", CommentType.SIDE),
                new IsEqual<>("comment # hashtag")
        );
        MatcherAssert.assertThat(
                "Comments are wrong!",
                parser.getComment("test-section.test", CommentType.SIDE),
                new IsEqual<>("comment # hashtag")
        );
        MatcherAssert.assertThat(
                "Parsed value is wrong!",
                parser.getComment("test-section # hashtag.test", CommentType.SIDE),
                new IsEqual<>("test # hashtag")
        );

        final YamlCommentDumper dumper = new YamlCommentDumper(options, parser, new StringReader(YamlCommentReaderTest.COMMENT));

        MatcherAssert.assertThat(
                "Comments are wrong!",
                dumper.dump(),
                new IsEqual<>(YamlCommentReaderTest.COMMENT)
        );
    }

}

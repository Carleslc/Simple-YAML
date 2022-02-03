package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.StringReader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

final class YamlCommentReaderTest {

    static final String COMMENT = "#test comment\n" +
        "test: 'test'\n" +
        "# test-section # comment # hashtag\n" +
        "test-section: # comment # hashtag\n" +
        "  test: 'test # hashtag' # comment # hashtag";

    @Test
    void isBlank() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentReader commentReader = new YamlCommentReader(options, reader);
        commentReader.nextLine();
        final boolean blank = commentReader.isBlank();

        MatcherAssert.assertThat(
            "The text is blank!",
            blank,
            new IsNot<>(new IsTrue())
        );
    }

    @Test
    void isComment() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentReader commentReader = new YamlCommentReader(options, reader);
        commentReader.nextLine();
        final boolean comment = commentReader.isComment();

        MatcherAssert.assertThat(
            "The text is not a comment!",
            comment,
            new IsTrue()
        );
    }

    @Test
    void nextLine() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentReader commentReader = new YamlCommentReader(options, reader);
        final boolean nextLine = commentReader.nextLine();

        MatcherAssert.assertThat(
            "The text has not a next line!",
            nextLine,
            new IsTrue()
        );
    }

    @Test
    void track() throws IOException {
        final StringReader reader = new StringReader(YamlCommentReaderTest.COMMENT);
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentReader commentReader = new YamlCommentReader(options, reader);
        commentReader.nextLine();
        final KeyTree.Node track = commentReader.track();

        MatcherAssert.assertThat(
            "The node has a root!",
            track.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node's name is not correct!",
            track.getName(),
            new IsNull<>()
        );
        MatcherAssert.assertThat(
            "There is a comment!",
            track.getComment(),
            new IsNull<>()
        );
        MatcherAssert.assertThat(
            "There is a side comment!",
            track.getSideComment(),
            new IsNull<>()
        );
    }

}

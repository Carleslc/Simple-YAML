package org.simpleyaml.configuration.comments;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

class CommentMapperTest {

    @Test
    void setComment() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final CommentMapper mapper = new CommentMapper(options);
        final String test_comment = "test comment";
        mapper.setComment("test", test_comment);
        final String test_side_comment = "test side comment";
        mapper.setComment("test", test_side_comment, CommentType.SIDE);
        final String comment = mapper.getComment("test");
        final String sidecomment = mapper.getComment("test", CommentType.SIDE);

        MatcherAssert.assertThat(
            "Comment couldn't set!",
            comment,
            new IsEqual<>(test_comment)
        );
        MatcherAssert.assertThat(
            "Side comment couldn't set!",
            sidecomment,
            new IsEqual<>(test_side_comment)
        );
    }

    @Test
    void getComment() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final CommentMapper mapper = new CommentMapper(options);

        MatcherAssert.assertThat(
            "There is a comment on test path",
            mapper.getComment("test"),
            new IsNull<>()
        );
        mapper.setComment("test", "test_comment");
        MatcherAssert.assertThat(
            "There is a comment on test path",
            mapper.getComment("test"),
            new IsNot<>(new IsNull<>())
        );
    }

    @Test
    void getNode() {
    }

}
package org.simpleyaml.configuration.comments;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

final class YamlCommentMapperTest {

    @Test
    void setComment() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentMapper mapper = new YamlCommentMapper(options);

        final String key = "test";

        final String testComment = "test # comment";
        mapper.setComment(key, testComment);

        final String testSideComment = "test # side comment";
        mapper.setComment(key, testSideComment, CommentType.SIDE);

        final KeyTree.Node node = mapper.getNode(key);

        MatcherAssert.assertThat(
                "There is no node!",
                node,
                new IsNot<>(new IsNull<>())
        );
        MatcherAssert.assertThat(
                "The node name is not test!",
                node.getName(),
                new IsEqual<>(key)
        );
        MatcherAssert.assertThat(
                "Wrong node comment!",
                node.getComment(),
                new IsEqual<>("# " + testComment)
        );
        MatcherAssert.assertThat(
                "Wrong node side comment!",
                node.getSideComment(),
                new IsEqual<>(" # " + testSideComment)
        );

        MatcherAssert.assertThat(
                "Comment couldn't set!",
                mapper.getComment(key),
                new IsEqual<>(testComment)
        );
        MatcherAssert.assertThat(
                "Side comment couldn't set!",
                mapper.getComment(key, CommentType.SIDE),
                new IsEqual<>(testSideComment)
        );
    }

    @Test
    void getComment() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentMapper mapper = new YamlCommentMapper(options);

        MatcherAssert.assertThat(
            "There is a comment on test path",
            mapper.getComment("test"),
            new IsNull<>()
        );

        mapper.setComment("test", "test # comment");

        MatcherAssert.assertThat(
            "Wrong comment",
            mapper.getComment("test"),
            new IsEqual<>("test # comment")
        );
        MatcherAssert.assertThat(
                "Wrong node comment",
                mapper.getNode("test").getComment(),
                new IsEqual<>("# test # comment")
        );
    }

}

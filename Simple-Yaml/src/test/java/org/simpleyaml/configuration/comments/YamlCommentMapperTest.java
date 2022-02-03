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
        final String test_comment = "test comment # hashtag";
        mapper.setComment("test", test_comment);
        final String test_side_comment = "test side comment # hashtag";
        mapper.setComment("test", test_side_comment, CommentType.SIDE);
        final String comment = mapper.getComment("test");
        final String sideComment = mapper.getComment("test", CommentType.SIDE);

        MatcherAssert.assertThat(
            "Comment couldn't set!",
            comment,
            new IsEqual<>(test_comment)
        );
        MatcherAssert.assertThat(
            "Side comment couldn't set!",
            sideComment,
            new IsEqual<>(test_side_comment)
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
        mapper.setComment("test", "test_comment");
        MatcherAssert.assertThat(
            "There is a comment on test path",
            mapper.getComment("test"),
            new IsNot<>(new IsNull<>())
        );
    }

    @Test
    void getNode() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final YamlCommentMapper mapper = new YamlCommentMapper(options);
        final String test_comment = "test comment # hashtag";
        final String test_side_comment = "test side comment # hashtag";
        final String nodeName = "test";
        mapper.setComment(nodeName, test_comment);
        mapper.setComment(nodeName, test_side_comment, CommentType.SIDE);
        final KeyTree.Node node = mapper.getNode(nodeName);

        MatcherAssert.assertThat(
            "There is no node!",
            node,
            new IsNot<>(new IsNull<>())
        );
        MatcherAssert.assertThat(
            "There is no comment on the node!",
            node.getComment(),
            new IsEqual<>("# " + test_comment + '\n')
        );
        MatcherAssert.assertThat(
            "There is no side comment near the node!",
            node.getSideComment(),
            new IsEqual<>(" # " + test_side_comment)
        );
        MatcherAssert.assertThat(
            "The node name is not test!",
            node.getName(),
            new IsEqual<>(nodeName)
        );
    }

}

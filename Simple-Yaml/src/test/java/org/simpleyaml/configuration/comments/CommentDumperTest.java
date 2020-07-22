package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.StringReader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.Throws;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

class CommentDumperTest {

    @Test
    void dump() throws IOException {
        final StringReader reader = new StringReader("test: 'test'\n" +
            "test-2: 'test-2'");
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final CommentMapper mapper = new CommentMapper(options);
        mapper.setComment("test", "test comment");
        mapper.setComment("test", "test comment", CommentType.SIDE);
        mapper.setComment("test-2", "test comment");
        mapper.setComment("test-2", "test comment", CommentType.SIDE);
        final CommentDumper dumper = new CommentDumper(options, mapper, reader);

        MatcherAssert.assertThat(
            "Comments are wrong!",
            dumper.dump(),
            new IsEqual<>("# test comment\n" +
                "test: 'test' # test comment\n" +
                "# test comment\n" +
                "test-2: 'test-2' # test comment\n")
        );
    }

    @Test
    void getNode() {
        final StringReader reader = new StringReader("test: 'test'\n" +
            "test-2: 'test-2'");
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final CommentMapper mapper = new CommentMapper(options);
        mapper.setComment("test", "test comment");
        mapper.setComment("test", "test comment", CommentType.SIDE);
        mapper.setComment("test-2", "test comment");
        mapper.setComment("test-2", "test comment", CommentType.SIDE);
        final CommentDumper dumper = new CommentDumper(options, mapper, reader);
        final KeyTree.Node testnode = dumper.getNode("test");
        final KeyTree.Node test2node = dumper.getNode("test-2");

        MatcherAssert.assertThat(
            "The indention is not 0!",
            testnode.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node name is not correct!",
            testnode.getName(),
            new IsEqual<>("test")
        );
        MatcherAssert.assertThat(
            "The comment's node is wrong",
            testnode.getComment(),
            new IsEqual<>("# test comment\n")
        );
        MatcherAssert.assertThat(
            "The side comment's node is wrong",
            testnode.getSideComment(),
            new IsEqual<>(" # test comment")
        );

        MatcherAssert.assertThat(
            "The indention is not 0!",
            test2node.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node name is not correct!",
            test2node.getName(),
            new IsEqual<>("test-2")
        );
        MatcherAssert.assertThat(
            "The Comment's node is wrong",
            test2node.getComment(),
            new IsEqual<>("# test comment\n")
        );
        MatcherAssert.assertThat(
            "The side comment's node is wrong",
            test2node.getSideComment(),
            new IsEqual<>(" # test comment")
        );
    }

}
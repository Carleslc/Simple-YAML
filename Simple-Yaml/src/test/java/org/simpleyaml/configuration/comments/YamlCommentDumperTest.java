package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.utils.StringUtils;

class YamlCommentDumperTest {

    @Test
    void dump() throws IOException {
        final String content = "test: 'test'\n" + "test-2: 'test-2'\n" + "test-3: 'test-3 #'\n";
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlCommentMapper mapper = new YamlCommentMapper(configuration.options());
        mapper.setComment("test", "test comment");
        mapper.setComment("test", "test comment", CommentType.SIDE);
        mapper.setComment("test-2", "test comment");
        mapper.setComment("test-2", "test comment", CommentType.SIDE);
        mapper.setComment("test-3", "test # comment");
        mapper.setComment("test-3", "test # comment", CommentType.SIDE);
        final StringWriter output = new StringWriter();
        final YamlCommentDumper dumper = new YamlCommentDumper(mapper, (writer -> {
            for (String line : StringUtils.lines(content)) {
                writer.write(line);
                writer.write('\n');
            }
        }), output);

        dumper.dump();

        MatcherAssert.assertThat(
            "Comments are wrong!",
            output.toString(),
            new IsEqual<>("# test comment\n" +
                "test: 'test' # test comment\n" +
                "# test comment\n" +
                "test-2: 'test-2' # test comment\n" +
                "# test # comment\n" +
                "test-3: 'test-3 #' # test # comment\n")
        );
    }

    @Test
    void getNode() throws IOException {
        final YamlConfiguration configuration = YamlConfiguration.loadConfigurationFromString("test: 'test'");
        final YamlCommentMapper mapper = new YamlCommentMapper(configuration.options());
        mapper.setComment("test", "test comment");
        mapper.setComment("test", "test comment", CommentType.SIDE);
        final YamlCommentDumper dumper = new YamlCommentDumper(mapper, configuration::dump, new StringWriter());
        final KeyTree.Node testNode = dumper.getNode("test");

        MatcherAssert.assertThat(
            "The indention is not 0!",
            testNode.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node name is not correct!",
            testNode.getName(),
            new IsEqual<>("test")
        );
        MatcherAssert.assertThat(
            "The comment's node is wrong",
            testNode.getComment(),
            new IsEqual<>("# test comment")
        );
        MatcherAssert.assertThat(
            "The side comment's node is wrong",
            testNode.getSideComment(),
            new IsEqual<>(" # test comment")
        );
    }

}

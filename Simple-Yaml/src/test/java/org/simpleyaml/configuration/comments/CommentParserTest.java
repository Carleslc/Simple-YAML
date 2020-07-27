package org.simpleyaml.configuration.comments;

import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.obj.TestYamlConfigurationOptions;

final class CommentParserTest {

    @Test
    void parse() throws IOException {
        final StringReader reader = new StringReader("# test comment\n" +
            "test: 'test' # test side comment \n" +
            "# test comment 2");
        final YamlConfiguration configuration = new YamlConfiguration();
        final YamlConfigurationOptions options = new TestYamlConfigurationOptions(configuration);
        final CommentParser parser = new CommentParser(options, reader);
        parser.parse();
    }

}

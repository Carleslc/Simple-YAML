package org.simpleyaml.examples;

import org.simpleyaml.configuration.comments.format.BlankLineYamlCommentFormatter;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatter;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatterConfiguration;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.utils.StringUtils;

import java.io.IOException;

/**
 * This example shows you how to format header and comments with custom prefixes, suffixes and blank lines.
 */
public class YamlCommentsFormatExample {

    public static void main(String[] args) throws IOException {
        // Create YamlFile with relative path
        final YamlFile yamlFile = new YamlFile("examples/test-comments-format.yml");

        // Load file
        yamlFile.createOrLoadWithComments();

        /* Header */

        // Set header format
        // Default header prefix is "# ", just like any other comment
        // Here we add a prefix line and add a suffix line to format the header
        yamlFile.options().headerFormatter()
                .prefixFirst("######################")
                .commentPrefix("##  ")
                .commentSuffix("  ##")
                .suffixLast("######################");

        // Set header
        yamlFile.setHeader("HEADER COMMENT"); // this header will be formatted using above header format and will have a blank line at the end

        /* Comments */

        // The default comment prefix is "# " without any blank lines above.
        // Here we change the default comment format to add blank lines automatically on root keys so it is more readable.
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        // Add values and comments
        yamlFile.path("key-1").set("This is a root key").comment("This comment has a blank line above (header separator)");

        yamlFile.path("key-2").set("This is a root key").comment("This comment has a blank line above (pretty format)")
                .path("key-2-1").set("This is a nested key").comment("This comment does not have a blank line above");

        yamlFile.path("key-2.key-2-2").set("This is a nested key")
                .comment("This comment has a blank line above").blankLine();

        // Blank lines above can also be added with the default BLANK_LINE comment format
        yamlFile.path("key-2.key-2-3").set("This is a nested key")
                .comment("This comment has a blank line above", YamlCommentFormat.BLANK_LINE);

        // Change the block comment prefix and suffix

        final String decorator = StringUtils.padding(20, '-');
        final String prefixFirst = "#" + decorator + "#\n# "; // Prefix for first line (default is "# ")
        final String prefixMultiline = YamlCommentFormatterConfiguration.DEFAULT_COMMENT_PREFIX; // Prefix for other comment lines (default)
        final String suffixLast = "\n#" + decorator + "#"; // Suffix for last comment line (none by default)

        final YamlCommentFormatter commentFormatter = yamlFile.options().commentFormatter();

        commentFormatter.blockFormatter()
                .prefix(prefixFirst, prefixMultiline)
                .suffix(suffixLast);

        // Change the side comment prefix to have an additional space
        commentFormatter.sideFormatter()
                .prefix("  # "); // Prefix for side comments (default is " # ")

        // Add values and comments
        yamlFile.path("custom").comment("Custom block format")
                .path("multiline").set("This is a nested key")
                    .comment("This is a multiline\ncomment without\nblank line above")
                    .commentSide("Side comment");

        // Set a custom comment formatter with same custom prefix and suffix and adding a blank line above
        // You could instead add a \n at the beginning of the block formatter prefix, but this is a demonstration
        // of how to reuse the default comment formatters (you could also create or inherit a PrettyYamlCommentFormatter)
        yamlFile.options().commentFormatter(
                new BlankLineYamlCommentFormatter(
                        commentFormatter.blockFormatter(),
                        commentFormatter.sideFormatter()
                )
        );
        yamlFile.path("custom.blank-line-custom").set("This is a nested key")
                .comment("This comment has\na blank line above\nand custom format");

        // You can also set a custom formatted comment directly prefixing all lines with # (raw)
        yamlFile.path("custom.raw").set("This is a nested key")
                .comment("\n# ***\n#~ This is a multiline\n#~~ custom raw comment\n# ***\n", YamlCommentFormat.RAW);

        // Save file
        yamlFile.save();

        System.out.println(yamlFile);
    }

}

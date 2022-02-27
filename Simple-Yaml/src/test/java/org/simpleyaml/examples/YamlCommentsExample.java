package org.simpleyaml.examples;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * This example shows you how to use this API to load and save a YAML file with comments.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlCommentsExample {

    public static void main(final String[] args) throws IOException {
        // Create new YAML file with relative path
        final YamlFile yamlFile = new YamlFile("examples/test-comments.yml");

        System.out.println(yamlFile.getFilePath());

        // Load the YAML file if is already created or create new one otherwise
        yamlFile.createOrLoadWithComments();

        // Set header format

        // This is optional, the default header prefix is "# "
        // Here we change the prefix and add some suffix to format the header.
        yamlFile.options().headerFormatter()
                .prefixFirst("######################")
                .commentPrefix("##  ")
                .commentSuffix("  ##")
                .suffixLast("######################");

        // Set header

        yamlFile.setHeader("HEADER COMMENT"); // this header will be formatted using above header format and will have a blank line at the end

        // Get header

        System.out.println("Header without format: " + yamlFile.options().header()); // this returns the string you set above
        System.out.println("Header with format:\n" + yamlFile.getHeader()); // this returns the header formatted

        System.out.println("Copy header: " + yamlFile.options().copyHeader()); // true if header is going to be written to the file
        System.out.print("Dump header:\n" + yamlFile.buildHeader()); // this returns how the header will be added to the file

        /*
          The difference between buildHeader() and getHeader() is that buildHeader() returns the header as it will be in the result file:
          it will have a blank line at the end and it would be empty "" if copying the header is not desired: options().copyHeader(false)
        */

        // Add some default values
        yamlFile.addDefault("test.number", 5);
        yamlFile.addDefault("test.string", "Hello world");
        yamlFile.addDefault("test.boolean", true);

        // Set comments programmatically

        yamlFile.setComment("test", "Test comments"); // The # prefix is not needed (it will be added automatically by the default comment format)
        yamlFile.setComment("test.string", "# Hello!"); // But you can include it if you want (this overrides the default comment format)
        yamlFile.setComment("test.list", "List of words");

        // You can add values after setting a comment
        yamlFile.set("test.list", Arrays.asList("Each word will be in a separated entry".split(" ")));

        // You can add comments even on list values!
        yamlFile.setComment("test.list.entry", "Comment on a list item");

        // Side comments
        yamlFile.setComment("test.list.entry", ":)", CommentType.SIDE);

        // Multiline comments
        yamlFile.setComment("test.wrap", "This is a\nmultiline comment");
        yamlFile.set("test.wrap", "# this is not a comment");

        // Setting blank lines as a comment, in different ways
        yamlFile.setComment("test.blank", "\n"); // Set \n as a block comment
        yamlFile.setComment("test.blank", "", YamlCommentFormat.BLANK_LINE); // Use the BLANK_LINE comment format
        yamlFile.setBlankLine("test.blank"); // Convenience method
        yamlFile.path("test.blank").blankLine(); // Alternative API

        yamlFile.set("test.blank", ""); // empty value (this is not a comment)
        yamlFile.set("math.pi", Math.PI);

        // Set default comment format

        // This is optional, the default comment prefix is "# " without any blank lines above.
        // Here we change the default comment format to add blank lines automatically on root keys so it is more readable.
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        /*
          The default comment format can be overridden in several ways:
          - Changing the default comment format with setCommentFormat method
          - Changing the comment format for a specific comment with setComment passing the comment format as the last argument
          - Adding the # prefix by hand on every comment line at setComment
            (if all comment lines are either \n or prefixed with # then the default comment format is ignored)
         */

        // With the PRETTY comment format a blank line will be added by default above the "math" key when setting a comment
        yamlFile.setComment("math", "Wonderful numbers");
        // Another way to add a blank line before a comment with the DEFAULT comment format (the default if PRETTY is not enabled)
        yamlFile.setComment("math", "\n# Wonderful numbers", YamlCommentFormat.DEFAULT);
        // Or using the BLANK_LINE comment format
        yamlFile.setComment("math", "Wonderful numbers", YamlCommentFormat.BLANK_LINE);
        // Or using the alternative API
        yamlFile.path("math").comment("Wonderful numbers", YamlCommentFormat.BLANK_LINE);
        yamlFile.path("math").comment("Wonderful numbers").blankLine();

        // With side comments the BLANK_LINE comment format sets the side comment as a block comment below
        yamlFile.setComment("math.pi", "Side comment below", CommentType.SIDE, YamlCommentFormat.BLANK_LINE);

        final Date now = new Date();
        final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Alternative API for setting values along with comments
        yamlFile.path("timestamp").comment("Some timestamps") // select the "timestamp" path and add a comment
                .path("canonicalDate").set(now).comment("ISO") // select the "timestamp.canonicalDate" child path, set its value and add a comment
                .parent() // go back to the "timestamp" path
                .path("formattedDate").set(df.format(now)).comment("Date/Time with format").commentSide(df.toPattern());

        // If a section or key does not exist then it will not be created when adding a comment to that path
        yamlFile.setComment("unknown", "This comment will not be written to the file");

        // Get comments

        // The DEFAULT or PRETTY comment formatters will return a clean comment using getComment, without # prefix nor blank lines
        System.out.println(
            yamlFile.getComment("test.string") + " " + yamlFile.getComment("test.list.entry", CommentType.SIDE)
        );

        // The RAW comment formatter will return the comments with the # prefix and blank lines using getComment
        yamlFile.setCommentFormat(YamlCommentFormat.RAW);

        System.out.println(
                yamlFile.getComment("test.wrap")
        );

        System.out.println(
                yamlFile.getComment("test.wrap", YamlCommentFormat.PRETTY) // change the comment format only for a specific comment
        );

        System.out.println(
                yamlFile.getComment("math") // Uses the RAW format set previously
        );

        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        // Footer comment
        yamlFile.setFooter("End"); // blank line will be added above the footer with the PRETTY comment format

        System.out.println(yamlFile.getFooter()); // will use the comment format previously set

        // Save the file!
        yamlFile.save();
    }

}

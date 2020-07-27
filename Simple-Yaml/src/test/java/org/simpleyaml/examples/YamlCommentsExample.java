package org.simpleyaml.examples;

import java.io.*;
import java.util.Objects;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

/**
 * This class shows you how to use this API to load and save a YAML file with comments.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlCommentsExample {

    public static void main(final String[] args) throws Exception {
        final YamlFile yamlFile = YamlCommentsExample.fileShouldPreserveComments(YamlCommentsExample.getResource("test-comments.yml"));

        // Add some comments programmatically
        yamlFile.setComment("test.string", "Hello!");
        yamlFile.setComment("test.list.entry", ":)", CommentType.SIDE);

        // Get comments programmatically
        System.out.println(
            yamlFile.getComment("test.string") + " " + yamlFile.getComment("test.list.entry", CommentType.SIDE)
        );

        // Other files with comments
        YamlCommentsExample.fileShouldPreserveComments(YamlCommentsExample.getResource("test-comments2.yml"));
        YamlCommentsExample.fileShouldPreserveComments(YamlCommentsExample.getResource("test-comments3.yml"));
    }

    private static YamlFile fileShouldPreserveComments(final String path) throws Exception {
        // Create new YAML file with relative path
        final YamlFile yamlFile = new YamlFile(path);

        // Load the YAML file if it is already created
        if (!yamlFile.exists()) {
            throw new FileNotFoundException(yamlFile.getFilePath() + " does not exist");
        }
        System.out.println(yamlFile.getFilePath() + " exists, loading configurations...");
        yamlFile.loadWithComments(); // Loads the entire file with existing comments

        final String loaded = yamlFile.fileToString();

        // Save the file with comments!
        try {
            yamlFile.saveWithComments();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final String saved = yamlFile.fileToString();

        final boolean same = saved.trim().equals(loaded.trim());

        // Check that comments have been properly copied
        System.out.println("File is the same after save with comments?: " + same);

        if (!same) {
            System.out.println(saved);

            // Restore original file
            try (final Writer writer = new OutputStreamWriter(new FileOutputStream(yamlFile.getConfigurationFile()))) {
                writer.write(loaded);
            }
            // Fail org.simple.yaml.test case
            throw new AssertionError(path + " comments are not properly copied");
        }

        return yamlFile;
    }

    private static String getResource(final String file) {
        return Objects.requireNonNull(YamlCommentsExample.class.getClassLoader().getResource(file)).getPath();
    }

}

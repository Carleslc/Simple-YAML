package org.simpleyaml.examples;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.*;
import java.util.Objects;

/**
 * This class shows you how to use this API to load and save a YAML file with comments.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlCommentsExample {

    public static void main(String[] args) throws Exception {
        YamlFile yamlFile1 = fileShouldPreserveComments(getResource("test-comments.yml"));

        // Get comments programmatically
        System.out.println(
            yamlFile1.getComment("test.string") + " " + yamlFile1.getComment("test.list.entry", CommentType.SIDE)
        );

        // Other files with comments
        fileShouldPreserveComments(getResource( "test-comments2.yml"));
        fileShouldPreserveComments(getResource("test-comments3.yml"));
    }

    private static YamlFile fileShouldPreserveComments(String path) throws Exception {
        // Create new YAML file with relative path
        YamlFile yamlFile = new YamlFile(path);

        // Load the YAML file if it is already created
        if (!yamlFile.exists()) {
            throw new FileNotFoundException(yamlFile.getFilePath() + " does not exist");
        }
        System.out.println(yamlFile.getFilePath() + " exists, loading configurations...");
        yamlFile.loadWithComments(); // Loads the entire file with existing comments

        String loaded = yamlFile.fileToString();

        // Add some comments programmatically
        yamlFile.setComment("test.string", "Hello!");
        yamlFile.setComment("test.list.entry", ":)", CommentType.SIDE);

        // Save the file with comments!
        try {
            yamlFile.saveWithComments();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String saved = yamlFile.fileToString();

        boolean same = saved.trim().equals(loaded.trim());

        // Check that comments have been properly copied
        System.out.println("File is the same after save with comments?: " + same);

        if (!same) {
            System.out.println(saved);

            // Restore original file
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(yamlFile.getConfigurationFile()))) {
                writer.write(loaded);
            }
            // Fail org.simple.yaml.test case
            throw new AssertionError(path + " comments are not properly copied");
        }

        return yamlFile;
    }

    private static String getResource(String file) {
        return Objects.requireNonNull(YamlCommentsExample.class.getClassLoader().getResource(file)).getPath();
    }

}
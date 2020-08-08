package org.simpleyaml.examples;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * This class shows you how to use this API to load and save a YAML file with comments.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlCommentsExample {

    public static void main(final String[] args) throws Exception {
        // Create new YAML file
        final YamlFile yamlFile = new YamlFile(getResource("test-comments.yml"));

        // Load the YAML file if it is already created
        if (!yamlFile.exists()) {
            throw new FileNotFoundException(yamlFile.getFilePath() + " does not exist");
        }

        // Add some comments programmatically
        yamlFile.setComment("test.string", "Hello!");
        yamlFile.setComment("test.list.entry", ":)", CommentType.SIDE);

        // Get comments programmatically
        System.out.println(
            yamlFile.getComment("test.string") + " " + yamlFile.getComment("test.list.entry", CommentType.SIDE)
        );

        // Save the file!
        try {
            yamlFile.save();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static URL getResource(final String file) {
        return Objects.requireNonNull(YamlCommentsExample.class.getClassLoader().getResource(file));
    }

}

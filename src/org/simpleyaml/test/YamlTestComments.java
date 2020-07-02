package org.simpleyaml.test;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class shows you how to use this API to load and save a YAML file with comments.
 * @author Carlos Lazaro Costa
 */
public final class YamlTestComments {
	
	public static void main(String[] args) throws IOException {
		fileShouldPreserveComments("test-comments.yml");
		fileShouldPreserveComments("test-comments2.yml");
		fileShouldPreserveComments("test-comments3.yml");
	}

	private static void fileShouldPreserveComments(String path) throws IOException {
		// Create new YAML file with relative path
		YamlFile yamlFile = new YamlFile(path);

		// Load the YAML file if is already created or create a new one otherwise
		try {
			if (!yamlFile.exists()) {
				System.err.println(yamlFile.getFilePath() + " does not exist");
				return;
			}
			System.out.println(yamlFile.getFilePath() + " exists, loading configurations...");
			yamlFile.load(); // Loads the entire file
		} catch (Exception e) {
			e.printStackTrace();
		}

		String loaded = yamlFile.toString().trim();

		// Save the file with comments!
		try {
			yamlFile.saveWithComments();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String saved = yamlFile.toString().trim();

		boolean same = saved.equals(loaded);

		// Check that comments have been properly copied
		System.out.println("File is the same after save with comments?: " + same);

		if (!same) {
			// Restore original file
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(yamlFile.getConfigurationFile()))) {
				writer.write(loaded);
			}
			// Fail test case
			throw new AssertionError(path + " comments are not properly copied");
		}
	}

}

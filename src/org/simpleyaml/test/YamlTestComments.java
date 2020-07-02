package org.simpleyaml.test;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;

/**
 * This class shows you how to use this API to load and save a YAML file with comments.
 * @author Carlos Lazaro Costa
 */
public final class YamlTestComments {
	
	public static void main(String[] args) throws IOException {
		
		// Create new YAML file with relative path
		YamlFile yamlFile = new YamlFile("test-comments.yml");
		
		// Load the YAML file if is already created or create a new one otherwise
		try {
			if (!yamlFile.exists()) {
				System.err.println(yamlFile.getFilePath() + " does not exist");
				return;
			}
			System.out.println(yamlFile.getFilePath() + " already exists, loading configurations...\n");
			yamlFile.load(); // Loads the entire file
		} catch (Exception e) {
			e.printStackTrace();
		}

		String loaded = fileToString(yamlFile);

		// Save the file with comments!
		try {
			yamlFile.saveWithComments();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String saved = fileToString(yamlFile);

		// Check that comments have been properly copied
		System.out.println("File is the same after save with comments?: " + saved.equals(loaded));
	}

	private static String fileToString(YamlFile file) throws IOException {
		return new String(Files.readAllBytes(file.getConfigurationFile().toPath())).trim();
	}

}

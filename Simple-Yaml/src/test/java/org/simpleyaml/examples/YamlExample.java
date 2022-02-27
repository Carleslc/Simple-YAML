package org.simpleyaml.examples;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

/**
 * YAML is a human-readable data serialization language.<br>
 * This example shows you how to use this API to use these files to save your data.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlExample {

    public static void main(final String[] args) {

        // Create new YAML file with relative path
        final YamlFile yamlFile = new YamlFile("examples/test.yml");

        // Load the YAML file if is already created or create new one otherwise
        try {
            if (!yamlFile.exists()) {
                yamlFile.createNewFile(true);
                System.out.println("New file has been created: " + yamlFile.getFilePath() + "\n");
            } else {
                System.out.println(yamlFile.getFilePath() + " already exists, loading configurations...\n");
            }
            yamlFile.load(); // Loads the entire file
            // If your file has comments inside you have to load it with yamlFile.loadWithComments()
        } catch (final Exception e) {
            e.printStackTrace();
        }

        System.out.println("Copy default values: " + yamlFile.options().copyDefaults());

        // You can manage hierarchies by separating the sections with a dot at path
        // Let's put some values to the file

        // Defaults are added when they are not present in the file, otherwise the existing values will not be overwritten
        yamlFile.addDefault("test.number", 5);
        yamlFile.addDefault("test.string", "Hello world");
        yamlFile.addDefault("test.boolean", true);

        // Setting values overwrites the value if there was any
        yamlFile.set("math.pi", Math.PI);
        yamlFile.set("math.e", Math.E);

        // More additions, e.g. adding entire lists

        final List<String> words = Arrays.asList("Each word will be in a separated entry".split(" "));
        yamlFile.set("test.list", words);

        // You can move between sections with a ConfigurationSection

        ConfigurationSection section = yamlFile.createSection("timestamp");

        // Adding dates

        Date now = new Date();
        section.set("canonicalDate", now);

        final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        section.set("formattedDate", df.format(now));

        // Remove values or sections

        yamlFile.remove("test.number"); // Also with yamlFile.set("test.number", null)
        yamlFile.remove("math.e");

        // You can check if a value is already present at the selected path

        section = yamlFile.getConfigurationSection("test");

        // Default values are always set if options().copyDefaults() is set to true
        System.out.println("There is a default value at " + section.getName() + ".number?: "
            + yamlFile.isSet(section.getName() + ".number"));

        System.out.println("There is a value at math.e?: " + yamlFile.isSet("math.e"));

        // And you can check if a path is a ConfigurationSection or a simple value

        System.out.println("Is " + section.getCurrentPath() + " a ConfigurationSection?: "
            + yamlFile.isConfigurationSection(section.getCurrentPath()) + "\n");

        // Now we'll get some objects from the file

        now = (Date) yamlFile.get("timestamp.canonicalDate");
        System.out.println("Date: " + now);

        System.out.println("Formatted Date: " + yamlFile.getString("timestamp.formattedDate"));

        // We can iterate over sections with getKeys(deep) and getValues(deep) methods

        section.getValues(false).entrySet().forEach(System.out::println); // false is not recursive

        // You can use many methods to obtain some types without casting (String, int, double...)

        final double pi = yamlFile.getDouble("math.pi");
        System.out.println("PI: " + pi);

        final List<String> list = yamlFile.getStringList("test.list");
        System.out.println("List: " + list);

        // And you can also use methods with default values if the path is unknown

        final String value = yamlFile.getString("randomSection.noValue"); // returns null
        System.out.println(value);

        final String defValue = yamlFile.getString("randomSection.noValue", "Default"); // returns Default
        System.out.println(defValue);

        // Finally, save changes!
        try {
            yamlFile.save();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // Now, you can restart this test and see how the file is loaded due to it's already created

        // You can delete the generated file uncommenting next line and catching the I/O Exception
        // yamlFile.deleteFile();
    }

}

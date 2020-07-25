package org.simpleyaml.test;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * YAML is a human-readable data serialization language.<br>
 * This class shows you how to use this API to use these files to save your data.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlExample {

    public static void main(String[] args) {

        // Create new YAML file with relative path
        YamlFile yamlFile = new YamlFile("examples/test.yml");

        // Load the YAML file if is already created or create new one otherwise
        try {
            if (!yamlFile.exists()) {
                System.out.println("New file has been created: " + yamlFile.getFilePath() + "\n");
                yamlFile.createNewFile(true);
            } else {
                System.out.println(yamlFile.getFilePath() + " already exists, loading configurations...\n");
            }
            yamlFile.load(); // Loads the entire file
            // If your file has comments inside you have to load it with yamlFile.loadWithComments()
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Copy default values: " + yamlFile.options().copyDefaults());

        // You can manage hierarchies by separating the sections with a dot at path
        // Let's put some values to the file

        yamlFile.addDefault("test.number", 5);
        yamlFile.addDefault("test.string", "Hello world");
        yamlFile.addDefault("test.boolean", true);

        yamlFile.set("math.pi", Math.PI);
        yamlFile.set("math.e", Math.E);

        // More additions, e.g. adding entire lists

        List<String> list = Arrays.asList("Each word will be in a separated entry".split("[\\s]+"));
        yamlFile.set("test.list", list);

        // You can move between sections with a ConfigurationSection

        ConfigurationSection section = yamlFile.createSection("timestamp");

        // Adding dates

        Date now = new Date();
        section.set("canonicalDate", now);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

        double pi = yamlFile.getDouble("math.pi");
        System.out.println(pi);

        // And you can also use methods with default values if the path is unknown

        String value = yamlFile.getString("randomSection.noValue"); // returns null
        System.out.println(value);

        String defValue = yamlFile.getString("randomSection.noValue", "Default");
        System.out.println(defValue);

        // Finally, save changes!
        try {
            yamlFile.save();
            // If your file has comments inside you have to save it with yamlFile.saveWithComments()
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now, you can restart this test and see how the file is loaded due to it's already created

        // You can delete the generated file uncommenting next line and catching the I/O Exception
        // yamlFile.deleteFile();
    }

}

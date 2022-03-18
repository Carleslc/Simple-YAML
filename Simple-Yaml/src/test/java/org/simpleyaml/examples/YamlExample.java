package org.simpleyaml.examples;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * YAML is a human-readable data serialization language.
 * <p/>
 * This example shows you how to use this API to save your data and configurations using YAML.
 */
public final class YamlExample {

    public static void main(final String[] args) {

        // Create new YAML file with relative path
        final YamlFile yamlFile = new YamlFile("examples/test.yml");

        // Load the YAML file if is already created or create new one otherwise
        try {
            if (!yamlFile.exists()) {
                yamlFile.createNewFile();
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

        // We can get a set of section keys with getKeys(deep)
        Set<String> keys = section.getKeys(false); // false means not recursive
        System.out.println(section.getCurrentPath() + " keys: " + keys);

        // We can get a map of section keys and values with getValues(deep)
        Map<String, Object> values = section.getValues(false);

        // We can iterate over these entries
        values.entrySet().stream()
                .filter(e -> !e.getKey().equals("list"))
                .forEach(System.out::println);

        // You can use many methods to obtain some types without casting (String, int, double...)

        final double pi = yamlFile.getDouble("math.pi");
        System.out.println("math.pi: " + pi);

        final List<String> list = yamlFile.getStringList("test.list");
        System.out.println("test.list: " + list);

        // If you want to select only one value from a list you can use list indexing

        System.out.println("test.list[0]: " + yamlFile.getString("test.list[0]"));
        System.out.println("test.list[1]: " + yamlFile.getString("test.list[1]"));
        System.out.println("test.list[-1]: " + yamlFile.getString("test.list[-1]")); // -1 stands for the last element

        // And you can also use methods with default values if the path is unknown

        final String value = yamlFile.getString("randomSection.noValue"); // returns null
        System.out.println(value);

        final String defValue = yamlFile.getString("randomSection.noValue", "Default"); // returns Default
        System.out.println(defValue);

        // By default strings are written without quotes if they are not needed so the configuration file remains clean
        yamlFile.set("quotes.plain", "This is plain style");

        // If a string contains special characters like # then it will be wrapped within single quotes
        yamlFile.set("quotes.wrap", "# this is wrapped automatically with single quote style"); // this is a value, not a comment

        // If you need it, you can enforce a quote style
        yamlFile.set("quotes.custom", "This is double quote style", QuoteStyle.DOUBLE);

        // You can change the quote style for all values with specific type
        yamlFile.options().quoteStyleDefaults().setQuoteStyle(String.class, QuoteStyle.DOUBLE);
        yamlFile.set("quotes.customDefault", "This is double quote style too");

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

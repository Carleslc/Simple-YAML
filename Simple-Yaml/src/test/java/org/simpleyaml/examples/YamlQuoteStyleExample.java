package org.simpleyaml.examples;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This example shows you how to set values with custom quote styles like double quotes.
 */
public class YamlQuoteStyleExample {

    public static void main(String[] args) throws IOException {
        // Create YamlFile with relative path
        final YamlFile yamlFile = new YamlFile("examples/test-quote-style.yml");

        // Load file
        yamlFile.createOrLoadWithComments();

        // Change the quote style for String values
        yamlFile.options().quoteStyleDefaults().setQuoteStyle(String.class, QuoteStyle.DOUBLE);

        // (Optional) Change the side comment prefix with additional space
        yamlFile.options().commentFormatter().sideFormatter().prefix("  # ");

        // Set language to EN with a side comment, using the alternative API (path method)
        // This will use the quote style specified before
        yamlFile.path("language").set("EN").commentSide("English(EN), French(FR)...");

        // Change the quote style for List values
        /*
         List<String> is not a valid type at runtime, because of Java type erasure
         If you need to have different quote styles for different types of lists then specify this line with
         the desired quote style before setting the list values
         */
        yamlFile.options().quoteStyleDefaults().setQuoteStyle(List.class, QuoteStyle.DOUBLE);

        // Set the list values
        // This will use the quote style specified before
        yamlFile.set("string-list", Arrays.asList("Hello", "World"));

        // Set another value
        // This will use the default QuoteStyle.PLAIN, because Boolean.class QuoteStyle was not changed
        yamlFile.set("boolean", true);

        // Save file
        yamlFile.save();

        System.out.println(yamlFile);
    }

}

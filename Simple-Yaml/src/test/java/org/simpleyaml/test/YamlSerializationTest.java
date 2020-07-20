package org.simpleyaml.test;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.serialization.ConfigurationSerialization;

/**
 * This class shows you how to use this API to serialize and deserialize Objects.
 *
 * @author Carlos Lazaro Costa
 */
public final class YamlSerializationTest {

    public static void main(String[] args) {

        /*
         * You can save entire objects in YAML files serializing them.
         *
         * Before saving or loading objects from file you've to register the class
         * to serialize or deserialize.
         *
         * We will save and load an instance of Person class.
         * Take a look to that class to see which methods you have to code to make it work properly.
         */
        ConfigurationSerialization.registerClass(Person.class);

        YamlFile yamlFile = new YamlFile("test2.yml");

        try {
            if (yamlFile.exists()) {
                yamlFile.load();

                // If the registered class have methods to serialize and deserialize objects,
                // this will load the object correctly.
                Person p = (Person) yamlFile.get("org.simple.yaml.test.people.12345678A");
                System.out.println("Loaded object:\n " + p);
            } else {
                yamlFile.createNewFile(true);
                System.out.println("New file has been created: " + yamlFile.getFilePath());

                // Write an object to the YAML file
                Person p = new Person("12345678A", "John", 1990);

                yamlFile.set("org.simple.yaml.test.people." + p.getDni(), p);

                // Don't forget to save the file!
                yamlFile.save();

                System.out.println("Restart to load object that has been saved.");
            }
            // You can delete the generated file uncommenting next line
            // yamlFile.deleteFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package org.simpleyaml.examples;

import java.util.LinkedHashMap;
import java.util.Map;
import org.simpleyaml.configuration.serialization.ConfigurationSerializable;

/**
 * Class that represents a Person that is used only for the test examples.<br>
 * Note that has methods to serialize and deserialize Person objects to save them later.
 *
 * @author Carlos Lazaro Costa
 */
public class Person implements ConfigurationSerializable {

    private final String dni;

    private final String name;

    private final int birthYear;

    private boolean isAlive;

    public Person(final String dni, final String name, final int birthYear, final boolean isAlive) {
        this.dni = dni;
        this.name = name;
        this.birthYear = birthYear;
        this.isAlive = isAlive;
    }

    public Person(final String dni, final String name, final int birthYear) {
        this(dni, name, birthYear, true);
    }

    /*
     * The following methods allows you to serialize and deserialize
     * your object to save them correctly to a YAML file.
     *
     * If you want you can create a constructor that accepts a single Map<String, Object>
     * to deserialize instead using the method deserialize of below.
     */

    public static Person deserialize(final Map<String, Object> mappedObject) { // note that is static
        return new Person((String) mappedObject.get("dni"),
            (String) mappedObject.get("name"),
            (int) mappedObject.get("birthYear"),
            (boolean) mappedObject.get("isAlive"));
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> mappedObject = new LinkedHashMap<String, Object>();
        mappedObject.put("dni", this.dni);
        mappedObject.put("name", this.name);
        mappedObject.put("birthYear", this.birthYear);
        mappedObject.put("isAlive", this.isAlive);
        return mappedObject;
    }

    public String getDni() {
        return this.dni;
    }

    public String getName() {
        return this.name;
    }

    public int getBirthYear() {
        return this.birthYear;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public void kill() {
        this.isAlive = false;
    }

    @Override
    public String toString() {
        return "Person [dni= " + this.dni + ", name= " + this.name + ", birthYear= " + this.birthYear + ", isAlive= " + this.isAlive + "]";
    }

}

# Simple YAML API

This Java API provides an easy-to-use way to store data using the YAML format.

## What is YAML?

YAML is a human-readable data-oriented serialization language.

Serialization is the process of translating data structures or object state
into a format that can be stored and reconstructed later in the same or another
computer environment.

You can learn more about YAML language [here](http://yaml.org/spec/1.1/);
Specifically, if you're interested to learn about the YAML syntax you can go to the
[Chapter 2 of the YAML specification](http://yaml.org/spec/1.1/#id857168)

Filename extensions: `.yaml`, `.yml`

## Download

To use this API all you need is to download the **Simple-YAML.jar** of this repository
and put it as dependency on your project.

## Examples

The best way to learn how to use this API is through some examples.

You can find two examples to test at `src/org/simpleyaml/test/` ((here))[link].
There are three files:
* `YamlTest`: An example for create YAML files, save or delete simple values and move through the file.
* `YamlSerializationTest`: An example for save complex objects using serialization.
* `Person`: An example of class for complex objects used in the previous file. Here you can see how to serialize and deserialize objects.

For more information and methods see the Javadoc at `doc` ((here))[link].

## Dependences

This API uses the next two dependences, which are already included in **YAML-Manager.jar**,
so you don't have to worry about anything more than put the jar as dependency on your project.

* (SnakeYAML)[https://bitbucket.org/asomov/snakeyaml]
* (Guava)[https://github.com/google/guava]

As this API uses _SnakeYAML_ you can use all methods from that.
+ (Javadoc)[http://javadox.com/org.yaml/snakeyaml/1.15/overview-summary.html]
+ (Documentation)[https://bitbucket.org/asomov/snakeyaml/wiki/Documentation]
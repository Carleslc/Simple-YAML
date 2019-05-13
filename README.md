# Simple YAML API

[![](https://jitpack.io/v/Carleslc/Simple-YAML.svg)](https://jitpack.io/#Carleslc/Simple-YAML)

_This Java API provides an easy-to-use way to store data using the YAML format._

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/carleslc)

## What is YAML?

YAML is a human-readable data-oriented serialization language.

Serialization is the process of translating data structures or object state
into a format that can be stored and reconstructed later in the same or another
computer environment.

You can learn more about YAML language [here](http://www.yaml.org/spec/1.2/spec.html);
Specifically, if you're interested to learn about the YAML syntax you can go to the
[Chapter 2 of the YAML specification](http://www.yaml.org/spec/1.2/spec.html#Preview).

Filename extensions: `.yaml`, `.yml`

## What is Simple-YAML?

This API is a port from [Bukkit](https://github.com/Bukkit/Bukkit) configuration wrapper, so you can use this library wherever you want without Bukkit dependency.
Core internal configuration representation matches with the Bukkit one, but without the dependencies of Bukkit itself.

In addition, simplicity is added with the class `YamlFile`, which is an extension of `YamlConfiguration`, with easy use for creation, management and serialization of yaml files.

Furthermore, you can optionally save your files with **comments** in mind. With the original `YamlConfiguration` that is not possible, because if you save the file from code then all previous comments in file will be removed. However, at the moment it is not possible to add comments via code, so you will need to write them with a text editor as usual.

## How to install

To use this API all you need is to download the **Simple-YAML-1.3.jar** from this repository
and put it as dependency on your project.

### Maven

If you are using Maven you do not need to download the jar. Instead, add this repository and dependency to your `pom.xml`:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```
<dependencies>
    <dependency>
        <groupId>com.github.Carleslc</groupId>
        <artifactId>Simple-YAML</artifactId>
        <version>1.3</version>
    </dependency>
</dependencies>
```

## How to use

The best way to learn how to use this API is through some examples.

You can find two examples to test at `src/org/simpleyaml/test/` [(here)](https://github.com/Carleslc/Simple-YAML/tree/master/src/org/simpleyaml/test).
There are three files:
* `YamlTest`: An example for create YAML files, save or delete simple values and move through the file.
* `YamlSerializationTest`: An example for save complex objects using serialization.
* `Person`: An example of class for complex objects used in the previous file. Here you can see how to serialize and deserialize objects.

For more information and methods see the Javadoc at `doc`.

## Dependences

This API uses the next two dependences, which are already included in **Simple-YAML-1.3.jar**,
so you don't have to worry about anything more than put the jar as dependency on your project.

* [SnakeYAML](https://bitbucket.org/asomov/snakeyaml)
* [Guava](https://github.com/google/guava)

As this API uses _SnakeYAML_ you can use all methods from that too.
+ [Javadoc](http://javadox.com/org.yaml/snakeyaml/1.15/overview-summary.html)
+ [Documentation](https://bitbucket.org/asomov/snakeyaml/wiki/Documentation)

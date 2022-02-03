# Simple YAML API

[![Latest version](https://jitpack.io/v/Carleslc/Simple-YAML.svg)](https://jitpack.io/#Carleslc/Simple-YAML) [![Build Status](https://travis-ci.com/Carleslc/Simple-YAML.svg?branch=master)](https://travis-ci.com/Carleslc/Simple-YAML)

_This Java API provides an easy-to-use way to store data and provide configurations using the YAML format._

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/carleslc)

## What is YAML?

YAML is a human-readable data-oriented serialization language.

Serialization is the process of translating data structures or object state into a format that can be stored and reconstructed later in the same or another computer environment.

You can learn more about YAML language [here](http://www.yaml.org/spec/1.2/spec.html). Specifically, if you're interested to learn about the YAML syntax you can go to the
[Chapter 2 of the YAML specification](http://www.yaml.org/spec/1.2/spec.html#Preview).

Filename extensions: `.yaml`, `.yml`

## What is Simple-YAML?

This API is a port from [Bukkit](https://github.com/Bukkit/Bukkit) configuration wrapper with some features added, so you can use this library wherever you want without Bukkit dependency.

Simplicity is added with the class [`YamlFile`](https://carleslc.me/Simple-YAML/doc/Simple-Yaml/org/simpleyaml/configuration/file/YamlFile.html), which is an extension of [`YamlConfiguration`](https://carleslc.me/Simple-YAML/doc/Simple-Yaml/org/simpleyaml/configuration/file/YamlConfiguration.html), with easy use for creation, management and serialization of yaml files. This class is probably the only class you will need.

Furthermore, you can optionally save your files with **comments** in mind. With the original [`YamlConfiguration`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/configuration/file/YamlConfiguration.java) that is not possible, because if you save the file from code then all previous comments in the file will be removed. You can write your comments with a text editor as usual. In addition, not only you can preserve your comments but also with this API you can add comments programmatically to your configuration. Sounds good, right?

## How to install

To use this API all you need is to download the [latest Simple-Yaml.jar](https://github.com/Carleslc/Simple-YAML/releases) and put it as a dependency on your project.

### Maven

If you are using Maven you do not need to download the jar. Instead, add this repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
</repositories>
```

<details>
  <summary>Yaml + Configuration</summary>

```xml
<dependency>
    <groupId>me.carleslc.Simple-YAML</groupId>
    <artifactId>Simple-Yaml</artifactId>
    <version>1.7.3</version>
</dependency>
```

</details>

<details>
  <summary>Configuration only</summary>

```xml
<dependency>
    <groupId>me.carleslc.Simple-YAML</groupId>
    <artifactId>Simple-Configuration</artifactId>
    <version>1.7.3</version>
</dependency>
```

</details>

### Gradle

If you are using Gradle you do not need to download the jar. Instead, add this repository and dependency to your build file:

```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

<details>
  <summary>Yaml + Configuration</summary>

```gradle
dependencies {
  implementation 'me.carleslc.Simple-YAML:Simple-Yaml:1.7.3'
}
```

</details>

<details>
  <summary>Configuration only</summary>

```gradle
dependencies {
  implementation 'me.carleslc.Simple-YAML:Simple-Configuration:1.7.3'
}
```

</details>

## How to use

The best way to learn how to use this API is through some examples.

You can find some examples to test [here](https://github.com/Carleslc/Simple-YAML/tree/master/Simple-Yaml/src/test/java/org/simpleyaml/examples).

* [`YamlExample`](https://github.com/Carleslc/Simple-YAML/blob/master/Simple-Yaml/src/test/java/org/simpleyaml/examples/YamlExample.java): An example to create YAML files, save or delete simple values and move through the file configuration.
* [`YamlCommentsExample`](https://github.com/Carleslc/Simple-YAML/blob/master/Simple-Yaml/src/test/java/org/simpleyaml/examples/YamlCommentsExample.java): An example to load and save YAML files keeping comments.
* [`YamlEncodingExample`](https://github.com/Carleslc/Simple-YAML/blob/master/Simple-Yaml/src/test/java/org/simpleyaml/examples/YamlEncodingExample.java): A minimal example to check your encoding with Unicode characters.
* [`YamlSerializationExample`](https://github.com/Carleslc/Simple-YAML/blob/master/Simple-Yaml/src/test/java/org/simpleyaml/examples/YamlSerializationExample.java): An example for saving complex objects using serialization.
* [`Person`](https://github.com/Carleslc/Simple-YAML/blob/master/Simple-Yaml/src/test/java/org/simpleyaml/examples/Person.java): An example of class for complex objects used in the previous file. Here you can see how to serialize and deserialize objects.

Example `.yml` files [here](https://github.com/Carleslc/Simple-YAML/tree/master/Simple-Yaml/src/test/resources).

For more information and methods see the **Javadoc**:

- [Simple-YAML](https://carleslc.me/Simple-YAML/doc)
  - [Simple-Yaml](https://carleslc.me/Simple-YAML/doc/Simple-Yaml)
  - [Simple-Configuration](https://carleslc.me/Simple-YAML/doc/Simple-Configuration)

## Dependencies

This API uses _SnakeYAML_, which is already included in the [latest Simple-Yaml.jar](https://github.com/Carleslc/Simple-YAML/releases), so you don't have to worry about anything more than putting up the jar as a dependency on your project.

* [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml)
    + [Javadoc](https://javadoc.io/doc/org.yaml/snakeyaml/latest/index.html)
    + [Documentation](https://bitbucket.org/snakeyaml/snakeyaml/wiki/Documentation)

## Looking for other file type configurations?

Have a look to these repositories:

- [XML](https://github.com/portlek/xmlgration)
- [JSON](https://github.com/portlek/jsongration)
- [HOCON](https://github.com/portlek/hocongration)
- [TOML](https://github.com/portlek/tomlgration)

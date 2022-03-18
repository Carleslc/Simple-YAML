package org.simpleyaml.examples;

import java.io.IOException;
import java.nio.charset.Charset;
import org.simpleyaml.configuration.file.YamlFile;

/**
 * This example shows you how to use unicode values.
 */
public class YamlEncodingExample {

    public static void main(final String[] args) throws IOException {
        final YamlFile yamlFile = new YamlFile("examples/test-encoding.yml");

        yamlFile.set("encoding.default", Charset.defaultCharset().name());
        yamlFile.set("encoding.charset", yamlFile.options().charset().name());
        yamlFile.set("encoding.unicode", yamlFile.options().isUnicode());

        yamlFile.setComment("encoding.unicode", "Should be true to display values properly");

        yamlFile.set("ö", "ö");
        yamlFile.set("umlauts", "öüäß");
        yamlFile.set("hiragana", "ひらがな");
        yamlFile.set("kanji", "漢字");
        yamlFile.set("emoji", "\uD83D\uDE01");

        System.out.println(yamlFile);

        yamlFile.save();
    }

}

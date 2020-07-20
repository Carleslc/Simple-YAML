package org.simpleyaml.test;

import java.io.IOException;
import java.nio.charset.Charset;
import org.simpleyaml.configuration.file.YamlFile;

public class YamlEncodingTest {

    public static void main(String[] args) throws IOException {
        YamlFile yamlFile = new YamlFile("test-encoding.yml");

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

        yamlFile.saveWithComments();
    }

}

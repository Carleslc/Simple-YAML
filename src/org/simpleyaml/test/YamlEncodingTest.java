package org.simpleyaml.test;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.charset.Charset;

public class YamlEncodingTest {

    public static void main(String[] args) throws IOException {
        YamlFile yamlFile = new YamlFile("test-encoding.yml");

        yamlFile.createNewFile(false);

        yamlFile.setComment("encoding", "Should be UTF-8");
        yamlFile.set("encoding.default", Charset.defaultCharset().name());
        yamlFile.set("encoding.charset", FileConfiguration.getCharset().name());
        yamlFile.set("encoding.unicode", FileConfiguration.SYSTEM_UTF);
        yamlFile.set("ö", "ö");
        yamlFile.set("umlauts", "öüäß");
        yamlFile.set("hiragana", "ひらがな");
        yamlFile.set("kanji", "漢字");
        yamlFile.set("emoji", "\uD83D\uDE01");

        System.out.println(yamlFile);

        yamlFile.saveWithComments();
    }
}

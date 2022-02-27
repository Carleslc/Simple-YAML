package org.simpleyaml.configuration.file;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import org.cactoos.io.InputStreamOf;
import org.cactoos.io.TempFile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.comments.*;
import org.simpleyaml.examples.Person;
import org.simpleyaml.utils.StringUtils;

class YamlFileTest {

    @Test
    void load() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test.yml"));
        final String content = testContent();
        yamlFile.load();
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile.load(new File(YamlFileTest.getResourceURI("test.yml")));
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile.load(YamlFileTest.getResourceURL("test.yml").openStream());
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }
    
    @Test
    void loadWithFolderSpaces() throws Exception {
        final YamlFile yamlFile = new YamlFile(Objects.requireNonNull(getClass().getClassLoader().getResource("folder with space/test.yml")));
        final String content = testContent();
        yamlFile.load();
        MatcherAssert.assertThat(
                "Couldn't load the file!",
                yamlFile.saveToString(),
                new IsEqual<>(content)
        );
    }

    @Test
    void loadConfiguration() throws Exception {
        YamlFile yamlFile = YamlFile.loadConfiguration(new File(YamlFileTest.getResourceURI("test.yml")));
        final String content = testContent();
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new InputStreamOf(YamlFileTest.getResourceURI("test.yml")));
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new StringReader(content));
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void loadWithComments() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        final String content = testComments();
        yamlFile.loadWithComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void loadConfigurationWithComments() throws Exception {
        YamlFile yamlFile = YamlFile.loadConfiguration(new File(YamlFileTest.getResourceURI("test-comments.yml")), true);
        final String content = testComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new InputStreamOf(YamlFileTest.getResourceURI("test-comments.yml")), true);
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new StringReader(content), true);
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void createOrLoad() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        final String content = testWithHeader();
        yamlFile.createOrLoad();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void createOrLoadWithComments() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        final String content = testComments();
        yamlFile.createOrLoadWithComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void save() throws Exception {
        final File temp = tempFile();
        temp.delete();

        final YamlFile yamlFile = new YamlFile(temp);

        MatcherAssert.assertThat(
            "File already exists!",
            yamlFile.exists(),
            new IsNot<>(new IsTrue())
        );

        final String content = "number: 5\n";
        yamlFile.set("number", 5);

        yamlFile.save();
        yamlFile.save(temp);
        yamlFile.save(temp.getAbsolutePath());

        MatcherAssert.assertThat(
            "File has not being correctly saved!",
            fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );
    }

    @Test
    void saveWithComments() throws Exception {
        final File temp = tempFile();
        temp.delete();

        final YamlFile yamlFile = new YamlFile(temp);

        MatcherAssert.assertThat(
            "File already exists!",
            yamlFile.exists(),
            new IsNot<>(new IsTrue())
        );

        final String content = "# Test\n" +
            "number: 5 # Side\n";

        yamlFile.set("number", 5);
        yamlFile.setComment("number", "Test");
        yamlFile.setComment("number", "Side", CommentType.SIDE);

        yamlFile.save();

        MatcherAssert.assertThat(
            "File has not being correctly saved with comments!",
            fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );
    }

    @Test
    void fileToString() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test.yml"));
        final String content = testContent();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (fileToString)!",
            fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );

        yamlFile.load();
        yamlFile.set("test.number", 10);

        MatcherAssert.assertThat(
            "fileToString must not change until save!",
            fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );

        final String newContent = "test:\n" +
            "  number: 10\n" +
            "  string: Hello world\n" +
            "  boolean: true\n" +
            "  list:\n" +
            "    - Each\n" +
            "    - word\n" +
            "    - will\n" +
            "    - be\n" +
            "    - in\n" +
            "    - a\n" +
            "    - separated\n" +
            "    - entry\n" +
            "math:\n" +
            "  pi: 3.141592653589793\n" +
            "timestamp:\n" +
            "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
            "  formattedDate: 04/07/2020 15:18:04\n";

        yamlFile.setConfigurationFile(tempFile());
        yamlFile.save();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file after save (fileToString)!",
            fileToStringUnix(yamlFile),
            new IsEqual<>(newContent)
        );
    }

    @Test
    void saveToString() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test.yml"));
        yamlFile.load();
        final String content = testContent();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (toString)!",
            yamlFile.toString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final String content = testComments();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments2() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments2.yml"));
        yamlFile.loadWithComments();

        final String content = fileToStringUnix(yamlFile);

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments3() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments3.yml"));
        yamlFile.loadWithComments();

        final String content = fileToStringUnix(yamlFile);

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments4() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments4.yml"));
        yamlFile.loadWithComments();

        final String content = testCommentsSpecial();

        MatcherAssert.assertThat(
                "Couldn't get the content of the file (saveToString)!",
                yamlFile.saveToString(),
                new IsEqual<>(content));
    }

    @Test
    void getComment() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        MatcherAssert.assertThat(
            "Couldn't parse the comments correctly!",
            yamlFile.getComment("test.string"),
            new IsEqual<>("Hello!")
        );

        MatcherAssert.assertThat(
                "Couldn't parse comments correctly!",
                yamlFile.getComment("test.list.entry"),
                new IsEqual<>("Comment on a list item")
        );

        MatcherAssert.assertThat(
            "Couldn't parse the side comments correctly!",
            yamlFile.getComment("test.list.entry", CommentType.SIDE),
            new IsEqual<>(":)")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("test.wrap"),
                new IsEqual<>("This is a\nmultiline comment")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the side comments correctly!",
                yamlFile.getComment("test.wrap", CommentType.SIDE),
                new IsNull<>()
        );
    }

    @Test
    void getCommentEdgeCases() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments4.yml"));
        yamlFile.loadWithComments();

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("test. wrap "),
                new IsEqual<>("Block #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("test. wrap ", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile.getString("test. wrap "),
                new IsEqual<>(" # not a comment ")
        );

        yamlFile.getConfigurationSection("test").getKeys(false)
                .forEach((key) ->
                        MatcherAssert.assertThat(
                                "Side comment mismatch (test." + key + ")",
                                yamlFile.getComment("test." + key, CommentType.SIDE),
                                new IsEqual<>("Side #comment with # character")));
    }

    @Test
    void setComment() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        yamlFile.setComment("test.string", "Edited hello comment!");
        yamlFile.setComment("test.string", "Edited hello side comment!", CommentType.SIDE);

        MatcherAssert.assertThat(
            "Couldn't parse the comments correctly!",
            yamlFile.getComment("test.string"),
            new IsEqual<>("Edited hello comment!")
        );

        MatcherAssert.assertThat(
            "Couldn't parse the comments correctly!",
            yamlFile.getComment("test.string", CommentType.SIDE),
            new IsEqual<>("Edited hello side comment!")
        );
    }

    @Test
    void setCommentFormat() throws IOException {
        final YamlFile yamlFile = new YamlFile();

        MatcherAssert.assertThat(
                "Couldn't get the default comment formatter correctly!",
                yamlFile.options().commentFormatter(),
                new IsSame<>(YamlCommentFormat.DEFAULT.commentFormatter())
        );

        yamlFile.path("first").set(1).comment("1");
        yamlFile.path("second").set(2).comment("2").commentSide("side");

        String contents = "# 1\nfirst: 1\n# 2\nsecond: 2 # side\n";

        MatcherAssert.assertThat(
                "Couldn't format the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("first"),
                new IsEqual<>("1")
        );

        final YamlCommentFormatter customFormatter = new YamlCommentFormatter(new YamlCommentFormatterConfiguration("## "));
        yamlFile.setCommentFormat(customFormatter);

        MatcherAssert.assertThat(
                "Couldn't get the comment formatter correctly!",
                yamlFile.options().commentFormatter(),
                new IsSame<>(customFormatter)
        );

        yamlFile.path("first").set(1).comment("1");
        yamlFile.path("second").set(2).comment("2").commentSide("side");

        contents = "## 1\nfirst: 1\n## 2\nsecond: 2 # side\n";

        MatcherAssert.assertThat(
                "Couldn't format the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("first"),
                new IsEqual<>("1")
        );

        yamlFile.setCommentFormat(YamlCommentFormat.RAW);

        yamlFile.path("first").set(1).comment("#1");
        yamlFile.path("second").set(2).comment("\n#2").commentSide("#side");

        contents = "#1\nfirst: 1\n\n#2\nsecond: 2 #side\n";

        MatcherAssert.assertThat(
                "Couldn't format the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("second"),
                new IsEqual<>("\n#2")
        );

        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        yamlFile.path("first").set(1).comment("1");
        yamlFile.path("second").set(2).comment("2").commentSide("side");

        contents = "# 1\nfirst: 1\n\n# 2\nsecond: 2 # side\n";

        MatcherAssert.assertThat(
                "Couldn't format the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("second"),
                new IsEqual<>("2")
        );

        yamlFile.setCommentFormat(YamlCommentFormat.BLANK_LINE);

        yamlFile.path("first").set(1).comment("1");
        yamlFile.path("second").set(2).comment("2").commentSide("side");

        contents = "\n# 1\nfirst: 1\n\n# 2\nsecond: 2\n# side\n";

        MatcherAssert.assertThat(
                "Couldn't format the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("second"),
                new IsEqual<>("\n2")
        );

        MatcherAssert.assertThat(
                "Couldn't get the comment correctly!",
                yamlFile.getComment("second", CommentType.SIDE),
                new IsEqual<>("\nside")
        );
    }

    @Test
    void setGetComment() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final String content = fileToStringUnix(yamlFile);

        yamlFile.setCommentFormat(YamlCommentFormat.RAW);

        yamlFile.setComment("test", yamlFile.getComment("test"));
        yamlFile.setComment("math", yamlFile.getComment("math"));
        yamlFile.setComment("test.comment", yamlFile.getComment("test.comment"));
        yamlFile.setComment("test.comment", yamlFile.getComment("test.comment", CommentType.SIDE), CommentType.SIDE);
        yamlFile.setComment("test.list.entry", yamlFile.getComment("test.list.entry"));
        yamlFile.setComment("test.list.entry", yamlFile.getComment("test.list.entry", CommentType.SIDE), CommentType.SIDE);

        MatcherAssert.assertThat(
                "Couldn't set the comments correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(content)
        );
    }

    @Test
    void header() throws Exception {
        YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.load();

        MatcherAssert.assertThat(
                "Couldn't build the header correctly!",
                yamlFile.buildHeader(),
                new IsEqual<>(testHeader())
        );

        // header without stripping # prefix and without the last blank line
        final String headerWithPrefix = testHeader().trim();

        MatcherAssert.assertThat(
                "Couldn't get the options header correctly!",
                yamlFile.options().header(),
                new IsEqual<>(headerWithPrefix)
        );

        MatcherAssert.assertThat(
                "Couldn't get the header correctly!",
                yamlFile.getHeader(),
                new IsEqual<>(headerWithPrefix)
        );

        MatcherAssert.assertThat(
                "Couldn't get the first key comment correctly (loaded without comments)!",
                yamlFile.getComment("test"),
                new IsNull<>()
        );

        yamlFile.loadWithComments();

        MatcherAssert.assertThat(
                "Couldn't build the header correctly!",
                yamlFile.buildHeader(),
                new IsEqual<>(testHeader())
        );

        MatcherAssert.assertThat(
                "Couldn't get the options header correctly!",
                yamlFile.options().header(),
                new IsEqual<>(headerWithPrefix)
        );

        MatcherAssert.assertThat(
                "Couldn't get the header correctly!",
                yamlFile.getHeader(),
                new IsEqual<>(headerWithPrefix)
        );

        MatcherAssert.assertThat(
                "Couldn't get the first key comment correctly!",
                yamlFile.getComment("test"),
                new IsEqual<>("Test comments")
        );

        final String headerSeparator = StringUtils.padding(20, '#');
        yamlFile.options().headerFormatter()
                .prefixFirst(headerSeparator).commentPrefix("##\t").commentSuffix("\t##").suffixLast(headerSeparator);

        final String newHeader = "New header\nSecond line";
        yamlFile.setHeader(newHeader);

        MatcherAssert.assertThat(
                "Couldn't get the options new header correctly!",
                yamlFile.options().header(),
                new IsEqual<>(newHeader)
        );

        MatcherAssert.assertThat(
                "Couldn't get the new header correctly!",
                yamlFile.getHeader(),
                new IsEqual<>(headerSeparator + "\n##\tNew header\t##\n##\tSecond line\t##\n" + headerSeparator)
        );

        yamlFile.options().headerFormatter(new YamlHeaderFormatter().stripPrefix(true));

        MatcherAssert.assertThat(
                "Couldn't get the options new header correctly!",
                yamlFile.options().header(),
                new IsEqual<>(newHeader)
        );

        MatcherAssert.assertThat(
                "Couldn't get the new header correctly!",
                yamlFile.getHeader(),
                new IsEqual<>(newHeader)
        );

        yamlFile.loadWithComments();

        // header stripping # prefix and without the last blank line
        final String headerStrip = "#####################\n" +
                "#  HEADER COMMENT  ##\n" +
                "#####################";

        MatcherAssert.assertThat(
                "Couldn't build the header correctly!",
                yamlFile.buildHeader(),
                new IsEqual<>(testHeader())
        );

        MatcherAssert.assertThat(
                "Couldn't get the stripped header correctly!",
                yamlFile.getHeader(),
                new IsEqual<>(headerStrip)
        );

        MatcherAssert.assertThat(
                "Couldn't get the options header correctly!",
                yamlFile.options().header(),
                new IsEqual<>(headerWithPrefix)
        );

        MatcherAssert.assertThat(
                "Couldn't get the first key comment correctly!",
                yamlFile.getComment("test"),
                new IsEqual<>("Test comments")
        );
    }

    @Test
    void footer() throws Exception {
        YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        MatcherAssert.assertThat(
                "Couldn't get the footer correctly!",
                yamlFile.getFooter(),
                new IsEqual<>("End")
        );

        yamlFile.setCommentFormat(YamlCommentFormat.RAW);

        MatcherAssert.assertThat(
                "Couldn't get the footer correctly!",
                yamlFile.getFooter(),
                new IsEqual<>("\n# End")
        );
    }

    @Test
    void path() throws IOException {
        YamlFile yamlFile = new YamlFile();

        yamlFile.path("default").addDefault("default");
        yamlFile.path("test")
                .comment("Test comment", YamlCommentFormat.BLANK_LINE).commentSide("Side comment")
                .path("children")
                .blankLine()
                .path("child1")
                .addDefault(1).comment("Child comment").blankLine()
                .parent()
                .addDefault("child2", 2)
                .set("child3", 3);

        final String contents =
                "default: default\n" +
                "\n" +
                "# Test comment\n" +
                "test: # Side comment\n" +
                "  \n" +
                "  children:\n" +
                "    \n" +
                "    # Child comment\n" +
                "    child1: 1\n" +
                "    child2: 2\n" +
                "    child3: 3\n";

        MatcherAssert.assertThat(
                "Wrong current path!",
                yamlFile.path("test.children").getCurrentPath(),
                new IsEqual<>("test.children")
        );

        final YamlFileWrapper parent = yamlFile.path("test.children").parent();

        MatcherAssert.assertThat(
                "Wrong parent path!",
                parent,
                new IsNot<>(new IsNull<>())
        );

        MatcherAssert.assertThat(
                "Wrong parent path!",
                parent.getCurrentPath(),
                new IsEqual<>("test")
        );

        MatcherAssert.assertThat(
                "Couldn't save the contents correctly!",
                yamlFile.saveToString(),
                new IsEqual<>(contents)
        );
    }

    @Test
    void exists() throws Exception {
        final YamlFile yamlFile = new YamlFile(tempFile());

        MatcherAssert.assertThat(
            "The file couldn't be found!",
            yamlFile.exists(),
            new IsTrue()
        );

        yamlFile.deleteFile();

        MatcherAssert.assertThat(
            "The file still exists!",
            yamlFile.exists(),
            new IsNot<>(new IsTrue())
        );
    }

    @Test
    void createNewFile() throws Exception {
        final YamlFile yamlFile = new YamlFile(tempFile());

        yamlFile.deleteFile();

        MatcherAssert.assertThat(
            "The file already exists!",
            yamlFile.exists(),
            new IsNot<>(new IsTrue())
        );

        yamlFile.createNewFile(false);

        MatcherAssert.assertThat(
            "The file couldn't be found!",
            yamlFile.exists(),
            new IsTrue()
        );
    }

    @Test
    void deleteFile() throws Exception {
        final YamlFile yamlFile = new YamlFile(tempFile());
        yamlFile.createOrLoad();

        MatcherAssert.assertThat(
            "The file does not exists!",
            yamlFile.exists(),
            new IsTrue()
        );

        yamlFile.deleteFile();

        MatcherAssert.assertThat(
            "The file has not being deleted!",
            yamlFile.exists(),
            new IsNot<>(new IsTrue())
        );
    }

    @Test
    void getSize() throws URISyntaxException {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        final String content = testComments();

        MatcherAssert.assertThat(
          "The file size is not correct!",
          yamlFile.getSize(),
          new IsEqual<>(((long) content.getBytes().length))
        );
    }

    @Test
    void getFilePath() throws URISyntaxException {
        final File file = new File(YamlFileTest.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile(file);

        MatcherAssert.assertThat(
            "Configuration file path is not the same!",
            yamlFile.getFilePath(),
            new IsEqual<>(file.getAbsolutePath()));
    }

    @Test
    void getConfigurationFile() throws URISyntaxException {
        final File file = new File(YamlFileTest.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile(file);

        MatcherAssert.assertThat(
            "Configuration file is not the same!",
            yamlFile.getConfigurationFile(),
            new IsSame<>(file));
    }

    @Test
    void setConfigurationFile() throws URISyntaxException {
        final File file = new File(YamlFileTest.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile();

        yamlFile.setConfigurationFile(file);

        MatcherAssert.assertThat(
            "Configuration file has not changed!",
            yamlFile.getConfigurationFile(),
            new IsSame<>(file));
    }

    @Test
    void copyTo() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments.yml"));
        final File copy = tempFile();
        yamlFile.copyTo(copy);

        final YamlFile copied = new YamlFile(copy);
        copied.loadWithComments();
        final String content = testComments();
        MatcherAssert.assertThat(
            "Couldn't copy the file!",
            copied.toString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void testDefaultListFormatting() throws Exception {
        final Person p1 = new Person("12345678A", "John", 1990);
        final Person p2 = new Person("12345678B", "Maria", 1990);

        final YamlFile yamlFile = new YamlFile();

        yamlFile.set("test", Arrays.asList(1, 2, 3));
        yamlFile.set("people", Arrays.asList(p1, p2));

        final String expected = fileToStringUnix(new YamlFile(YamlFileTest.getResourceURI("test-map-list.yml")));

        MatcherAssert.assertThat(
                "List formatting is not the expected!",
                yamlFile.saveToString(),
                new IsEqual<>(expected));
    }

    static String testContent() {
        return "test:\n" +
                "  number: 5\n" +
                "  string: Hello world\n" +
                "  boolean: true\n" +
                "  list:\n" +
                "    - Each\n" +
                "    - word\n" +
                "    - will\n" +
                "    - be\n" +
                "    - in\n" +
                "    - a\n" +
                "    - separated\n" +
                "    - entry\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                "timestamp:\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  formattedDate: 04/07/2020 15:18:04\n";
    }

    static String testHeader() {
        return "######################\n" +
                "##  HEADER COMMENT  ##\n" +
                "######################\n\n";
    }

    static String testWithHeader() {
        return testHeader() +
                "test:\n" +
                "  number: 5\n" +
                "  string: Hello world\n" +
                "  boolean: true\n" +
                "  list:\n" +
                "    - Each\n" +
                "    - word\n" +
                "    - will\n" +
                "    - be\n" +
                "    - in\n" +
                "    - a\n" +
                "    - separated\n" +
                "    - entry\n" +
                "  wrap: '# this is not a comment'\n" +
                "  blank: ''\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                "timestamp:\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  formattedDate: 04/07/2020 15:18:04\n";
    }

    static String testComments() {
        return testHeader() +
                "# Test comments\n" +
                "test:\n" +
                "  number: 5\n" +
                "  # Hello!\n" +
                "  string: Hello world\n" +
                "  boolean: true\n" +
                "  # List of words\n" +
                "  list:\n" +
                "    - Each\n" +
                "    - word\n" +
                "    - will\n" +
                "    - be\n" +
                "    - in\n" +
                "    - a\n" +
                "    - separated\n" +
                "    # Comment on a list item\n" +
                "    - entry # :)\n" +
                "  # This is a\n" +
                "  # multiline comment\n" +
                "  wrap: '# this is not a comment'\n" +
                "\n" +
                "  blank: ''\n" +
                "\n" +
                "# Wonderful numbers\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                "  # Side comment below\n" +
                "\n" +
                "# Some timestamps\n" +
                "timestamp:\n" +
                "  # ISO\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  # Date/Time with format\n" +
                "  formattedDate: 04/07/2020 15:18:04 # dd/MM/yyyy HH:mm:ss\n" +
                "\n" +
                "# End\n";
    }

    static String testCommentsSpecial() {
        return testHeader() +
                "# Test comments\n" +
                "test:\n" +
                "  # Block #comment with # character\n" +
                "  ' wrap ': ' # not a comment ' # Side #comment with # character\n" +
                "  single: 'text with # character' # Side #comment with # character\n" +
                "  double: 'text with # character'  # Side #comment with # character\n" +
                "  es'cape: text with \"#\" character \\\" # Side #comment with # character\n" +
                "  es'cape2': text with '#' character \\\" # Side #comment with # character\n" +
                "  :es:cape3\": 'This is a string ''''with a # character \"inside of it' # Side #comment with # character\n" +
                "  -? escape4: -'# not a \\#comment # Side #comment with # character\n" +
                "  multiline: 'This is a string\\\" \\\" which got ''wrapped and also contains a     #\n" +
                "    in its ''content.' # Side #comment with # character\n" +
                "  multiline2: | # Side #comment with # character\n" +
                "    'line one' # not a comment\n" +
                "    line two # not a comment\n" +
                "  # This is a'#\n" +
                "  # multiline comment #~\n" +
                "  special2: text\"#\"' # Side #comment with # character\n" +
                "     # unexpected indentation comment but valid\n" +
                "  special3: text'#''# not comment # Side #comment with # character\n" +
                "  special4: text''#''# not comment # Side #comment with # character\n" +
                "  entries: # Side #comment with # character\n" +
                "    # Comment on a list item with # character\n" +
                "    - entry'#'' #:)\n" +
                "    # dangling comment\n" +
                "  # Block #comment with # character\n" +
                "\n" +
                "  # Multiple line comment with blank line\n" +
                "  comment: 'text with # character' # Side #comment with # character\n" +
                "\n" +
                "  # Multiple line comment\n" +
                "  #  with blank line\n" +
                "  blank: '' # Side #comment with # character\n" +
                "  # Multiple line comment with blank line\n";
    }

    static URI getResourceURI(final String file) throws URISyntaxException {
        return Objects.requireNonNull(YamlFileTest.getResourceURL(file).toURI());
    }

    static URL getResourceURL(final String file) {
        return YamlFileTest.class.getClassLoader().getResource(file);
    }

    static File tempFile() throws Exception {
        return new TempFile().value().toFile();
    }

    static String fileToStringUnix(YamlFile yamlFile) throws IOException {
        String content = yamlFile.fileToString();
        if (content != null) {
            // Strip Windows carriage to ensure testable contents are the same as in Unix
            content = content.replace("\r", "");
        }
        return content;
    }

}

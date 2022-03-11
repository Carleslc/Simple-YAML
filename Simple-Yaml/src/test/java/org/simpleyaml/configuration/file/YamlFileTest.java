package org.simpleyaml.configuration.file;

import org.cactoos.io.InputStreamOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.comments.KeyTree;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatter;
import org.simpleyaml.configuration.comments.format.YamlCommentFormatterConfiguration;
import org.simpleyaml.configuration.comments.format.YamlHeaderFormatter;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.serialization.ConfigurationSerialization;
import org.simpleyaml.examples.Person;
import org.simpleyaml.utils.StringUtils;
import org.simpleyaml.utils.TestResources;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.*;

class YamlFileTest {

    @Test
    void load() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test.yml"));
        final String content = TestResources.testContent();
        yamlFile.load();
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile.load(new File(TestResources.getResourceURI("test.yml")));
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile.load(TestResources.getResourceURL("test.yml").openStream());
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }
    
    @Test
    void loadWithFolderSpaces() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("folder with space/test.yml"));
        final String content = TestResources.testContent();
        yamlFile.load();
        MatcherAssert.assertThat(
                "Couldn't load the file!",
                yamlFile.saveToString(),
                new IsEqual<>(content)
        );
    }

    @Test
    void loadConfiguration() throws Exception {
        YamlFile yamlFile = YamlFile.loadConfiguration(new File(TestResources.getResourceURI("test.yml")));
        final String content = TestResources.testContent();
        MatcherAssert.assertThat(
            "Couldn't load the file!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new InputStreamOf(TestResources.getResourceURI("test.yml")));
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
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        final String content = TestResources.testComments();
        yamlFile.loadWithComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void loadConfigurationWithComments() throws Exception {
        YamlFile yamlFile = YamlFile.loadConfiguration(new File(TestResources.getResourceURI("test-comments.yml")), true);
        final String content = TestResources.testComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
        yamlFile = YamlFile.loadConfiguration(new InputStreamOf(TestResources.getResourceURI("test-comments.yml")), true);
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
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        final String content = TestResources.testWithHeader();
        yamlFile.createOrLoad();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void createOrLoadWithComments() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        final String content = TestResources.testComments();
        yamlFile.createOrLoadWithComments();
        MatcherAssert.assertThat(
            "Couldn't load the file with comments!",
            yamlFile.saveToString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void save() throws Exception {
        final File temp = TestResources.tempFile();
        //noinspection ResultOfMethodCallIgnored
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
            TestResources.fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );
    }

    @Test
    void saveWithComments() throws Exception {
        final File temp = TestResources.tempFile();
        //noinspection ResultOfMethodCallIgnored
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
            TestResources.fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );
    }

    @Test
    void fileToString() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test.yml"));
        final String content = TestResources.testContent();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (fileToString)!",
            TestResources.fileToStringUnix(yamlFile),
            new IsEqual<>(content)
        );

        yamlFile.load();
        yamlFile.set("test.number", 10);

        MatcherAssert.assertThat(
            "fileToString must not change until save!",
            TestResources.fileToStringUnix(yamlFile),
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

        yamlFile.setConfigurationFile(TestResources.tempFile());
        yamlFile.save();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file after save (fileToString)!",
            TestResources.fileToStringUnix(yamlFile),
            new IsEqual<>(newContent)
        );
    }

    @Test
    void saveToString() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test.yml"));
        yamlFile.load();
        final String content = TestResources.testContent();

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
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final String content = TestResources.testComments();

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments2() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments2.yml"));
        yamlFile.loadWithComments();

        final String content = TestResources.fileToStringUnix(yamlFile);

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments3() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments3.yml"));
        yamlFile.loadWithComments();

        final String content = TestResources.fileToStringUnix(yamlFile);

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (saveToString)!",
            yamlFile.saveToString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments4() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments4.yml"));
        yamlFile.loadWithComments();

        final String content = TestResources.testCommentsSpecial();

        MatcherAssert.assertThat(
                "Couldn't get the content of the file (saveToString)!",
                yamlFile.saveToString(),
                new IsEqual<>(content));
    }

    @Test
    void getComment() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
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

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("math", YamlCommentFormat.RAW),
                new IsEqual<>("\n# Wonderful numbers")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the side comments below correctly!",
                yamlFile.getComment("math.pi", CommentType.SIDE),
                new IsEqual<>("Side comment below")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the side comments below correctly!",
                yamlFile.getComment("math.pi", CommentType.SIDE, YamlCommentFormat.RAW),
                new IsEqual<>("\n# Side comment below")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("timestamp"),
                new IsEqual<>("Some timestamps")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("timestamp", YamlCommentFormat.RAW),
                new IsEqual<>("\n# Some timestamps")
        );
    }

    @Test
    void getCommentEdgeCases() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments4.yml"));
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

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("blank.empty", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character\nMultiple line side comment")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("test.entries.entry'#''", CommentType.SIDE),
                new IsEqual<>(":)\ndangling comment")
        );

        final YamlFile yamlFile2 = new YamlFile();

        yamlFile2.path("wrap")
                .set(" # not a comment", QuoteStyle.PLAIN)
                .commentSide("Side #comment with # character");

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.getComment("wrap", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile2.getString("wrap"),
                new IsEqual<>(" # not a comment")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.saveToString(),
                new IsEqual<>("wrap: ' # not a comment' # Side #comment with # character\n"));

        yamlFile2.remove("wrap");

        yamlFile2.path("null")
                .commentSide("# Side #comment with # character")
                .set(null, QuoteStyle.SINGLE);

        String tagContents = "'null': !!null '' # Side #comment with # character\n";

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.getComment("null", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile2.getString("null"),
                new IsNull<>()
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.saveToString(),
                new IsEqual<>(tagContents));

        yamlFile2.loadFromString(tagContents);

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.getComment("null", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile2.getString("null"),
                new IsNull<>()
        );

        yamlFile2.remove("null");

        yamlFile2.options().quoteStyleDefaults().setQuoteStyle(Integer.class, QuoteStyle.SINGLE);

        yamlFile2.path("i").set(1).commentSide("Side #comment with # character");

        tagContents = "i: !!int '1' # Side #comment with # character\n";

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.getComment("i", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile2.getInt("i"),
                new IsEqual<>(1)
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.saveToString(),
                new IsEqual<>(tagContents));

        yamlFile2.loadFromString(tagContents);

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.getComment("i", CommentType.SIDE),
                new IsEqual<>("Side #comment with # character")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the values correctly!",
                yamlFile2.getInt("i"),
                new IsEqual<>(1)
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile2.saveToString(),
                new IsEqual<>(tagContents));
    }

    @Test
    void loadWithTag() throws IOException {
        final YamlFile configuration = new YamlFile();
        configuration.loadFromString("tag: !!str ' # not a comment'\n");

        MatcherAssert.assertThat(
                "Comments are wrong!",
                configuration.getComment("tag", CommentType.SIDE),
                new IsNull<>()
        );

        MatcherAssert.assertThat(
                "Value is wrong!",
                configuration.get("tag"),
                new IsEqual<>(" # not a comment")
        );
    }

    @Test
    void setComment() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
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
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        yamlFile.loadWithComments();

        final String content = TestResources.fileToStringUnix(yamlFile);

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
        YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        yamlFile.load();

        MatcherAssert.assertThat(
                "Couldn't build the header correctly!",
                yamlFile.buildHeader(),
                new IsEqual<>(TestResources.testHeader())
        );

        // header without stripping # prefix and without the last blank line
        final String headerWithPrefix = TestResources.testHeader().trim();

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
                new IsEqual<>(TestResources.testHeader())
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
                new IsEqual<>(TestResources.testHeader())
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
        YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
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

        yamlFile.setConfigurationFile(TestResources.getResourceURI("test-comments4.yml"));
        yamlFile.loadWithComments();

        MatcherAssert.assertThat(
                "Couldn't get the footer correctly!",
                yamlFile.getFooter(),
                new IsEqual<>("\n" +
                        "# Multiline\n" +
                        "# footer with blank lines")
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
                .setChild("child3", 3);

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
        final YamlFile yamlFile = new YamlFile(TestResources.tempFile());

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
        final YamlFile yamlFile = new YamlFile(TestResources.tempFile());

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
        final YamlFile yamlFile = new YamlFile(TestResources.tempFile());
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
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        final String content = TestResources.testComments();

        MatcherAssert.assertThat(
          "The file size is not correct!",
          yamlFile.getSize(),
          new IsEqual<>(((long) content.getBytes().length))
        );
    }

    @Test
    void getFilePath() throws URISyntaxException {
        final File file = new File(TestResources.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile(file);

        MatcherAssert.assertThat(
            "Configuration file path is not the same!",
            yamlFile.getFilePath(),
            new IsEqual<>(file.getAbsolutePath()));
    }

    @Test
    void getConfigurationFile() throws URISyntaxException {
        final File file = new File(TestResources.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile(file);

        MatcherAssert.assertThat(
            "Configuration file is not the same!",
            yamlFile.getConfigurationFile(),
            new IsSame<>(file));
    }

    @Test
    void setConfigurationFile() throws URISyntaxException {
        final File file = new File(TestResources.getResourceURI("test.yml"));
        final YamlFile yamlFile = new YamlFile();

        yamlFile.setConfigurationFile(file);

        MatcherAssert.assertThat(
            "Configuration file has not changed!",
            yamlFile.getConfigurationFile(),
            new IsSame<>(file));
    }

    @Test
    void copyTo() throws Exception {
        final YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-comments.yml"));
        final File copy = TestResources.tempFile();
        yamlFile.copyTo(copy);

        final YamlFile copied = new YamlFile(copy);
        copied.loadWithComments();
        final String content = TestResources.testComments();
        MatcherAssert.assertThat(
            "Couldn't copy the file!",
            copied.toString(),
            new IsEqual<>(content)
        );
    }

    @Test
    void testMapListSerialization() throws Exception {
        ConfigurationSerialization.registerClass(Person.class);

        final YamlFile mapListYamlFile = new YamlFile(TestResources.getResourceURI("test-map-list.yml"));
        final String expected = TestResources.fileToStringUnix(mapListYamlFile);

        mapListYamlFile.loadWithComments();

        MatcherAssert.assertThat(
                "Could not load/save map list elements!",
                mapListYamlFile.saveToString(),
                new IsEqual<>(expected));

        MatcherAssert.assertThat(
                "Could not load map list comments!",
                mapListYamlFile.getComment("people[-1].id"),
                new IsEqual<>("Comment on list map element"));

        final Person p1 = new Person("12345678A", "John", 1990);
        final Person p2 = new Person("12345678B", "Maria", 1990);

        final List<Integer> testList = Arrays.asList(1, 2, 3, 2, 3);
        final List<Person> peopleList = Arrays.asList(p1, p2); // Serializable

        // Set -> Comment

        YamlFile yamlFile = new YamlFile();

        yamlFile.set("test", testList);
        yamlFile.set("people", peopleList);

        yamlFile.setComment("test[-1]", "last");
        yamlFile.setComment("test.2", "repeated");
        yamlFile.setComment("test[0]", "first");
        yamlFile.setComment("people[1].id", "Comment on list map element");

        final String output = yamlFile.saveToString();

        MatcherAssert.assertThat(
                "Could not save map list elements!",
                output,
                new IsEqual<>(expected));

        yamlFile.loadFromString(output);

        MatcherAssert.assertThat(
                "Could not parse list elements!",
                yamlFile.getIntegerList("test"),
                new IsEqual<>(testList));

        MatcherAssert.assertThat(
                "Could not parse map list elements!",
                yamlFile.getList("people"),
                new IsEqual<>(peopleList));

        MatcherAssert.assertThat(
                "Could not load/save map list elements!",
                yamlFile.saveToString(),
                new IsEqual<>(expected));

        // Comment -> Set

        yamlFile = new YamlFile();

        yamlFile.setComment("test[0]", "first");
        yamlFile.setComment("test.2", "repeated");
        yamlFile.setComment("test[-1]", "last");
        yamlFile.setComment("people[1].id", "Comment on list map element");

        yamlFile.set("test", testList);
        yamlFile.set("people", peopleList);

        MatcherAssert.assertThat(
                "Could not save map list elements!",
                yamlFile.saveToString(),
                new IsEqual<>(expected));

        // Comment (different order) -> Set

        yamlFile = new YamlFile();

        yamlFile.setComment("people[1].id", "Comment on list map element");
        yamlFile.setComment("test[-1]", "last");
        yamlFile.setComment("test[0]", "first");
        yamlFile.setComment("test.2", "repeated");

        yamlFile.set("test", testList);
        yamlFile.set("people", peopleList);

        MatcherAssert.assertThat(
                "Could not save map list elements!",
                yamlFile.saveToString(),
                new IsEqual<>(expected));
    }

    @Test
    void getListMap() throws Exception {
        ConfigurationSerialization.registerClass(Person.class);

        final Person p1 = new Person("12345678A", "John", 1990);
        final Person p2 = new Person("12345678B", "Maria", 1990);

        YamlFile yamlFile = new YamlFile(TestResources.getResourceURI("test-map-list.yml"));
        yamlFile.loadWithComments();

        final KeyTree tree = yamlFile.getCommentMapper().getKeyTree();

        MatcherAssert.assertThat(
                "List map node must be a list!",
                tree.get("people").isList(),
                new IsTrue()
        );

        MatcherAssert.assertThat(
                "Could not get list map keys!",
                tree.get("people[0].name"),
                new IsNot<>(new IsNull<>())
        );

        MatcherAssert.assertThat(
                "Could not get list map keys!",
                tree.get("people[0].name").getName(),
                new IsEqual<>("name")
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("people[0]"),
                new IsEqual<>(p1)
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("people[1]"),
                new IsEqual<>(p2)
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.getString("people[0].name"),
                new IsEqual<>(p1.getName())
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.getString("people[-1].id"),
                new IsEqual<>(p2.getId())
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.getInt("test[-3]"),
                new IsEqual<>(3)
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("test[-6]"),
                new IsNull<>()
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("test[5]"),
                new IsNull<>()
        );

        yamlFile.set("test[-1]", 4);

        MatcherAssert.assertThat(
                "Could not set list map values!",
                yamlFile.getInt("test[5]"),
                new IsEqual<>(4)
        );

        Map<String, Integer> innerMap = new LinkedHashMap<>();
        innerMap.put("one", 1);
        innerMap.put("two", 2);

        Map<String, Collection<Map<String, Integer>>> deepMap = new LinkedHashMap<>();

        List<Map<String, Integer>> listMap = new ArrayList<>(Collections.singletonList(innerMap));
        Set<Map<String, Integer>> setMap = new LinkedHashSet<>(Collections.singleton(innerMap));

        deepMap.put("list", listMap);
        deepMap.put("set", setMap);

        yamlFile.createSection("section").set("deep", deepMap);

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.getMapList("section.deep.list"),
                new IsEqual<>(listMap)
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("section.deep.list[0]"),
                new IsEqual<>(listMap.get(0))
        );

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.getInt("section.deep.list[0].one"),
                new IsEqual<>(listMap.get(0).get("one"))
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.get("section.deep.set"),
                new IsEqual<>(setMap)
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.get("section.deep.set[0]"),
                new IsEqual<>(listMap.get(0))
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.getInt("section.deep.list[0].one"),
                new IsEqual<>(listMap.get(0).get("one"))
        );

        yamlFile.set("section.deep.list[0].three", 3);

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.getInt("section.deep.list[0].three"),
                new IsEqual<>(3)
        );

        Map<String, String> innerMap2 = new LinkedHashMap<>();
        innerMap2.put("a", "a");
        innerMap2.put("b", "b");

        yamlFile.set("section.deep.list[-1]", innerMap2);
        yamlFile.set("section.deep.set[-1]", innerMap2);

        MatcherAssert.assertThat(
                "Could not set list map values!",
                yamlFile.get("section.deep.list[1]"),
                new IsEqual<>(innerMap2)
        );

        MatcherAssert.assertThat(
                "Could not set set map values!",
                yamlFile.get("section.deep.set[-1]"),
                new IsEqual<>(innerMap2)
        );

        yamlFile.set("section.deep.list[-1]", null);
        yamlFile.set("section.deep.set", null);

        MatcherAssert.assertThat(
                "Could not get list map values!",
                yamlFile.get("section.deep.list[1]"),
                new IsNull<>()
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.get("section.deep.set"),
                new IsNull<>()
        );

        yamlFile = new YamlFile();

        yamlFile.set("section.deep", deepMap);

        yamlFile.setComment("section.deep.list[0]", "[0]");
        yamlFile.setComment("section.deep.list[0].one", "[0].one");
        yamlFile.setComment("section.deep.list[0].one", "ONE", CommentType.SIDE);

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.getComment("section.deep.list[0]"),
                new IsEqual<>("[0]")
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.getComment("section.deep.list[0].one"),
                new IsEqual<>("[0].one")
        );

        MatcherAssert.assertThat(
                "Could not get set map values!",
                yamlFile.getComment("section.deep.list[0].one", CommentType.SIDE),
                new IsEqual<>("ONE")
        );

        final String output = "section:\n" +
                "  deep:\n" +
                "    list:\n" +
                "      # [0]\n" +
                "        # [0].one\n" +
                "      - one: 1 # ONE\n" +
                "        two: 2\n" +
                "        three: 3\n";

        MatcherAssert.assertThat(
                "Could not save list map comments!",
                yamlFile.saveToString(),
                new IsEqual<>(output)
        );
    }

}

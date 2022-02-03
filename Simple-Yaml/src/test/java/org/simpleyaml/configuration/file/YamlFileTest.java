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
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.IsTrue;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.examples.Person;

class YamlFileTest {

    private static URI getResourceURI(final String file) throws URISyntaxException {
        return Objects.requireNonNull(YamlFileTest.getResourceURL(file).toURI());
    }

    private static URL getResourceURL(final String file) {
        return YamlFileTest.class.getClassLoader().getResource(file);
    }

    private static File tempFile() throws Exception {
        return new TempFile().value().toFile();
    }

    private static String fileToStringUnix(YamlFile yamlFile) throws IOException {
        String content = yamlFile.fileToString();
        if (content != null) {
            // Strip Windows carriage to ensure testable contents are the same as in Unix
            content = content.replace("\r", "");
        }
        return content;
    }

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
            "Couldn't get the content of the file (toString)!",
            yamlFile.toString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments2() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments2.yml"));
        yamlFile.loadWithComments();

        final String content = "# Section\n" +
            "section:\n" +
            "  # Sub section\n" +
            "  sub-section-1:\n" +
            "    # List of numbers\n" +
            "    list:\n" +
            "      - 1\n" +
            "      - 2\n" +
            "      - 3\n" +
            "  sub-section-2: # Side comment\n" +
            "    list:\n" +
            "      - 1\n" +
            "      - 2 # Side comment on an arbitrary element\n" +
            "      - 3\n" +
            "  sub-section-3:\n" +
            "    # List of numbers\n" +
            "    list:        # Side comment with extra space\n" +
            "      - 1\n" +
            "      - 2\n" +
            "      - 3\n";

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (toString)!",
            yamlFile.toString(),
            new IsEqual<>(content));
    }

    @Test
    void saveToStringWithComments3() throws Exception {
        final YamlFile yamlFile = new YamlFile(YamlFileTest.getResourceURI("test-comments3.yml"));
        yamlFile.loadWithComments();

        final String content = "backup-config:\n" +
            '\n' +
            "  #######################################################################################################################\n" +
            "  # SERVER-FILES BACKUP\n" +
            "  #######################################################################################################################\n" +
            '\n' +
            "  # Backups your server.jar and all setting files before startup to /backups/server/...zip\n" +
            "  server-files-backup:\n" +
            "    enable: false\n" +
            '\n' +
            "    # Set max-days to 0 if you want to keep your server backups forever.\n" +
            "    max-days: 7\n" +
            '\n' +
            '\n' +
            "  #######################################################################################################################\n" +
            "  # WORLDS BACKUP\n" +
            "  #######################################################################################################################\n" +
            '\n' +
            "  # Backups all folders starting with \"world\" to /backups/worlds/...zip\n" +
            "  worlds-backup:\n" +
            "    enable: false\n" +
            '\n' +
            "    # Set max-days to 0 if you want to keep your world backups forever.\n" +
            "    max-days: 7\n" +
            '\n' +
            '\n' +
            "  #######################################################################################################################\n" +
            "  # PLUGINS BACKUP\n" +
            "  #######################################################################################################################\n" +
            '\n' +
            "  # Backups your plugins folder before startup to /backups/plugins/...zip\n" +
            "  plugins-backup:\n" +
            "    enable: true\n" +
            '\n' +
            "    # Set max-days to 0 if you want to keep your plugin backups forever.\n" +
            "    max-days: 7\n" +
            '\n';

        MatcherAssert.assertThat(
            "Couldn't get the content of the file (toString)!",
            yamlFile.toString(),
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
                "Couldn't parse the comments correctly!",
                yamlFile.getString("test.comment"),
                new IsEqual<>("text with # hashtag")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the comments correctly!",
                yamlFile.getComment("test.comment"),
                new IsEqual<>("Block #comment with # hashtag")
        );

        MatcherAssert.assertThat(
            "Couldn't parse the side comments correctly!",
            yamlFile.getComment("test.list.entry", CommentType.SIDE),
            new IsEqual<>(":)")
        );

        MatcherAssert.assertThat(
                "Couldn't parse the side comments correctly!",
                yamlFile.getComment("test.comment", CommentType.SIDE),
                new IsEqual<>("Side #comment with # hashtag")
        );
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

        final String expected = new YamlFile(YamlFileTest.getResourceURI("test-map-list.yml")).fileToString();

        MatcherAssert.assertThat(
                "List formatting is not the expected!",
                yamlFile.saveToString(),
                new IsEqual<>(expected));
    }

    public static String testContent() {
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

    public static String testCommentsHeader() {
        return "#####################\n" +
                "## INITIAL COMMENT ##\n" +
                "#####################\n" +
                "\n" +
                "# Test comments";
    }

    public static String testWithHeader() {
        return testCommentsHeader() + '\n' +
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
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                "timestamp:\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  formattedDate: 04/07/2020 15:18:04\n";
    }

    private static String testComments() {
        return testCommentsHeader() + '\n' +
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
                "      # Comment on a list item\n" +
                "    - entry # :)\n" +
                "  # Block #comment with # hashtag\n" +
                "  comment: 'text with # hashtag' # Side #comment with # hashtag\n" +
                "\n" +
                "# Wonderful number\n" +
                "math:\n" +
                "  pi: 3.141592653589793\n" +
                "  # Comment without direct key\n" +
                "\n" +
                "# Some timestamps\n" +
                "timestamp:\n" +
                "  # ISO\n" +
                "  canonicalDate: 2020-07-04T13:18:04.458Z\n" +
                "  # Date/Time with format\n" +
                "  formattedDate: 04/07/2020 15:18:04\n" +
                "\n" +
                "# End\n";
    }

}

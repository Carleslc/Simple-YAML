package org.simpleyaml.utils;

import org.cactoos.io.InputStreamOf;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TempFile;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public final class TestResources {

    public static URI getResourceURI(final String file) throws URISyntaxException {
        return Objects.requireNonNull(getResourceURL(file).toURI());
    }

    public static URL getResourceURL(final String file) {
        return TestResources.class.getClassLoader().getResource(file);
    }

    public static InputStream getResourceInputStream(final String file) {
        return new InputStreamOf(new ResourceOf(file));
    }

    public static File tempFile() throws Exception {
        return new TempFile().value().toFile();
    }

    public static String fileToStringUnix(final YamlFile yamlFile) throws IOException {
        // Strip Windows carriage to ensure testable contents are the same as in Unix
        return StringUtils.stripCarriage(yamlFile.fileToString());
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

    public static String testHeader() {
        return "######################\n" +
                "##  HEADER COMMENT  ##\n" +
                "######################\n\n";
    }

    public static String testWithHeader() {
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

    public static String testComments() {
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

    public static String testCommentsSpecial() {
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
                "  # This is a # multiline'\n" +
                "  # comment\n" +
                "  multiline2: | # Side #comment with # character\n" +
                "    'line one' # not a comment\n" +
                "    line two # not a comment\n" +
                "  special2: text\"#\"' # Side #comment with # character\n" +
                "     # unexpected indentation comment but valid\n" +
                "  special3: text'#''# not comment # Side #comment with # character\n" +
                "  special4: text''#''# not comment # Side #comment with # character\n" +
                "  deep0: # Side #comment with # character\n" +
                "    deep1:\n" +
                "      # Deep block comment\n" +
                "      deep2: deep # Deep side comment\n" +
                "      # Deep side comment below\n" +
                "  entries: # Side #comment with # character\n" +
                "    # Comment on a list item with # character\n" +
                "    - entry'#'' #:)\n" +
                "    # dangling comment\n" +
                "\n" +
                "blank:\n" +
                "  # Block #comment with # character\n" +
                "\n" +
                "  # Multiple line comment with blank line\n" +
                "  comment: 'text with # character' # Side #comment with # character\n" +
                "\n" +
                "  # Multiple line comment\n" +
                "  #  with blank line\n" +
                "  empty: '' # Side #comment with # character\n" +
                "           # Multiple line side comment\n" +
                "\n" +
                "explicit:\n" +
                "  # This is explicit style\n" +
                "  # Key comment 1\n" +
                "  'this is a # multiline'' key': value # Value comment\n" +
                "  # Key comment 2\n" +
                "  key: |- # Value comment\n" +
                "    this is a multiline\n" +
                "    value with blank line\n" +
                "  # Key comment 3\n" +
                "  # Key comment 4\n" +
                "  ? |\n" +
                "    this is a # multiline'\n" +
                "    key literal\n" +
                "  : | # Value comment\n" +
                "    this is a # multiline'\n" +
                "    value literal\n" +
                "  # Value comment below\n" +
                "\n" +
                "# Multiline\n" +
                "# footer with blank lines\n";
    }

    public static String testCommentsSingle() {
        return testHeader() +
                "# Test comments\n" +
                "'test':\n" +
                "  'number': !!int '5'\n" +
                "  # Hello!\n" +
                "  'string': 'Hello world'\n" +
                "  'boolean': !!bool 'true'\n" +
                "  # List of words\n" +
                "  'list':\n" +
                "    - 'Each'\n" +
                "    - 'word'\n" +
                "    - 'will'\n" +
                "    - 'be'\n" +
                "    - 'in'\n" +
                "    - 'a'\n" +
                "    - 'separated'\n" +
                "    # Comment on a list item\n" +
                "    - 'entry' # :)\n" +
                "  # This is a\n" +
                "  # multiline comment\n" +
                "  'wrap': '# this is not a comment'\n" +
                "\n" +
                "  'blank': ''\n" +
                "\n" +
                "# Wonderful numbers\n" +
                "'math':\n" +
                "  'pi': !!float '3.141592653589793'\n" +
                "  # Side comment below\n" +
                "\n" +
                "# Some timestamps\n" +
                "'timestamp':\n" +
                "  # ISO\n" +
                "  'canonicalDate': 2020-07-04T13:18:04.458Z\n" +
                "  # Date/Time with format\n" +
                "  'formattedDate': '04/07/2020 15:18:04' # dd/MM/yyyy HH:mm:ss\n" +
                "\n" +
                "# End\n";
    }

    public static String testCommentsLiteral() {
        return testHeader() +
                "# Test comments\n" +
                "\"test\":\n" +
                "  \"number\": !!int |-\n" +
                "    5\n" +
                "  # Hello!\n" +
                "  \"string\": |-\n" +
                "    Hello world\n" +
                "  \"boolean\": !!bool |-\n" +
                "    true\n" +
                "  # List of words\n" +
                "  \"list\":\n" +
                "    - |-\n" +
                "      Each\n" +
                "    - |-\n" +
                "      word\n" +
                "    - |-\n" +
                "      will\n" +
                "    - |-\n" +
                "      be\n" +
                "    - |-\n" +
                "      in\n" +
                "    - |-\n" +
                "      a\n" +
                "    - |-\n" +
                "      separated\n" +
                "    # Comment on a list item\n" +
                "    - |- # :)\n" +
                "      entry\n" +
                "  # This is a\n" +
                "  # multiline comment\n" +
                "  \"wrap\": |-\n" +
                "    # this is not a comment\n" +
                "\n" +
                "  \"blank\": \"\"\n" +
                "\n" +
                "# Wonderful numbers\n" +
                "\"math\":\n" +
                "  \"pi\": !!float |-\n" +
                "    3.141592653589793\n" +
                "  # Side comment below\n" +
                "\n" +
                "# Some timestamps\n" +
                "\"timestamp\":\n" +
                "  # ISO\n" +
                "  \"canonicalDate\": 2020-07-04T13:18:04.458Z\n" +
                "  # Date/Time with format\n" +
                "  \"formattedDate\": |- # dd/MM/yyyy HH:mm:ss\n" +
                "    04/07/2020 15:18:04\n" +
                "\n" +
                "# End\n";
    }

    public static String testCommentsFolded() {
        return testCommentsLiteral().replace(" |-", " >-");
    }
}

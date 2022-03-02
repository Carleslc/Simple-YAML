package org.simpleyaml.obj;

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

    public static String fileToStringUnix(YamlFile yamlFile) throws IOException {
        String content = yamlFile.fileToString();
        if (content != null) {
            // Strip Windows carriage to ensure testable contents are the same as in Unix
            content = content.replace("\r", "");
        }
        return content;
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
}

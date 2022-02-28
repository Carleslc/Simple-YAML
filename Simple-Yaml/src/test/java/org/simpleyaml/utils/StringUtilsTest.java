package org.simpleyaml.utils;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    void lines() {
        MatcherAssert.assertThat(
                StringUtils.lines("1\n2\n3\n\n"),
                new IsEqual<>(new String[] { "1", "2", "3" })
        );
        MatcherAssert.assertThat(
                StringUtils.lines("1\n2\n3\n\n", false),
                new IsEqual<>(new String[] { "1", "2", "3", "", "" })
        );
        MatcherAssert.assertThat(
                StringUtils.lines("\n1\n2\n3"),
                new IsEqual<>(new String[] { "", "1", "2", "3" })
        );
        MatcherAssert.assertThat(
                StringUtils.lines("1\n2\n3"),
                new IsEqual<>(new String[] { "1", "2", "3" })
        );
        MatcherAssert.assertThat(
                StringUtils.lines("1"),
                new IsEqual<>(new String[] { "1" })
        );
        MatcherAssert.assertThat(
                StringUtils.lines("\n"),
                new IsEqual<>(new String[0])
        );
        MatcherAssert.assertThat(
                StringUtils.lines("\n", false),
                new IsEqual<>(new String[] { "", "" })
        );
    }

    @Test
    void indentation() {
        MatcherAssert.assertThat(
                StringUtils.indentation(5),
                new IsEqual<>("     ")
        );
        MatcherAssert.assertThat(
                StringUtils.indentation(0),
                new IsEqual<>("")
        );
        MatcherAssert.assertThat(
                StringUtils.indentation(-1),
                new IsEqual<>("")
        );
    }

    @Test
    void padding() {
        MatcherAssert.assertThat(
                StringUtils.padding(5, '-'),
                new IsEqual<>("-----")
        );
        MatcherAssert.assertThat(
                StringUtils.padding(0, '-'),
                new IsEqual<>("")
        );
        MatcherAssert.assertThat(
                StringUtils.padding(-1, '-'),
                new IsEqual<>("")
        );
    }

    @Test
    void stripIndentation() {
        MatcherAssert.assertThat(
                StringUtils.stripIndentation(" "),
                new IsEqual<>("")
        );
        MatcherAssert.assertThat(
                StringUtils.stripIndentation("  Hi  "),
                new IsEqual<>("Hi  ")
        );
        MatcherAssert.assertThat(
                StringUtils.stripIndentation("Hi"),
                new IsEqual<>("Hi")
        );
        MatcherAssert.assertThat(
                StringUtils.stripIndentation(" 1 \n  2 "),
                new IsEqual<>("1 \n2 ")
        );
    }

    @Test
    void stripPrefix() {
        MatcherAssert.assertThat(
                StringUtils.stripPrefix("# Comment", "# "),
                new IsEqual<>("Comment")
        );
        MatcherAssert.assertThat(
                StringUtils.stripPrefix(" # Comment", "# "),
                new IsEqual<>(" # Comment")
        );
        MatcherAssert.assertThat(
                StringUtils.stripPrefix("#Comment", "# "),
                new IsEqual<>("#Comment")
        );
        MatcherAssert.assertThat(
                StringUtils.stripPrefix("#Comment", "# ", "#"),
                new IsEqual<>("Comment")
        );
    }

    @Test
    void afterNewLine() {
        MatcherAssert.assertThat(
                StringUtils.afterNewLine("1\n2"),
                new IsEqual<>("2")
        );
        MatcherAssert.assertThat(
                StringUtils.afterNewLine("\n2"),
                new IsEqual<>("2")
        );
        MatcherAssert.assertThat(
                StringUtils.afterNewLine("1\n"),
                new IsEqual<>("")
        );
        MatcherAssert.assertThat(
                StringUtils.afterNewLine("1"),
                new IsEqual<>("")
        );
    }

    @Test
    void splitTrailingNewLines() {
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines(""),
                new IsEqual<>(new String[] { "", "" })
        );
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines("\n"),
                new IsEqual<>(new String[] { "", "\n" })
        );
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines("1\n2"),
                new IsEqual<>(new String[] { "1\n2", "" })
        );
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines("1\n"),
                new IsEqual<>(new String[] { "1", "\n" })
        );
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines("\n2"),
                new IsEqual<>(new String[] { "\n2", "" })
        );
        MatcherAssert.assertThat(
                StringUtils.splitTrailingNewLines("1\n2\n\n"),
                new IsEqual<>(new String[] { "1\n2", "\n\n" })
        );
    }

    @Test
    void allLinesArePrefixed() {
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("", ""),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("#", ""),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("#", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\t#\n  #\n# ", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\t#\n  #\n \n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed(" ", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\n\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("1", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("#\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\n#\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("\n\n#\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("1\n#\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixed("#\n#\n 1", "#"),
                new IsEqual<>(false)
        );
    }

    @Test
    void allLinesArePrefixedOrBlank() {
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("", ""),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("#", ""),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("#", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("#\n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\t#\n  #\n# ", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\t#\n  #\n# \n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\t#\n  #\n \n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank(" ", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\n\n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("1", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\n#\n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("\n\n#\n", "#"),
                new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("1\n#\n", "#"),
                new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
                StringUtils.allLinesArePrefixedOrBlank("#\n#\n 1", "#"),
                new IsEqual<>(false)
        );
    }

    @Test
    void quoteNewLines() {
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines(""),
                new IsEqual<>("")
        );
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines("1"),
                new IsEqual<>("1")
        );
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines("\n"),
                new IsEqual<>("\\n")
        );
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines("\\n"),
                new IsEqual<>("\\n")
        );
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines("\n\n"),
                new IsEqual<>("\\n\\n")
        );
        MatcherAssert.assertThat(
                StringUtils.quoteNewLines("#\n#\n 1"),
                new IsEqual<>("#\\n#\\n 1")
        );
    }
}

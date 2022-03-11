package org.simpleyaml.configuration.comments;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.HasSize;
import org.llorllale.cactoos.matchers.HasValues;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class YamlKeyTreeTest {

    @Test
    void add() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String defaultnodekey = "defaultnodekey";
        final String nodewithcommentkey = "nodewithcommentkey";
        tree.add(defaultnodekey);
        tree.add(nodewithcommentkey);
        final KeyTree.Node defaultnode = tree.get(defaultnodekey);
        final KeyTree.Node nodewithcomment = tree.get(nodewithcommentkey);
        final String commentOfNodewithcomment = "Comment of nodewithcomment";
        final String sideCommentOfNodewithcomment = "Side comment of nodewithcomment";
        nodewithcomment.setComment(commentOfNodewithcomment);
        nodewithcomment.setSideComment(sideCommentOfNodewithcomment);

        MatcherAssert.assertThat(
            "The node has a root!",
            defaultnode.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node's name is not correct!",
            defaultnode.getName(),
            new IsEqual<>(defaultnodekey)
        );
        MatcherAssert.assertThat(
            "There is a comment!",
            defaultnode.getComment(),
            new IsNull<>()
        );
        MatcherAssert.assertThat(
            "There is a side comment!",
            defaultnode.getSideComment(),
            new IsNull<>()
        );

        MatcherAssert.assertThat(
            "The node has a root!",
            nodewithcomment.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node's name is not correct!",
            nodewithcomment.getName(),
            new IsEqual<>(nodewithcommentkey)
        );
        MatcherAssert.assertThat(
            "There is not a a comment!",
            nodewithcomment.getComment(),
            new IsEqual<>(commentOfNodewithcomment)
        );
        MatcherAssert.assertThat(
            "There is not a side comment!",
            nodewithcomment.getSideComment(),
            new IsEqual<>(sideCommentOfNodewithcomment)
        );
    }

    @Test
    void findParent() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String nodekey = "nodekey";
        tree.add(nodekey);
        final KeyTree.Node node = tree.get(nodekey);
        node.add("test-child");
        node.add("test-child-2");
        final KeyTree.Node parent = tree.findParent(2);

        MatcherAssert.assertThat(
            "The node has a root!",
            parent.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node's name is not correct!",
            parent.getName(),
            new IsEqual<>(nodekey)
        );
        MatcherAssert.assertThat(
            "There is a a comment!",
            parent.getComment(),
            new IsNull<>()
        );
        MatcherAssert.assertThat(
            "There is a side comment!",
            parent.getSideComment(),
            new IsNull<>()
        );
    }

    @Test
    void get() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String nodekey = "nodekey";
        tree.add(nodekey);
        final KeyTree.Node node = tree.get(nodekey);

        MatcherAssert.assertThat(
            "The node has a root!",
            node.getIndentation(),
            new IsEqual<>(0)
        );
        MatcherAssert.assertThat(
            "The node's name is not correct!",
            node.getName(),
            new IsEqual<>(nodekey)
        );
        MatcherAssert.assertThat(
            "There is a comment!",
            node.getComment(),
            new IsNull<>()
        );
        MatcherAssert.assertThat(
            "There is a side comment!",
            node.getSideComment(),
            new IsNull<>()
        );
    }

    @Test
    void getChild() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());

        final KeyTree.Node child = tree.add("test").add("child");
        final KeyTree.Node child2 = child.add("child2");
        final KeyTree.Node child3 = child.add("child3");

        MatcherAssert.assertThat(
                "The node is not correct!",
                child2,
                new IsSame<>(tree.get("test.child.child2"))
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child").getFirst(),
                new IsSame<>(tree.get("test.child").get(0))
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child").getLast(),
                new IsSame<>(tree.get("test.child").get(child.size() - 1))
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child").getLast(),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child[0]"),
                new IsSame<>(child2)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child[-1]"),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                child.get("[-1]"),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                child.get(-1),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child[1]"),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child.[1]"),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                child.get(1),
                new IsSame<>(child3)
        );

        MatcherAssert.assertThat(
                "The node is not correct!",
                tree.get("test.child[2]"),
                new IsNull<>()
        );
    }

    @Test
    void keys() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String nodekey1 = "nodekey1";
        final String nodekey2 = "nodekey2";
        final String nodekey3 = "nodekey3";
        tree.add(nodekey1);
        tree.add(nodekey2);
        tree.add(nodekey3);
        final Set<String> keys = tree.keys();

        MatcherAssert.assertThat(
            "There are not 3 node in the tree!",
            keys.size(),
            new IsEqual<>(3)
        );
        MatcherAssert.assertThat(
            "The node's key are not correct!",
            keys,
            new HasValues<>("nodekey1", "nodekey2", "nodekey3")
        );
    }

    @Test
    void entries() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String nodekey1 = "nodekey1";
        final String nodekey2 = "nodekey2";
        final String nodekey3 = "nodekey3";
        tree.add(nodekey1);
        tree.add(nodekey2);
        tree.add(nodekey3);
        final Set<Map.Entry<String, KeyTree.Node>> entries = tree.entries();

        MatcherAssert.assertThat(
            "There are not 3 node in the tree!",
            entries,
            new HasSize(3)
        );
        MatcherAssert.assertThat(
            "The node's key are not correct!",
            entries.stream().map(Map.Entry::getKey).collect(Collectors.toList()),
            new HasValues<>("nodekey1", "nodekey2", "nodekey3")
        );
    }

    @Test
    void testToString() {
        final YamlConfiguration configuration = new YamlConfiguration();
        final KeyTree tree = new KeyTree(configuration.options());
        final String nodekey = "nodekey";
        tree.add(nodekey);
        final String expected = "{indent=0, path='', name='', comment='null', side='null', children=[{indent=0, path='nodekey', name='nodekey', comment='null', side='null', children=[]}]}";

        MatcherAssert.assertThat(
            "toString returns a wrong value.",
            tree.toString(),
            new IsEqual<>(expected)
        );
    }

}
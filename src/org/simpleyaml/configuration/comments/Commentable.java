package org.simpleyaml.configuration.comments;

public interface Commentable {

    /**
     * Adds a comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * @param path path of desired section or value
     * @param comment the comment to add, # symbol is not needed
     * @param type either above (block) or side
     */
    void addComment(String path, String comment, CommentType type);

    /**
     * Adds a block comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     * @param path path of desired section or value
     * @param comment the comment to add, # symbol is not needed
     */
    default void addComment(String path, String comment) {
        addComment(path, comment, CommentType.BLOCK);
    }
}

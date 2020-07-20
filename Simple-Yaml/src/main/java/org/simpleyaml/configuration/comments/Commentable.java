package org.simpleyaml.configuration.comments;

public interface Commentable {

    String COMMENT_PREFIX = "# ";

    /**
     * Adds a comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     *
     * @param path path of desired section or value
     * @param comment the comment to add, # symbol is not needed
     * @param type either above (block) or side
     */
    void setComment(String path, String comment, CommentType type);

    /**
     * Adds a block comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     *
     * @param path path of desired section or value
     * @param comment the comment to add, # symbol is not needed
     */
    default void setComment(final String path, final String comment) {
        this.setComment(path, comment, CommentType.BLOCK);
    }

    /**
     * Retrieve the comment of the section or value selected by path.
     *
     * @param path path of desired section or value
     * @param type either above (block) or side
     * @return the comment of the section or value selected by path,
     * or null if that path does not have any comment of this type
     */
    String getComment(String path, CommentType type);

    /**
     * Retrieve the block comment of the section or value selected by path.
     *
     * @param path path of desired section or value
     * @return the block comment of the section or value selected by path,
     * or null if that path does not have any comment of type block
     */
    default String getComment(final String path) {
        return this.getComment(path, CommentType.BLOCK);
    }

}

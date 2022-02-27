package org.simpleyaml.configuration.comments;

public interface Commentable {

    /**
     * Set a comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     *
     * @param path    path of desired section or key
     * @param comment the comment to add, # symbol is not needed
     * @param type    either above (BLOCK) or SIDE
     */
    void setComment(String path, String comment, CommentType type);

    /**
     * Set a block comment above the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     *
     * @param path    path of desired section or key
     * @param comment the comment to add, # symbol is not needed
     */
    default void setComment(final String path, final String comment) {
        this.setComment(path, comment, CommentType.BLOCK);
    }

    /**
     * Retrieve the comment of the section or value selected by path.
     *
     * @param path path of desired section or key
     * @param type either above (BLOCK) or SIDE
     * @return the comment of the section or value selected by path,
     * or null if that path does not have any comment of this type
     */
    String getComment(String path, CommentType type);

    /**
     * Retrieve the block comment of the section or value selected by path.
     *
     * @param path path of desired section or key
     * @return the block comment of the section or value selected by path,
     * or null if that path does not have any comment of type block
     */
    default String getComment(final String path) {
        return this.getComment(path, CommentType.BLOCK);
    }

}

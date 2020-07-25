package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.comments.*;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;

import java.io.*;
import java.nio.file.Files;

/**
 * An extension of {@link YamlConfiguration} which saves all data in Yaml to a configuration file
 * with the added possibility to save and manage comments.
 * Note that this implementation is not synchronized.
 *
 * @author Carlos Lázaro Costa
 */
public class YamlFile extends YamlConfiguration implements Commentable {

    /** File where data will be saved for this configuration */
    private File configFile;

    /** A comment mapper to add comments to sections or values **/
    private CommentMapper commentMapper;

    /**
     * Builds this {@link FileConfiguration} without any configuration file.
     * <p>
     * In order to save changes you will have to use one of these methods before:<br>
     * 	- {@link #setConfigurationFile(File)}<br>
     * 	- {@link #setConfigurationFile(String)}<br>
     * Or set the file when saving changes with {@link #save(File)}
     */
    public YamlFile() {}

    /**
     * Builds this {@link FileConfiguration} with the file specified by path.
     *
     * @param path location for the configuration file
     * @throws IllegalArgumentException if path is null or is a directory.
     * <br>Note that if <code>IllegalArgumentException</code> is thrown then this
     * configuration file will be <b>null</b>.
     */
    public YamlFile(String path) throws IllegalArgumentException {
        setConfigurationFile(path);
    }

    /**
     * Builds this {@link FileConfiguration} with a source file.
     *
     * @param file the configuration file
     * @throws IllegalArgumentException if file is null or is a directory.
     * <br>Note that if <code>IllegalArgumentException</code> is thrown then this
     * configuration file will be <b>null</b>.
     */
    public YamlFile(File file) throws IllegalArgumentException {
        setConfigurationFile(file);
    }

    /**
     * Saves this {@link FileConfiguration} to the configuration file location.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an
     * exception will be thrown.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     * <p>
     * Note that this method will not save comments,
     * if needed use {@link #saveWithComments()} instead.
     *
     * @throws IOException if it hasn't been possible to save configuration file
     * @see #saveWithComments()
     */
    public void save() throws IOException {
        Validate.notNull(configFile, "This configuration file is null!");
        save(configFile);
    }

    /**
     * Saves this {@link FileConfiguration} to the configuration file location with comments.
     * <p>
     * <b>IMPORTANT: Use {@link #save()} if configuration file has no comments or don't need it
     * to improve time efficiency.</b>
     * <p>
     * Comments copied will be those loaded with {@link #loadWithComments()} and those added
     * with {@link #setComment(String, String)} or {@link #setComment(String, String, CommentType)}.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @throws IOException if it hasn't been possible to save configuration file
     * @see #save()
     */
    public void saveWithComments() throws IOException {
        Validate.notNull(configFile, "The configuration file is null!");
        write(configFile, saveToStringWithComments());
    }

    /**
     * Saves this {@link FileConfiguration} to a string, comments included, and returns it.
     *
     * @return String containing this configuration with comments.
     * @throws IOException if it hasn't been possible to save configuration file
     * @see #saveToString
     */
    public String saveToStringWithComments() throws IOException {
        return new CommentDumper(options(), parseComments(), new StringReader(saveToString())).dump();
    }

    /**
     * Parse comments from the current file configuration.
     *
     * @return a comment mapper with comments parsed
     * @throws IOException if it hasn't been possible to read the configuration file
     */
    private CommentMapper parseComments() throws IOException {
        if (commentMapper == null) {
            String contents = fileToString();
            if (contents != null) {
                commentMapper = new CommentParser(options(), new StringReader(contents));
                ((CommentParser) commentMapper).parse();
            } else {
                commentMapper = new CommentMapper(options());
            }
        }
        return commentMapper;
    }

    /**
     * Adds a comment to the section or value selected by path.
     * Comment will be indented automatically.
     * Multi-line comments can be provided using \n character.
     *
     * @param path path of desired section or value
     * @param comment the comment to add, # symbol is not needed
     * @param type either above (block) or side
     */
    @Override
    public void setComment(String path, String comment, CommentType type) {
        if (commentMapper == null) {
            commentMapper = new CommentMapper(options());
        }
        commentMapper.setComment(path, comment, type);
    }

    /**
     * Retrieve the comment of the section or value selected by path.
     *
     * @param path path of desired section or value
     * @param type either above (block) or side
     * @return the comment of the section or value selected by path,
     * or null if that path does not have any comment of this type
     */
    @Override
    public String getComment(String path, CommentType type) {
        return commentMapper != null ? commentMapper.getComment(path, type) : null;
    }

    /**
     * Loads configurations from this configuration file.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     * <p>
     * Note that this method will not load comments of original configuration file,
     * if needed use {@link #loadWithComments()} instead.
     *
     * @throws IOException if it hasn't been possible to load file
     * @throws InvalidConfigurationException if there has been an error while parsing configuration file
     * @throws FileNotFoundException if configuration file is not found
     */
    public void load() throws InvalidConfigurationException, IOException {
        Validate.notNull(configFile, "This configuration file is null!");
        load(configFile);
    }

    /**
     * Loads configurations from this configuration file including comments.
     * <p>
     * <b>IMPORTANT: Use {@link #load()} if configuration file has no comments or don't need it
     * to improve time efficiency.</b>
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @throws IOException if it hasn't been possible to load file
     * @throws InvalidConfigurationException if there has been an error while parsing configuration file
     * @throws FileNotFoundException if configuration file is not found
     */
    public void loadWithComments() throws InvalidConfigurationException, IOException {
        load();
        parseComments();
    }

    /**
     * If this configuration file does not exist then it is created along with missing parent directories.
     * <p>
     * Otherwise loads configurations from this configuration file.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @throws IOException if it hasn't been possible to load file
     * @throws InvalidConfigurationException if there has been an error while parsing configuration file
     * @throws FileNotFoundException if configuration file is not found
     * @see #createOrLoadWithComments()
     */
    public void createOrLoad() throws IOException, InvalidConfigurationException {
        createNewFile(false);
        load();
    }

    /**
     * If this configuration file does not exist then it is created along with missing parent directories.
     * <p>
     * Otherwise loads configurations from this configuration file, including comments.
     * <p>
     * This method will use the {@link #options()} {@link FileConfigurationOptions#charset() charset} encoding,
     * which defaults to UTF8.
     *
     * @throws IOException if it hasn't been possible to load file
     * @throws InvalidConfigurationException if there has been an error while parsing configuration file
     * @throws FileNotFoundException if configuration file is not found
     * @see #createOrLoad()
     */
    public void createOrLoadWithComments() throws IOException, InvalidConfigurationException {
        createNewFile(false);
        loadWithComments();
    }

    /**
     * Tests whether this configuration file exists.
     *
     * @return <code>true</code> if and only if this configuration file exists;
     * 		   <code>false</code> otherwise
     */
    public boolean exists() {
        Validate.notNull(configFile, "This configuration file is null!");
        return configFile.exists();
    }

    /**
     * Creates a new empty file atomically for this configuration file.
     * <p>
     * Parent directories will be created if they do not exist.
     *
     * @param overwrite indicates if file must be overwritten if it already exists.
     * Note that if overwrite is set to false and there is already a file with that path file
     * will not be created and no exception is thrown.
     * @throws IOException if I/O error occurs creating the configuration file
     */
    public void createNewFile(boolean overwrite) throws IOException {
        Validate.notNull(configFile, "This configuration file is null!");
        if (overwrite || !configFile.exists()) {
            try {
                File parents = configFile.getParentFile();
                if (parents != null)
                    parents.mkdirs();
                configFile.createNewFile();
            } catch (SecurityException e) {
                throw new IOException(e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * Deletes this configuration file from disk.
     *
     * @throws IOException if file cannot be deleted
     */
    public void deleteFile() throws IOException {
        Validate.notNull(configFile, "This configuration file is null!");
        if (!configFile.delete()) {
            throw new IOException("Failed to delete " + configFile);
        }
    }

    /**
     * Returns the size of this configuration file.
     *
     * @return the size, in bytes, of this configuration file.
     */
    public long getSize() {
        return configFile.length();
    }

    /**
     * Returns the absolute pathname string of this configuration file.
     *
     * @return the absolute path where configuration file is located.
     */
    public String getFilePath() {
        Validate.notNull(configFile, "This configuration file is null!");
        return configFile.getAbsolutePath();
    }

    /**
     * Returns this configuration file where data is located.
     *
     * @return the configuration file where this {@link FileConfiguration} writes.
     */
    public File getConfigurationFile() {
        return configFile;
    }

    /**
     * Rebuilds this {@link FileConfiguration} with the file specified by path.
     *
     * @param path location for the configuration file
     * @throws IllegalArgumentException if path is null or is a directory.
     * <br>Note that if <code>IllegalArgumentException</code> is thrown then this
     * configuration file will be <b>null</b>.
     */
    public void setConfigurationFile(String path) throws IllegalArgumentException {
        Validate.notNull(path, "Path cannot be null.");
        configFile = new File(path);
        if (configFile.isDirectory()) {
            configFile = null;
            throw new IllegalArgumentException(configFile.getName() + " is a directory!");
        }
    }

    /**
     * Rebuilds this {@link FileConfiguration} with a source file.
     *
     * @param file the configuration file
     * @throws IllegalArgumentException if file is null or is a directory.
     * <br>Note that if <code>IllegalArgumentException</code> is thrown then this
     * configuration file will be <b>null</b>.
     */
    public void setConfigurationFile(File file) throws IllegalArgumentException {
        Validate.notNull(file, "File cannot be null.");
        configFile = file;
        if (configFile.isDirectory()) {
            configFile = null;
            throw new IllegalArgumentException(configFile.getName() + " is a directory!");
        }
    }

    /**
     * Copy this configuration file to another path, without deleting configuration file.
     * If there is already a file on the other path it will be overwritten.
     *
     * @param path the location of the new file, including name (mustn't be a directory)
     * @return the new copied file
     * @throws FileNotFoundException if configuration file is not found as source to copy
     * @throws IllegalArgumentException if path is a directory or it is null
     * @throws IOException if there I/O error occurs copying file
     */
    public File copyTo(final String path) throws FileNotFoundException, IllegalArgumentException, IOException {
        Validate.notNull(path, "Path cannot be null.");
        File copy = new File(path);
        copyTo(copy);
        return copy;
    }

    /**
     * Copy this configuration file to another file, without deleting configuration file.
     *
     * @param file destination file (mustn't be a directory)
     * @throws FileNotFoundException if configuration file is not found as source to copy
     * @throws IllegalArgumentException if path is a directory or it is null
     * @throws IOException if there I/O error occurs copying file
     */
    public void copyTo(File file) throws FileNotFoundException, IllegalArgumentException, IOException {
        Validate.notNull(configFile, "This configuration file is null!");
        if (!configFile.exists()) {
            throw new FileNotFoundException(configFile.getName() + " is not found in " + configFile.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is a directory!");
        }
        try (OutputStream fos = Files.newOutputStream(file.toPath())) {
            Files.copy(configFile.toPath(), fos);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Returns a representation of the already saved configuration file.
     *
     * @return the configuration file disk contents
     * @throws IOException if configuration file cannot be read
     */
    public String fileToString() throws IOException {
        if (configFile == null) {
            return null;
        }
        return new String(Files.readAllBytes(configFile.toPath()));
    }

    /**
     * Returns a representation of this configuration file.
     *
     * @return a representation of this configuration file
     * If something goes wrong then this string is an error message.
     */
    @Override
    public String toString() {
        try {
            return commentMapper == null ? saveToString() : saveToStringWithComments();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}

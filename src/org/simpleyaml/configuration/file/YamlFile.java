package org.simpleyaml.configuration.file;

import org.simpleyaml.configuration.comments.*;
import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
     * This method will save using the system default encoding, or possibly
     * using UTF8.
     * <p>
     * Note that this method will not copy the comments of original configuration file,
     * use {@link #saveWithComments()} instead.
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
     * <b>IMPORTANT: Use {@link #save()} if configuration file has not comments or don't need it
     * to improve time efficiency.</b>
     * <p>
     * This method will copy the comments from the configuration file if exists to the saved new file.<br>
     * <b>Note</b> that comments next to a value will not be copied, to be copied the comment must have character
     * <code>'#'</code> as first character, ignoring indentations and blank spaces.
     * <p>
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten.
     * <p>
     * This method will save using the system default encoding, or possibly
     * using UTF8.
	 *
	 * @throws IOException if it hasn't been possible to save configuration file
     * @see #save()
     */
	public void saveWithComments() throws IOException {
		Validate.notNull(configFile, "This configuration file is null!");

		if (configFile.getParentFile() != null) {
			configFile.getParentFile().mkdirs();
		}

		// Write file with comments
		try (Writer writer = new OutputStreamWriter(
				new FileOutputStream(configFile),
				UTF8_OVERRIDE && !UTF_BIG ? StandardCharsets.UTF_8 : Charset.defaultCharset()
		)) {
			writer.write(new CommentDumper(parseComments(), new StringReader(saveToString())).dump());
		}
	}

	/**
	 * Parse comments from the current file configuration.
	 *
	 * @return a comment mapper with comments parsed
	 * @throws IOException if it hasn't been possible to read the configuration file
	 */
	private CommentMapper parseComments() throws IOException {
		if (commentMapper == null) {
			commentMapper = new CommentParser(new StringReader(fileToString()));
			((CommentParser) commentMapper).parse();
		}
		return commentMapper;
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
	 * Loads configurations from this configuration file.
	 *
	 * @throws IOException if it hasn't been possible to load file
	 * @throws InvalidConfigurationException if there has been an error while parsing configuration file
	 * @throws FileNotFoundException if configuration file is not found
	 */
	public void load() throws InvalidConfigurationException, IOException {
		Validate.notNull(configFile, "This configuration file is null!");
		options().copyDefaults(true);
		options().copyHeader(true);
		load(configFile);
	}

	/**
	 * Loads configurations from this configuration file including comments.
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
	 * Furthermore there will be created the parent directories if there not exists.
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
     * Removes the specified path if it exists.
     * The entry will be removed, either a value or an entire section.
     * 
     * @param path Path of the object to remove.
     */
    public void remove(String path) {
    	set(path, null);
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
	public void addComment(String path, String comment, CommentType type) {
		if (commentMapper == null) {
			commentMapper = new CommentMapper();
		}
		commentMapper.addComment(path, comment, type);
	}

	/**
	 * Returns a representation of the configuration file.
	 *
	 * @return the configuration file contents
	 * @throws IOException if configuration file cannot be read
	 */
    public String fileToString() throws IOException {
    	return new String(Files.readAllBytes(getConfigurationFile().toPath()));
	}

	/**
	 * Returns a representation of the configuration file.
	 *
	 * @return the configuration file contents.
	 * If something goes wrong then this string is an error message.
	 */
	@Override
    public String toString() {
		try {
			return fileToString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}

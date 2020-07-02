package org.simpleyaml.configuration.file;

import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * An extension of {@link YamlConfiguration} which saves all data in Yaml to a configuration file.
 * Note that this implementation is not synchronized.
 * 
 * @author Carlos Lázaro Costa
 */
public class YamlFile extends YamlConfiguration {
	
	/** File where data will be saved for this configuration */
	private File configFile;
	
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
	 * @throws IOException if it hasn't been possible to save configuration file
     * @see #save()
     */
	public void saveWithComments() throws IOException {
		Validate.notNull(configFile, "This configuration file is null!");

		if (configFile.getParentFile() != null) {
			configFile.getParentFile().mkdirs();
		}

		BufferedReader r_from = new BufferedReader(new StringReader(fileToString()));
		BufferedReader r_new = new BufferedReader(new StringReader(saveToString()));

		String line_from = r_from.readLine();
		String line_new = r_new.readLine();

		// Map comments <PossibleKey, Queue<Comment>>
		Map<String, Queue<String>> commentMap = new HashMap<>();

		while (line_from != null) {
			String trim = line_from.trim();
			if (trim.isEmpty() || trim.startsWith("#")) { // Save comment
				StringBuilder comment = new StringBuilder(line_from.length());
				comment.append(line_from).append('\n');
				line_from = r_from.readLine();
				while (line_from != null && ((trim = line_from.trim()).isEmpty() || trim.startsWith("#"))) {
					comment.append(line_from).append('\n');
					line_from = r_from.readLine();
				}
				// Save key (null for end of file) and its comment
				insertComment(commentMap, substring(line_from, ':'), comment);
			} else {
				line_from = r_from.readLine();
			}
		}

		r_from.close();

		StringBuilder res = new StringBuilder();

		// Copy new file and comments if key is found
		while (line_new != null) {
			String comment = getComment(commentMap, substring(line_new, ':'));
			if (comment != null) {
				res.append(comment);
			}
			if (!line_new.trim().startsWith("#")) {
				res.append(line_new).append('\n');
			}
			line_new = r_new.readLine();
		}

		r_new.close();

		// Copy end of file comment, if found
		String comment = getComment(commentMap, null);
		if (comment != null) {
			res.append(comment);
		}

		// Write file with comments
		try (Writer writer = new OutputStreamWriter(
				new FileOutputStream(configFile),
				UTF8_OVERRIDE && !UTF_BIG ? StandardCharsets.UTF_8 : Charset.defaultCharset()
		)) {
			writer.write(res.toString());
		}
	}

	private void insertComment(Map<String, Queue<String>> commentMap, String key, StringBuilder commentBuilder) {
		String comment = commentBuilder.toString();
		if (key != null) {
			key = key.trim();
		}
		Queue<String> commentsList = commentMap.get(key);
		if (commentsList != null) {
			commentsList.add(comment);
		} else {
			commentMap.put(key, new LinkedList<>(Collections.singletonList(comment)));
		}
	}

	private String getComment(Map<String, Queue<String>> commentMap, String key) {
		if (key != null) {
			key = key.trim();
		}
		Queue<String> comments = commentMap.get(key);
		return comments != null ? comments.poll() : null;
	}
	
	/**
	 * Returns the substring of s that starts at position 0 and ends at the position
	 * of character c (excluded).
	 * @param s the String to search
	 * @param c the end character of substring (excluded)
	 * @return the substring of <b>s</b> that starts at position 0 and ends at the position
	 * of character <b>c</b> (excluded). If <b>s</b> is <b>null</b>, returns <b>null</b>.
	 * If <b>c</b> is not contained in <b>s</b>, returns <b>s</b>.
	 */
	private String substring(String s, char c) {
		if (s != null) {
			int n = s.length();
			StringBuilder aux = new StringBuilder(n);
			for (int i = 0; i < n; ++i) {
				char charAt = s.charAt(i);
				if (charAt != c)
					aux.append(charAt);
				else break;
			}
			return aux.toString();
		}
		return null;
	}
	
	/**
	 * Copy this configuration file to another path, without deleting configuration file.
	 * If there is already a file on the other path it will be overwritten.
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
	 * @param file destination file (mustn't be a directory)
	 * @return the new copied file
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
	 * @throws IOException if it hasn't been possible to load file
	 * @throws InvalidConfigurationException if has been an error while parsing configuration file
	 * @throws FileNotFoundException if configuration file is not found
	 */
	public void load() throws InvalidConfigurationException, IOException {
		Validate.notNull(configFile, "This configuration file is null!");
		options().copyDefaults(true);
		options().copyHeader(true);
		load(configFile);
	}
	
	/**
	 * Tests whether this configuration file exists.
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
	 * @throws IOException if file cannot be deleted
	 */
	public void deleteFile() throws IOException {
		Validate.notNull(configFile, "This configuration file is null!");
		if (!configFile.delete())
			throw new IOException("Failed to delete " + configFile);
	}
	
	/**
	 * Returns the size of this configuration file.
	 * @return the size, in bytes, of this configuration file.
	 */
	public long getSize() {
		return configFile.length();
	}
	
	/**
	 * Returns the absolute pathname string of this configuration file. 
	 * @return the absolute path where configuration file is located.
	 */
	public String getFilePath() {
		Validate.notNull(configFile, "This configuration file is null!");
		return configFile.getAbsolutePath();
	}
	
	/**
	 * Returns this configuration file where data is located.
	 * @return the configuration file where this {@link FileConfiguration} writes.
	 */
	public File getConfigurationFile() {
		return configFile;
	}
	
	/**
	 * Rebuilds this {@link FileConfiguration} with the file specified by path.
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

    public String fileToString() throws IOException {
    	return new String(Files.readAllBytes(getConfigurationFile().toPath()));
	}

	@Override
    public String toString() {
		try {
			return fileToString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}

package org.simpleyaml.configuration.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.simpleyaml.exceptions.InvalidConfigurationException;
import org.simpleyaml.utils.Validate;
import com.google.common.io.Files;

/**
 * An extension of {@link YamlConfiguration} which saves all data in Yaml to
 * a configuration file.
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
		File old = File.createTempFile("tmp\\oldFileData", null);
		if (configFile.exists())
			copyTo(old);
		save(configFile);
		if (old != null) {
			copyComments(old);
			old.delete();
		}
	}

	/**
	 * Insert all comments on <b>from</b> file to configuration file.
	 * @param from source file
	 * @throws IOException if I/O error occurs while copying comments
	 */
	private void copyComments(File from) throws IOException {
		BufferedReader r_from = new BufferedReader(new FileReader(from));
		BufferedReader r_new = new BufferedReader(new FileReader(configFile));
		
		StringBuilder res = new StringBuilder();
		String line_from = r_from.readLine();
		String line_new = r_new.readLine();
		
		// Map comments <PossibleKey, Comment>
		Map<String, String> comments = new HashMap<String, String>();
		
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
				// Save Key (null for end of file) and its comment
				comments.put(substring(line_from, ':'), comment.toString());
			}
			else
				line_from = r_from.readLine();
		}
		
		// Restarts reader
		r_from.close();
		r_from = new BufferedReader(new FileReader(from));
		
		// Copy new file and comments if key is found
		int commentsWritten = 0, n = comments.size();
		while (line_new != null) {
			if (commentsWritten < n) {
				String comment = comments.get(substring(line_new, ':'));
				if (comment != null) {
					res.append(comment);
					commentsWritten++;
				}
			}
			if (!line_new.trim().startsWith("#"))
				res.append(line_new).append('\n');
			line_new = r_new.readLine();
		}
		
		// Copy end of file comment, if found
		String comment = comments.get(null);
		if (comment != null)
			res.append(comment);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(configFile, false));
		out.write(res.toString());
		out.close();
		r_from.close();
		r_new.close();
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
		if (!configFile.exists())
			throw new FileNotFoundException(configFile.getName() + " is not found in " + configFile.getAbsolutePath());
		else {
			if (!file.isDirectory())
				Files.copy(configFile, file);
			else
				throw new IllegalArgumentException(file.getAbsolutePath() + " is a directory!");
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
}
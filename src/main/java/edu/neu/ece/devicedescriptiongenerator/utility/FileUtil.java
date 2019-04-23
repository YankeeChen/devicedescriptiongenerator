package edu.neu.ece.devicedescriptiongenerator.utility;

import java.io.File;

/**
 * File utility class that contains methods on file operations.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public final class FileUtil {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private FileUtil() {
	}

	/**
	 * Load file from file path.
	 * 
	 * @param filePath
	 *            File URL in string.
	 * @return A File object.
	 */
	public static File loadFile(String filePath) {
		File f = new File(filePath);
		if (!f.isFile() || !f.canRead())
			throw new IllegalStateException("Cannot load file from " + filePath);
		return f;
	}

	/**
	 * Create a file from file path. Remove existing file with the file path if any.
	 * 
	 * @param filePath
	 *            File URL in string.
	 * @return A File object
	 */
	public static File createFile(String filePath) {
		File f = new File(filePath);
		if (f.isDirectory())
			throw new IllegalStateException("Cannot create file from " + filePath);
		if (f.exists()) {
			f.delete();
		}
		return f;
	}
}

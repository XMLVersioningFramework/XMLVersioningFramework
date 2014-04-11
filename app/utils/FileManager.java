package utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * @deprecated use org.apache.commons.io.FileUtils class instead
 */
@Deprecated
public class FileManager {
	public FileManager() {
	}

	/**
	 * 
	 * @param fullPath
	 * @deprecated use org.apache.commons.io.FileUtils.forceMkdir(File
	 *             directory) instead
	 */
	public static void createFolder(String fullPath) {

		File directory = new File(fullPath);

		if (!directory.exists()) {
			try {
				FileUtils.forceMkdir(directory);
			} catch (IOException e) {
				System.out.println("Failed to create the directory: "
						+ fullPath);
				e.printStackTrace();
			}
			boolean result = directory.exists();
			if (result) {
				System.out.println("Dir: " + fullPath + " created");
			} else {
				System.out.println("Failed to create the directory, check above for details.");
			}
		} else
			System.out.println("Dir: " + fullPath
					+ " already exists, skipped creation");
	}

	/**
	 * @deprecated use org.apache.commons.io.FileUtils class instead
	 */
	@Deprecated
	public static File createFile(String fileContent, String fileName) {
		String filePath = "./";
		return createFile(fileContent, fileName, filePath);
	}

	/**
	 * @deprecated use org.apache.commons.io.FileUtils class instead
	 */
	@Deprecated
	public static File createFile(String data, String fileName, String filePath) {
		String fileURL = filePath + fileName;
		System.out.println(fileURL);
		File file = new File(fileURL);
		try {
			FileUtils.writeStringToFile(file, data);
		} catch (IOException e) {
			System.err.println("Couldn't write to the file :" + fileURL);
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * @deprecated use org.apache.commons.io.FileUtils class instead
	 */
	@Deprecated
	public static String readFileToString(String fileURL) {
		File file = new File(fileURL);

		String fileContents = "";
		try {
			fileContents += FileUtils.readFileToString(file);
		} catch (IOException e) {
			System.err.println("Failed read from file, file url: " + fileURL);
			e.printStackTrace();
		}
		return fileContents;
	}

	/**
	 * @deprecated use org.apache.commons.io.FileUtils class instead
	 */
	@Deprecated
	public static boolean removeFolder(String fullPath) throws IOException {
		File directory = new File(fullPath);
		if (directory.exists()) {
			if (directory.isDirectory()) {
				FileUtils.deleteDirectory(directory);
			}
		}
		return false;
	}
}

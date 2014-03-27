package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

/**
 * @deprecated use org.apache.commons.io.FileUtils class instead
 */
@Deprecated
public class FileManager {
	public FileManager() {
	}

	public static void createFolder(String fullPath) {

		File theDir = new File(fullPath);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();

			if (result) {
				System.out.println("Dir: " + fullPath + " created");
			}
		} else
			System.out.println("Dir: " + fullPath
					+ " already exists, skipped creation");
	}

	public static void createFile(String fileContent, String fileName) {
		String filePath = "./";
		createFile(fileContent, fileName, filePath);
	}

	public static void createFile(String fileContent, String fileName,
			String filePath) {
		PrintWriter out;
		try {
			out = new PrintWriter(filePath + fileName);
			out.write(fileContent);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			System.err
					.println("Couldn't write to the file, reason File Not found:");
			e.printStackTrace();
		}

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

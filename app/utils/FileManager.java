package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

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

	public static String readFileToString(String fileURL) {
		String fileContents = "";
		try {
			Scanner tempFile = new Scanner(new File(fileURL));
			while (tempFile.hasNextLine()) {
				fileContents += tempFile.nextLine();
			}
			tempFile.close();
		} catch (FileNotFoundException e) {
			System.err
					.println("Failed to find a file in the working dir, file url: "
							+ fileURL);
			e.printStackTrace();
		}
		return fileContents;
	}

}

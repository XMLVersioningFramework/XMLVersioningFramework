package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class BackendHandlerInterface {
	public static final String GIT = "git";
	public static final String XCHRONICLER = "XChronicler";
	public static final String SIRIX = "sirix";
	public static final String rootBackendFolder = "./backends/";
	

	public abstract Object getRepository();

	public abstract boolean init();

	public abstract boolean removeExistingRepository();

	public abstract boolean commit(String url, String content, String message,
			User user);

	public abstract String commitAFile(TempFile tf);

	public abstract TempFile getFile(String url);
	
	public abstract Logs getLog();
	
	public abstract RepositoryRevision checkout(String revision);

	public abstract ArrayList<RepositoryFile> getWorkingDirFiles();

	public abstract String getRepositoryPath();

	public abstract RepositoryRevision getRepositoryHEAD();

	
	public abstract ArrayList<String> getDiff(int relativeRevisionId);
	
	public abstract int getVersionId();
	
	
	/**
	 * removes the relative path to the working directory and replaces with '.'
	 * 
	 * @param fileURL
	 * @return
	 */
	public String getStrippedFileURL(String fileURL) {
		return fileURL.replaceFirst(this.getRepositoryPath(), ".");
	}

	public static String runCommand(String s) {
		String returnMsg = "";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(s);
			p.waitFor();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				returnMsg += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnMsg;
	}

}

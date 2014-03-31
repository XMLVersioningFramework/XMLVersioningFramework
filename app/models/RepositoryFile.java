package models;

/**
 * Encapsulates a file from a repository. It contains 2 attributes, its URL
 * and its Content, both are strings.
 * 
 */
public class RepositoryFile {
	private String fileURL;
	private String fileContent;

	public RepositoryFile(String fileURL) {
		this.fileURL = fileURL;
	}

	public RepositoryFile(String fileURL, String fileContent) {
		this.fileURL = fileURL;
		this.fileContent = fileContent;
	}

	/**
	 * @param fileURL
	 *            the fileURL to set
	 */
	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}

	/**
	 * @param fileContent
	 *            the fileContent to set
	 */
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	/**
	 * @return the fileURL
	 */
	public String getFileURL() {
		return fileURL;
	}

	/**
	 * @return the fileContent
	 */
	public String getFileContent() {
		return fileContent;
	}

}

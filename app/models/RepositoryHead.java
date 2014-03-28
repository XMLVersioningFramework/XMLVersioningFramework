package models;

import java.util.ArrayList;

import play.libs.Json;
import utils.JSONConstants;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class RepositoryHead {
	private long elapsedTime;

	private ArrayList<RepositoryFile> repositoryFiles;

	private String lastCommit, lastCommitMessage, lastCommitAuthor;

	/**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @return the lastCommit
	 */
	public String getLastCommit() {
		return lastCommit;
	}

	/**
	 * @return the lastCommitAuthor
	 */
	public String getLastCommitAuthor() {
		return lastCommitAuthor;
	}

	/**
	 * @return the lastCommitMessage
	 */
	public String getLastCommitMessage() {
		return lastCommitMessage;
	}

	/**
	 * @return the repositoryFiles
	 */
	public ArrayList<RepositoryFile> getRepositoryFiles() {
		return repositoryFiles;
	}

	/**
	 * @param elapsedTime
	 *            the elapsedTime to set
	 */
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	/**
	 * @param lastCommit
	 *            the lastCommit to set
	 */
	public void setLastCommit(String lastCommit) {
		this.lastCommit = lastCommit;
	}

	/**
	 * @param lastCommitAuthor
	 *            the lastCommitAuthor to set
	 */
	public void setLastCommitAuthor(String lastCommitAuthor) {
		this.lastCommitAuthor = lastCommitAuthor;
	}

	/**
	 * @param lastCommitMessage
	 *            the lastCommitMessage to set
	 */
	public void setLastCommitMessage(String lastCommitMessage) {
		this.lastCommitMessage = lastCommitMessage;
	}

	/**
	 * @param repositoryFiles
	 *            the repositoryFiles to set
	 */
	public void setRepositoryFiles(ArrayList<RepositoryFile> repositoryFiles) {
		this.repositoryFiles = repositoryFiles;
	}

	/**
	 * Writes the content of repository Head to JSON
	 * 
	 * @param head
	 * @return
	 */
	public ObjectNode toJSON() {
		ObjectNode headJSON = Json.newObject();
		for (RepositoryFile repoFile : this.getRepositoryFiles()) {
			ObjectNode fileObjectNode = headJSON.putArray(JSONConstants.FILES)
					.addObject();

			fileObjectNode.put(JSONConstants.FILE_URL, repoFile.getFileURL());
			fileObjectNode.put(JSONConstants.FILE_CONTENT,
					repoFile.getFileContent());
		}
		headJSON.put(JSONConstants.ELAPSED_TIME, this.getElapsedTime());
		headJSON.put(JSONConstants.LAST_COMMIT, this.getLastCommit());
		headJSON.put(JSONConstants.LAST_COMMIT_MESSAGE,
				this.getLastCommitMessage());
		headJSON.put(JSONConstants.LAST_COMMIT_AUTHOR,
				this.getLastCommitAuthor());
		return headJSON;
	}

}

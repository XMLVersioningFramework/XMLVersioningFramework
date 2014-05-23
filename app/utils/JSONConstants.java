/**
 * 
 */
package utils;


/**
 * Util class to help and normalize with json requests
 */
public final class JSONConstants {

	// Files
	public static final String FILES = "files";
	public static final String FILE_CONTENT = "fileContent";
	public static final String FILE_URL = "fileURL";

	// Commits
	public static final String LAST_COMMIT = "lastCommit";
	public static final String LAST_COMMIT_AUTHOR = "lastCommitAuthor";
	public static final String LAST_COMMIT_MESSAGE = "lastCommitMessage";
	public static final String COMMIT_FILE_URL = "commitFileUrl";
	

	public static final String USER = "user";
	public static final String MESSAGE = "message";
	
	public static final String REVISION_ID = "revisionID";
	
	
	public static final String RELATIVE_VERSION="relativeVersion";
	 
	/**
	 * TODO: IS this equal to {@link #FILE_CONTENT}? if so, unify here, in the
	 * caller and in the js
	 */
	public static final String CONTENT = "content";

	/**
	 * TODO: IS this equal to {@link #FILE_URL}? if so, unify here, in the
	 * caller and in the js
	 */
	public static final String URL = "url";

	public static final String BACKEND = "backend";
	public static final String ELAPSED_TIME = "elapsedTime";
	/**
	 * TODO: IS this equal to {@link #ELAPSED_TIME}? if so, unify here, in the
	 * caller and in the js
	 */
	public static final String TIME = "time";

	// Answer status
	public static final String ANSWER = "answer";
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	


	// PRIVATE //

	/**
	 * The caller references the constants using <tt>Constants.EMPTY_STRING</tt>
	 * , and so on. Thus, the caller should be prevented from constructing
	 * objects of this class, by declaring this private constructor.
	 */
	private JSONConstants() {
		// this prevents even the native class from calling this ctor as well :
		throw new AssertionError();
	}
}

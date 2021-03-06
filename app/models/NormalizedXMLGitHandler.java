package models;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Encapsulates the canonicalization of XML through Git
 * Heavily depends on GitHandler for the implementation
 *
 */
public class NormalizedXMLGitHandler extends BackendHandlerInterface {
	BackendHandlerInterface gitHandler = GitHandler.getInstance(); 

	private NormalizedXMLGitHandler() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final BackendHandlerInterface INSTANCE = new NormalizedXMLGitHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public Object getRepository() {
		//TODO: Stub, needs revision
		return gitHandler.getRepository();
	}

	@Override
	public boolean init() {
		//TODO: Stub, needs revision
		return gitHandler.init();
	}

	@Override
	public boolean removeExistingRepository() {
		//TODO: Stub, needs revision
		return gitHandler.removeExistingRepository();
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		//TODO: Stub, needs revision
		return gitHandler.commit(url, content, message, user);
	}

	@Override
	public String commitAFile(TempFile tf) {
		//TODO: Stub, needs revision
		return gitHandler.commitAFile(tf);
	}

	@Override
	public TempFile getFile(String url) {
		//TODO: Stub, needs revision
		return gitHandler.getFile(url);
	}

	@Override
	public Logs getLog() {
		//TODO: Stub, needs revision
		return gitHandler.getLog();
	}

	@Override
	public RepositoryRevision checkout(String revision) {
		//TODO: Stub, needs revision
		return gitHandler.checkout(revision);
	}

	@Override
	public ArrayList<RepositoryFile> getWorkingDirFiles() {
		//TODO: Stub, needs revision
		return gitHandler.getWorkingDirFiles();
	}

	@Override
	public String getRepositoryPath() {
		//TODO: Stub, needs revision
		return gitHandler.getRepositoryPath();
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		//TODO: Stub, needs revision
		return gitHandler.getRepositoryHEAD();
	}



	@Override
	public HashSet<String> getDiff(int relativeRevisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean revert(int relativeRevision) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commit(String url, String content, String message,
			User user, int relativeVersion) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public long getSize() {
		return gitHandler.getSize();
	}

}

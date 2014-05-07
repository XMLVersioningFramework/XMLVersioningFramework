package models;

import java.util.ArrayList;

/**
 * Encapsulates the canonicalization of XML through Git
 * Heavily depends on GitHandler for the implementation
 *
 */
public class CanonicalXMLGitHandler extends BackendHandlerInterface {

	private CanonicalXMLGitHandler() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final BackendHandlerInterface INSTANCE = new CanonicalXMLGitHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeExistingRepository() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String commitAFile(TempFile tf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TempFile getFile(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Logs getLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepositoryRevision checkout(String revision) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<RepositoryFile> getWorkingDirFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepositoryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		// TODO Auto-generated method stub
		return null;
	}
}

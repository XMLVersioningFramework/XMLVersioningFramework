package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;

import utils.FileManager;

public class GitHandler extends BackendHandlerInterface {
	static final String BASE_URL = rootBackendFolder+"git/";
	public static final String REPOSITORY_URL = BASE_URL + "repo/";
	static Git gitRepository = null;
	private static GitHandler instance = null;

	private GitHandler() {

	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final GitHandler INSTANCE = new GitHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 
	 * @deprecated This method used the command line and is deprecated, use
	 *             init() instead.
	 */
	@Deprecated
	public static boolean initCommandLine() {
		// remove the current repo
		System.out.println("init");
		String remmove = "rm -rf " + REPOSITORY_URL;
		System.out.println(runCommand(remmove));
		String mkdir = "mkdir " + REPOSITORY_URL;
		System.out.println(mkdir);
		System.out.println(runCommand(mkdir));
		// init repo
		System.out.println(runCommand("git init " + REPOSITORY_URL));
		return true;
	}

	public boolean init() {
		return init(REPOSITORY_URL);
	}

	public static boolean init(String repositoryURL) {
		if (removeExistingRepository(repositoryURL)) {
			try {
				InitCommand initCommand = new InitCommand().setBare(false)
						.setDirectory(new File(repositoryURL));
				gitRepository = initCommand.call();

				System.out.println("Success initializing the repository");
				System.out.println("Repository directory: "
						+ gitRepository.getRepository().getDirectory()
								.getCanonicalPath());
			} catch (NullPointerException e) {
				System.err
						.println("Error creating the directory for repository:");
				e.printStackTrace();
				return false;
			} catch (GitAPIException e) {
				System.err.println("Error initializing the repository:");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				System.err
						.println("Error fetching the cannonical path of the repository:");
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * overloads removeExistingRepository(String repositoryURL) with default
	 * repository name
	 * 
	 * @return true if succeeded, false if not
	 */
	public static boolean removeExistingRepository() {
		if (removeExistingRepository(REPOSITORY_URL))
			return true;
		return false;
	}

	/**
	 * @param repositoryURL
	 *            the repository URL
	 * @return true if able to remove or repository inexistent; false if
	 *         existent but unable to remove.
	 */
	public static boolean removeExistingRepository(String repositoryURL) {
		final String gitRepositorySuffix = "/.git";
		File directory = new File(repositoryURL);
		File gitRepository = new File(repositoryURL + gitRepositorySuffix);
		/**
		 * if repository exists then remove it
		 */
		if (gitRepository.exists()) {
			try {
				FileUtils.deleteDirectory(gitRepository);
				FileUtils.cleanDirectory(directory);
			} catch (IOException e) {
				System.out.println("Failed to remove the repository: "
						+ repositoryURL);
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			/**
			 * else its not required to
			 */
			return true;
		}
	}

	/**
	 * Overloads the add(Repository repo, String filepattern, boolean update)
	 * method passing the update value as false and using the current git
	 * repository
	 * 
	 * @param repo
	 * @param filepattern
	 * @return
	 */
	public static boolean add(String filepattern) {
		if (GitHandler.getGitRepository() == null) {
			System.err.println("Have not init repo: " + filepattern);
			return false;
		}
		if (add(GitHandler.getGitRepository().getRepository(), filepattern,
				false))
			return true;
		return false;
	}

	/**
	 * Overloads the add(Repository repo, String filepattern, boolean update)
	 * method passing the update value as false
	 * 
	 * @param repo
	 * @param filepattern
	 * @return
	 */
	public static boolean add(Repository repo, String filepattern) {
		if (add(repo, filepattern, false))
			return true;
		return false;
	}

	/**
	 * Adds a file/dir to the repository for commit
	 * 
	 * @param repo
	 * @param filepattern
	 * @param update
	 * @return
	 */
	public static boolean add(Repository repo, String filepattern,
			boolean update) {
		try {
			AddCommand addCommand = new AddCommand(repo);
			addCommand.addFilepattern(filepattern);
			addCommand.setUpdate(update);

			try {
				addCommand.call();
			} catch (NoFilepatternException e) {
				System.err
						.println("Failed to add file, did not include a file pattern? File pattern received was: "
								+ filepattern);
				// e.printStackTrace();
				return false;
			} catch (GitAPIException e) {
				System.err.println("Fail to add the file to the repository:");
				// e.printStackTrace();
				return false;
			}
		} catch (JGitInternalException e) {
			System.err
					.println("Fail to add the file to the repository internal error");
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean commit(String message, String name, String email) {
		try {
			GitHandler.getGitRepository().commit().setAuthor(name, email)
					.setMessage(message).call();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NoMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (UnmergedPathsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ConcurrentRefUpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (WrongRepositoryStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (JGitInternalException e) {
			System.err
					.println("Fail to add the file to the repository internal error");
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param gitRepository
	 *            the gitRepository to set
	 */
	private static void setGitRepository(Git gitRepository) {
		GitHandler.gitRepository = gitRepository;
	}

	public Object getRepository() {
		return getGitRepository();
	}

	/**
	 * @return the gitRepository
	 */
	private static Git getGitRepository() {
		return gitRepository;
	}

	/**
	 * 
	 * @return The working directory path
	 * @throws IOException
	 */
	public static String getWorkingDirectoryPath() {
		return GitHandler.getGitRepository().getRepository().getWorkTree()
				.getPath();
	}

	public ArrayList<String> getWorkingDirFiles() {

		// TODO: check if working dir is up to date
		ArrayList<String> workingDirFiles = null;
		File workingDir;
		try {
			workingDir = GitHandler.getGitRepository().getRepository()
					.getWorkTree();
			workingDirFiles = getWorkingDirFilesPath(workingDir);
		} catch (NoWorkTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workingDirFiles;
	}

	// TODO: use commons.io.FileUtils to fetch those
	private static ArrayList<String> getWorkingDirFilesPath(File workingDir) {
		ArrayList<String> workingDirFiles = new ArrayList<String>();
		for (final File fileEntry : workingDir.listFiles()) {
			if (fileEntry.isDirectory()) {
				// TODO: go recursive
			} else {
				workingDirFiles.add(fileEntry.getPath());
			}
		}
		return workingDirFiles;
	}

	/**
	 * removes the relative path to the working directory and replaces with '.'
	 * 
	 * @param fileURL
	 * @return
	 */
	public static String stripFileURL(String fileURL) {
		return fileURL.replaceFirst(GitHandler.getWorkingDirectoryPath(), ".");
	}

	@Override
	public String commitAFile(TempFile tf) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public TempFile getFile(String url) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		
		
		buildFile(url, content);

		/**
		 * Add to repository
		 */
		while (!GitHandler.add(url)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("success adding file");

		/**
		 * Commit changes
		 */
		while (!GitHandler.commit(message, user.getName(), user.getEmail())) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Creates a File on the repository directory
	 * @param url
	 * @param content
	 */
	//TODO: Should be moved away from here
	private static void buildFile(String url, String content) {
		String fileName = url;
		String fileContent = content;
		String filePath = GitHandler.REPOSITORY_URL;
		FileManager.createFile(fileContent, fileName, filePath);
	}
}

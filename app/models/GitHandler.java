package models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

public class GitHandler extends BackendHandlerInterface {
	static final String BASE_URL = "./backends/git/";
	public static final String REPOSITORY_URL = BASE_URL + "repo/";
	static Git gitRepository = null;

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

	public static boolean init() {
		return init(REPOSITORY_URL);
	}

	public static boolean init(String repositoryURL) {
		try {
			InitCommand initCommand = new InitCommand().setBare(false)
					.setDirectory(new File(repositoryURL));
			gitRepository = initCommand.call();

			System.out.println("Success initializing the repository");
			System.out.println("Repository directory: "
					+ gitRepository.getRepository().getDirectory()
							.getCanonicalPath());
		} catch (NullPointerException e) {
			System.err.println("Error creating the directory for repository:");
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

	public static boolean commit(String message) {
		try {
			GitHandler.getGitRepository().commit().setMessage(message).call();
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

	/**
	 * @return the gitRepository
	 */
	public static Git getGitRepository() {
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

	public static ArrayList<String> getWorkingDirFiles() {

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
}

package models;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;

public class GitHandler extends BackendHandlerInterface {
	static final String BASE_URL = "./backends/git/";
	static final String REPOSITORY_URL = BASE_URL + "repo";
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
		if (add(getGitRepository().getRepository(), filepattern, false))
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
		AddCommand addCommand = new AddCommand(repo);
		addCommand.addFilepattern(filepattern);
		addCommand.setUpdate(update);

		try {
			addCommand.call();
		} catch (NoFilepatternException e) {
			System.err
					.println("Failed to add file, did not include a file pattern? File pattern received was: "
							+ filepattern);
			e.printStackTrace();
			return false;
		} catch (GitAPIException e) {
			System.err.println("Fail to add the file to the repository:");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean commit() {
		return false;
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
}

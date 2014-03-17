package models;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

//use JGIT
public class GitHandler extends BackendHandlerInterface {
	static final String BASE_URL = "./backends/git/";
	static final String REPO_URL = BASE_URL + "repo";

	/**
	 * 
	 * @deprecated This method used the command line and is deprecated, use
	 *             init() instead.
	 */
	@Deprecated
	public static boolean initCommandLine() {
		// remove the current repo
		System.out.println("init");
		String remmove = "rm -rf " + REPO_URL;
		System.out.println(runCommand(remmove));
		String mkdir = "mkdir " + REPO_URL;
		System.out.println(mkdir);
		System.out.println(runCommand(mkdir));
		// init repo
		System.out.println(runCommand("git init " + REPO_URL));
		return true;
	}

	public static boolean init() {
		return init(REPO_URL);
	}

	public static boolean init(String repoURL) {
		try {
			InitCommand initCommand = new InitCommand().setBare(false)
					.setDirectory(new File(repoURL));
			Git gitRepository = initCommand.call();
			
			System.out.println("Success initializing the repository");
			System.out.println("Repository directory: " + gitRepository.getRepository().getDirectory().getCanonicalPath());
		} catch (NullPointerException e) {
			System.err.println("Error creating the directory for repository:");
			e.printStackTrace();
			return false;
		} catch (GitAPIException e) {
			System.err.println("Error initializing the repository:");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("Error fetching the cannonical path of the repository:");
			e.printStackTrace();
		}
		return true;
	}

	/*public static TempFile getFile(String url){
	  
	 }*/
}

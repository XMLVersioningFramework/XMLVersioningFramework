package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import utils.FileManager;

public class GitHandler extends BackendHandlerInterface {
	static final String BASE_URL = rootBackendFolder + "git/";
	public static final String REPOSITORY_URL = BASE_URL + "repo/";
	static Git gitRepository = null;

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
		String remove = "rm -rf " + REPOSITORY_URL;
		System.out.println(runCommand(remove));
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
	public boolean removeExistingRepository() {
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
		if (GitHandler.gitRepository == null) {
			System.err.println("Have not init repo: " + filepattern);
			return false;
		}
		if (add(GitHandler.gitRepository.getRepository(), filepattern, false))
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

	public Logs getLog() {
		String returnString = "";
		Logs logs = new Logs();
		try {
			for (RevCommit element : gitRepository.log().call()) {
				logs.addLog(new Log(element.getName(), element.getFullMessage()));
			}
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logs;
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

	public Object getRepository() {
		return gitRepository;
	}

	/**
	 * 
	 * @return The working directory path
	 * @throws IOException
	 */
	public String getRepositoryPath() {
		return GitHandler.gitRepository.getRepository().getWorkTree().getPath();
	}

	public ArrayList<RepositoryFile> getWorkingDirFiles() {

		// TODO: check if working dir is up to date
		ArrayList<RepositoryFile> workingDirFiles = null;
		File workingDir;
		try {
			workingDir = GitHandler.gitRepository.getRepository().getWorkTree();
			workingDirFiles = getWorkingDirFilesPath(workingDir);
		} catch (NoWorkTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workingDirFiles;
	}

	// TODO: use commons.io.FileUtils to fetch those
	private static ArrayList<RepositoryFile> getWorkingDirFilesPath(
			File workingDir) {
		ArrayList<RepositoryFile> workingDirFiles = new ArrayList<RepositoryFile>();
		for (final File fileEntry : workingDir.listFiles()) {
			if (fileEntry.isDirectory()) {
				// TODO: go recursive
			} else {
				workingDirFiles.add(new RepositoryFile(fileEntry.getPath()));
			}
		}
		return workingDirFiles;
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

	/**
	 * Only prepared for a single file TODO: Needs to be able to receive more
	 * than one
	 */
	@Override
	public boolean commit(String url, String content, String message, User user) {

		buildFile(url, content);

		/**
		 * Add to repository
		 */
		if(!GitHandler.add(url)){
			System.out.println("Fail to add the file to the repository");
		}

		System.out.println("success adding file");

		/**
		 * Commit changes
		 */
		RevCommit commit;
		boolean committed = false;
		try {
			commit = GitHandler.gitRepository.commit()
					.setAuthor(user.getName(), user.getEmail())
					.setMessage(message).call();
			/**
			 * Is this really still necessary?
			 */
			int commitTime = Integer.MIN_VALUE;
			while (commitTime == Integer.MIN_VALUE) {
				/**
				 * wait for the commit to be successful
				 */
				Thread.sleep(100);
				commitTime = commit.getCommitTime();
			}
			committed = true;

		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return committed;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return committed;
		}
		return committed;
	}

	/**
	 * Creates a File on the repository directory
	 * 
	 * @param url
	 * @param content
	 */
	// TODO: Should be moved away from here
	private static void buildFile(String url, String content) {
		System.out.println("running buildFile");
		String fileName = url;
		String fileContent = content;
//				"<?xml version=\"1.0\" encoding=\"UTF-16\" standalone=\"yes\"?>"
//				+ "<!--asdasd-->"
//				+ "<a><div a=\"aa\" b=\"bb\" c=\"cc\">hej</div></a>";
		String filePath = GitHandler.REPOSITORY_URL;
		File file=FileManager.createFile(fileContent, fileName, filePath);
		
		System.out.println("writning to file"+fileContent);
		Runtime rt = Runtime.getRuntime();
		
		try {
			System.out.println("running xmllint");
			Process pr = rt.exec("/usr/bin/xmllint --c14n "+filePath+fileName);
			System.out.println("create file "+filePath+fileName+".norm");
			// hook up child process output to parent
			InputStream lsOut = pr.getInputStream();
			InputStreamReader r = new InputStreamReader(lsOut);
			BufferedReader in = new BufferedReader(r);
			String line="";
			String unSplitText="";
			while((line=in.readLine())!= null){
				unSplitText+=line;
			}
			
			unSplitText=fileContent;
			boolean inTag=false;
			String outPutString="";
			System.out.println("before"+unSplitText);
			for (int i = 0; i < unSplitText.length(); i++) {
				char c=(char)unSplitText.getBytes()[i];
				
				if(c=='<'){
					if(((char)unSplitText.getBytes()[i+1])=='!'||((char)unSplitText.getBytes()[i+1]=='?')){
						outPutString+=c;//if it is comment or <?
					}else{
						inTag=true;
						outPutString+="<\n";
					}
					
				}else if(c=='>'){
					inTag=false;
					outPutString+=c;
				}else if(inTag){
					if(c==' '){
						outPutString+="\n";
					}else{
						outPutString+=c;
					}
				}else{
					outPutString+=c;
				}
				System.out.println(outPutString+"\n\n\n");
			}
			
		
			
			
			
			
			
			
			/*
			// read the child process' output
			fileContent="";
			char c;
			int i;
			boolean questionMark=false;
			boolean inside=false;
			while ((i = lsOut.read()) != -1) {
				c=(char)i;
				if(c=='<'){
					inside=true;
				}
				if(c=='>'){
					if(!questionMark){
						fileContent+='\n';
					}
					inside=false;
				}
				if(inside&&c==' '){
					fileContent+='\n';
				}else{
					fileContent+=c;
				}
				if(c=='?'){
					questionMark=true;
				}else{
					questionMark=false;
				}
				
			}*/
			
			
		
			FileUtils.write(file, outPutString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		RepositoryRevision head = new RepositoryRevision();
		populateHEADRepositoryFiles(head);
		Git git = (Git) this.getRepository();
		Repository repository = git.getRepository();
		populateHEADGeneralData(head,git);


		return head;
	}

	private void populateHEADGeneralData(RepositoryRevision head,Git git) {
		try {
			
			ObjectId headObject = git.getRepository().resolve(Constants.HEAD);

			head.setLastCommit(git.log().add(headObject).call().iterator()
					.next().getId().getName());
			head.setLastCommitMessage(git.log().add(headObject).call()
					.iterator().next().getShortMessage());
			head.setLastCommitAuthor(git.log().add(headObject).call()
					.iterator().next().getAuthorIdent().getName());

		} catch (RevisionSyntaxException | IOException | GitAPIException
				| NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void populateHEADRepositoryFiles(RepositoryRevision head) {
		head.setRepositoryFiles(this.getWorkingDirFiles());
		for (RepositoryFile repoFile : head.getRepositoryFiles()) {
			repoFile.setFileContent(FileManager.readFileToString(repoFile
					.getFileURL()));
			repoFile.setFileURL(this.getStrippedFileURL(repoFile.getFileURL()));
		}
	}



	@Override
	public RepositoryRevision checkout(String revision) {
		System.out.println("running checkout");
		Git git = (Git) this.getRepository();
		Repository repository = git.getRepository();
		try {
			git.checkout().setStartPoint(revision).setCreateBranch(true).setName("newbranch").call();
		}catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RepositoryRevision head = new RepositoryRevision();
		populateHEADRepositoryFiles(head);
		populateHEADGeneralData(head,git);
		try {
			git.checkout().setName("master").call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return head;
	}
	
	
	

}

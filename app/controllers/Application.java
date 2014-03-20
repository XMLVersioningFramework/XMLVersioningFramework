package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import models.GitHandler;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import play.libs.Json;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.FileManager;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		return ok("this is just the index, you should be looking somewhere else");

	}

	public static Result initRepository() {
		ObjectNode returnJson = Json.newObject();
		if (GitHandler.init()) {
			returnJson.put("answer", "success");
		} else {
			returnJson.put("answer", "fail");
		}

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(returnJson);
	}

	public static Result getHEAD() {
		if (GitHandler.getGitRepository() == null) {
			System.out
					.println("Git repo was not initialized in the system, starting it now...");
			GitHandler.init();
		}
		ObjectNode head = Json.newObject();
		/**
		 * { status: OK | KO, files:[fileURL:fileURL,content:content],
		 * timestamp: timestamp, commit: commit commit: message }
		 */

		long startTime = System.nanoTime();
		ArrayList<String> workingDirFiles = GitHandler.getWorkingDirFiles();

		long elapsedTime = System.nanoTime() - startTime;
		addFilesToJSONArray(head.putArray("Files"), workingDirFiles);
		head.put("elapsedTime", elapsedTime);

		String lastCommit = "-";
		String lastCommitMessage = "-";
		String lastCommitAuthor = "-";
		ObjectId headObject;
		try {
			headObject = GitHandler.getGitRepository().getRepository()
					.resolve(Constants.HEAD);

			lastCommit = GitHandler.getGitRepository().log().add(headObject)
					.call().iterator().next().getId().getName();
			lastCommitMessage = GitHandler.getGitRepository().log()
					.add(headObject).call().iterator().next().getShortMessage();
			lastCommitAuthor = GitHandler.getGitRepository().log()
					.add(headObject).call().iterator().next().getAuthorIdent()
					.getName();

		} catch (RevisionSyntaxException | IOException | GitAPIException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return badRequest(head);
		}
		head.put("lastCommit", lastCommit);
		head.put("lastCommitMessage", lastCommitMessage);
		head.put("lastCommitAuthor", lastCommitAuthor);

		return ok(head);
	}

	private static void addFilesToJSONArray(ArrayNode array,
			ArrayList<String> workingDirFiles) {
		for (String fileURL : workingDirFiles) {
			String fileContents = FileManager.readFileToString(fileURL);

			String strippedFileURL = GitHandler.stripFileURL(fileURL);
			ObjectNode tempObjectNode = array.addObject();
			tempObjectNode.put("fileURL", strippedFileURL);
			tempObjectNode.put("fileContent", fileContents);
		}
	}

	public static Result commit() {
		/**
		 * Fetch content
		 */
		long start = System.nanoTime();
		final Map<String, String[]> postInput = request().body()
				.asFormUrlEncoded();

		String url = postInput.get("url")[0];
		String content = postInput.get("content")[0];
		String message = postInput.get("message")[0];
		String user = postInput.get("user")[0];
		String backend = postInput.get("backend")[0];

		System.out.println("Commit message:");
		System.out.println("\tUrl: " + url);
		System.out.println("\tContent: " + content);
		System.out.println("\tMessage: " + message);
		System.out.println("\tUser: " + user);
		System.out.println("\tBackend: " + backend);

		/**
		 * Create File
		 */
		String fileName = url;
		String fileContent = content;
		String filePath = GitHandler.REPOSITORY_URL;
		FileManager.createFile(fileContent, fileName, filePath);

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
		while (!GitHandler.commit(message)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");

		long elapsedTime = System.nanoTime() - start;
		ObjectNode returnJson = Json.newObject();
		returnJson.put("time", elapsedTime);
		returnJson.put("answer", "success");

		return ok(returnJson);
	}

	private static Result add(String filepattern) {
		if (GitHandler.add(filepattern))
			return ok("Success adding the pattern: " + filepattern);
		return ok("Failed to add the pattern: " + filepattern);
	}

	public static Result commit(String message) {
		if (GitHandler.commit(message)) {
			return ok("Success commiting changes");
		}
		return ok("Failed to commit changes, check log for details");
	}

	public static Result checkPreFlight() {
		// Need to add the correct domain in here!!
		response().setHeader("Access-Control-Allow-Origin", "*");
		// Only allow POST
		response().setHeader("Access-Control-Allow-Methods", "POST");
		// Cache response for 5 minutes
		response().setHeader("Access-Control-Max-Age", "300");
		// Ensure this header is also allowed!
		response().setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");
		return ok();

	}
}

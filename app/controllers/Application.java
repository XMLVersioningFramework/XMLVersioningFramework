package controllers;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;

import models.GitHandler;
import play.libs.Json;
import play.mvc.BodyParser;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.FileManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		return ok("this is just the index, you should be looking somewhere else");

	}

	public static Result initRepository() {
		ObjectNode returnJson=Json.newObject();
		if (GitHandler.init()) {
			returnJson.put("answer", "success");
		} else {
			returnJson.put("answer", "success");
		}
		
		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(returnJson);
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result sayHello() {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			ObjectNode result = Json.newObject();
			String name = result.findPath("name").textValue();
			if (name == null) {
				result.put("status", "KO");
				result.put("message", "Missing parameter [name]");
				return badRequest(result);
			} else {
				result.put("status", "OK");
				result.put("message", "Hello " + name);
				return ok(result);
			}
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
		add(url);
		
		/**
		 * Commit changes
		 */
		commit(message);

		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");
		long elapsedTime = System.nanoTime() - start;
		ObjectNode returnJson=Json.newObject();
		returnJson.put("timme", elapsedTime);
		returnJson.put("answer", "success");
		
		
		return ok(returnJson);

		/*
		 * JsonNode json = request().body().asJson(); if (json == null) { return
		 * badRequest("Expecting Json data"); } else { ObjectNode result =
		 * Json.newObject(); String name = result.findPath("name").textValue();
		 * if (name == null) { result.put("status", "KO"); result.put("message",
		 * "Missing parameter [name]"); return badRequest(result); } else {
		 * result.put("status", "OK"); result.put("message", "Hello " + name);
		 * return ok(result); } }
		 * 
		 * //fetch file String fileName ="";
		 * 
		 * 
		 * //stage info add(fileName);
		 * 
		 * //commit commit("message"); return ok();
		 */
	}

	private static Result add(String filepattern) {
		if (GitHandler.add(filepattern))
			return ok("Success adding the pattern: " + filepattern);
		return ok("Failed to add the pattern: " + filepattern);
	}

	public static Result commit(String message) {
		if(GitHandler.commit(message)){
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

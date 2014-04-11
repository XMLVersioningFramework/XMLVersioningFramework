package controllers;

import java.util.Map;

import models.BackendHandlerInterface;
import models.GitHandler;
import models.User;
import models.UserHandler;
import models.XChroniclerHandler;
import play.libs.Json;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JSONConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		return ok("this is just the index, you should be looking somewhere else");
	}

	private static BackendHandlerInterface getBackend(String backendName) {
		if (backendName.equals(BackendHandlerInterface.GIT)) {
			return GitHandler.getInstance();
		} else if (backendName.equals(BackendHandlerInterface.XCHRONICLER)) {
			return XChroniclerHandler.getInstance();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Used for testing, allows to initialize a repository with HTTP/GET
	 * 
	 * @param backendName
	 * @return
	 */
	public static Result initRepositoryWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.init()) {
			return ok("Success initializing repository");
		} else {
			return ok("Failure to initialize repository");
		}
	}

	/**
	 * Initializes the repositories, should clean existing ones if they exist
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result initRepository() {
		final Map<String, String[]> postInput = getPOSTData();
		String backendName = postInput.get(utils.JSONConstants.BACKEND)[0];
		BackendHandlerInterface backend = getBackend(backendName);
		ObjectNode returnJson = Json.newObject();

		if (backend.init()) {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.SUCCESS);
		} else {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.FAIL);
		}
		response().setHeader("Access-Control-Allow-Origin", "*");

		return ok(returnJson);
	}

	/**
	 * Collects the data submitted on a HTTP/POST request
	 * 
	 * @return
	 */
	private static Map<String, String[]> getPOSTData() {
		return request().body().asFormUrlEncoded();
	}

	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getHEAD() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(backend.getRepositoryHEAD().toJSON());
	}
	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getLog() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		System.out.println(backend.getLog());
		return ok(Json.toJson(backend.getLog()));
	}

	/**
	 * Uses HTTP/GET
	 * 
	 * @return
	 */
	public static Result getHEADWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);

		return ok(backend.getRepositoryHEAD().toJSON());
	}

	public static Result removeRepository(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.removeExistingRepository())
			return ok(JSONConstants.SUCCESS);
		return ok(JSONConstants.FAIL);
	}

	public static Result commit() {
		/**
		 * Fetch content
		 */
		
		final Map<String, String[]> postInput = getPOSTData();

		String url = postInput.get(JSONConstants.URL)[0];
		String content = postInput.get(JSONConstants.CONTENT)[0];
		String message = postInput.get(JSONConstants.MESSAGE)[0];
		String userId = postInput.get(JSONConstants.USER)[0];
		String backendName = postInput.get(JSONConstants.BACKEND)[0];

		/**
		 * Pre-processes the POST contents
		 */
		BackendHandlerInterface backend = getBackend(backendName);
		User user = UserHandler.getUser(Integer.parseInt(userId));

		/**
		 * Tries to perform the Commit
		 */
		String answer = "";
		long elapsedTime = Long.MAX_VALUE;
		long start = System.nanoTime();
		if (backend.commit(url, content, message, user)) {
			answer = JSONConstants.SUCCESS;
			elapsedTime = System.nanoTime() - start;
		} else {
			answer = JSONConstants.FAIL;
		}

		/**
		 * Creates the response
		 */

		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");

		ObjectNode returnJson = Json.newObject();
		returnJson.put(JSONConstants.TIME, elapsedTime);
		returnJson.put(JSONConstants.ANSWER, answer);

		return ok(returnJson);
	}
	
	public static Result testVfile(){
		XChroniclerHandler backend = (XChroniclerHandler) XChroniclerHandler.getInstance();

		return ok(backend.generateVFileSimpleTest());
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

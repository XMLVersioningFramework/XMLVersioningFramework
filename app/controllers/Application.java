package controllers;

import java.util.Map;

import models.GitHandler;
import play.libs.Json;
import play.mvc.BodyParser;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		final Map<String, String[]> post = request().body().asFormUrlEncoded();
		// String callback=post.get("callback")[0];
		String test = "asds";
		JsonNode jsonObject = Json.toJson(test);
		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(jsonObject);
		// return ok(Jsonp.jsonp(callback, mapper));

		// return
		// ok("this is just the index, you should be looking somewhere else");

	}

	public static Result initRepository() {
		if (GitHandler.init()) {
			return ok("Success creating repository");
		} else {
			return ok("Failed to create repository");
		}
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

	public static Result add(String filepattern) {
		if (GitHandler.add(filepattern))
			return ok("Success adding the pattern: " + filepattern);
		return ok("Failed to add the pattern: " + filepattern);
	}

	public static Result commit(String message) {
		if (GitHandler.commit(message))
			return ok("Success commiting changes");
		return ok("Failed to commit changes, check log for details");
	}

	// @BodyParser.Of(BodyParser.Json.class)
	// public static Result getFile(String url) {
	// TempFile tf = GitHandler.getFile(url);
	// return ok(Json.toJson(tf));
	// }

	public static Result checkPreFlight() {
		/*// Need to add the correct domain in here!!
		response().setHeader("Access-Control-Allow-Origin", "*");
		// Only allow POST
		response().setHeader("Access-Control-Allow-Methods", "POST");
		// Cache response for 5 minutes
		response().setHeader("Access-Control-Max-Age", "300");
		// Ensure this header is also allowed!
		response().setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");*/
		return ok();
	}
}

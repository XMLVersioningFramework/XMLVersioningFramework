package controllers;

import models.GitHandler;
import models.TempFile;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
	
	public static Result index(){
		return ok("this is just the index, you should be looking somewhere else");
	}

	public static Result initRepository() {
		if (GitHandler.init()) {
			return ok("Success creating repository");
		} else {
			return ok("Failed to create repository");
		}
	}

	public static Result add(String filepattern) {
		if(GitHandler.add(filepattern))
			return ok("Success adding the pattern: " + filepattern);
		return ok("Failed to add the pattern: " + filepattern);
	}

	public static Result commit(String message) {
		if(GitHandler.commit(message))
			return ok("Success commiting changes");
		return ok("Failed to commit changes, check log for details");
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result getFile(String url) {
		TempFile tf = GitHandler.getFile(url);
		return ok(Json.toJson(tf));
	}

}

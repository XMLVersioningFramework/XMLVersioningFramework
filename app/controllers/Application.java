package controllers;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.GitHandler;
import models.TempFile;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.Jsonp;
import play.mvc.Action;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.SimpleResult;

public class Application extends Controller {
	
	public static Result index(){
	    final Map<String, String[]> post = request().body().asFormUrlEncoded();
        //String callback=post.get("callback")[0];
        String test="asds";
	    JsonNode jsonObject =Json.toJson(test);
	   // return new ActionWrapper(super.onRequest(request, actionMethod));
	    response().setHeader("Access-Control-Allow-Origin", "*");
	    return ok(jsonObject);
        //return ok(Jsonp.jsonp(callback, mapper));
		
	//	return ok("this is just the index, you should be looking somewhere else");
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
	public static Result checkPreFlight() {
	    response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
	    response().setHeader("Access-Control-Allow-Methods", "POST");   // Only allow POST
	    response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
	    response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!  
	    return ok();
	}

}

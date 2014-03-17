package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import models.Git;
import models.TempFile;
import play.libs.Json;
import play.mvc.BodyParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Application extends Controller {
    
	public static Result initRepo() {
		Git.init();
		return ok(views.html.index.render("Hello my framwork"));
	}
	
	public static Result CommitFile() {
		return ok(views.html.index.render("Hello my framwork"));
	}
	@BodyParser.Of(BodyParser.Json.class)
	public static Result getFile(String url) {
		TempFile tf=Git.getFile(url);
		return ok(Json.toJson(tf));
	}
    
    
}

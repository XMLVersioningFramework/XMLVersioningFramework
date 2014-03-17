package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import models.GitHandler;
import models.TempFile;
import play.libs.Json;
import play.mvc.BodyParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Application extends Controller {

	public static Result initRepo() {
		if (GitHandler.init()) {
			return ok(views.html.index.render("Success creating"));
		} else {
			return ok(views.html.index.render("Failed to create"));
		}
	}

	public static Result addFile() {
		return ok(views.html.index.render("Hello my framwork"));
	}

	public static Result commitFile() {
		return ok(views.html.index.render("Hello my framwork"));
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result getFile(String url) {
		TempFile tf = GitHandler.getFile(url);
		return ok(Json.toJson(tf));
	}

}

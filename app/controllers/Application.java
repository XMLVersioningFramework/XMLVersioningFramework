package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import models.Task;
import models.UserStory;
import models.UserStoryHandler;
import play.libs.Json;
import play.mvc.BodyParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class Application extends Controller {
    
	public static Result addDataPoint(String i) {
		return ok(views.html.index.render("Hello my framwork"+i));
	}
	@BodyParser.Of(BodyParser.Json.class)
	public static Result index() {
		//JsonNode json = request().body().asJson();
		//ObjectNode result = Json.newObject();
    	//List<UserStory> userStories = UserStoryHandler.loadAllUserStories();
    	//for (UserStory userStory : userStorys) {
    //		System.out.println(userStory);
	//	}
    	return ok(views.html.index.render("Hello my framwork"));
    	//List<Task> tasks = Task.find.all();
     // 	return ok(views.html.index.render("Hello my framwork"));

    }
    public static Result getUserstories() {
    	List<UserStory> userStories = UserStoryHandler.loadAllUserStories();
    	return ok(Json.toJson(userStories));
    }
    public static Result getAUserstory(String userStoryName) {
    	//List<Task> tasks = Task.find.all();
    	UserStory userStory = UserStoryHandler.getUserStoryInfo(userStoryName);
    	return ok(Json.toJson(userStory));
    }
        
    
    
}

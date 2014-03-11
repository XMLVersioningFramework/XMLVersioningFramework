package models;

import java.util.ArrayList;
import java.util.List;

import play.libs.Json;

public class UserStory {
	String html="";
	String name="";
	List<Test> tests = new ArrayList<Test>();
	public UserStory(String tName,String tHtml) {
		html=tHtml;
		name=tName;
	}
	public void addTests(Test test){
		tests.add(test);
	}
	public String getHtml(){
		return html;
	}
	public String getName(){
		return name;
	}
	public List<Test> getTests(){
		return tests;
	}
	
	
	@Override
	public String toString() {
		return html;
	}
	
}

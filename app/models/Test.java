package models;

import java.util.ArrayList;
import java.util.List;

public class Test {
	List<String> input = new ArrayList<String>();
	List<String> output = new ArrayList<String>();
	String runFile="";
	String name="";
	
	public Test(String tName){
		name=tName;
	}
	public String getName(){
		return name;
	}
	public void addInput(String s){
		input.add(s);
	}
	public void addOutput(String s){
		output.add(s);
	}
	public List<String> getInput() {
		return input;
	}
	public List<String> getOutput() {
		return output;
	}
	public String getRunFile() {
		return runFile;
	}
	public void setRunFile(String tRunFile) {
		runFile=tRunFile;
	}
	

}

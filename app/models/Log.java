package models;

public class Log {
	String id;
	String text;
	public Log(String id,String text){
		this.id=id;
		this.text=text;
	}
	public String getId(){
		return id;
	}
	public String getText(){
		return text;
	}
}

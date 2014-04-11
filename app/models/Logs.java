package models;

import java.util.ArrayList;

public class Logs {
	ArrayList<Log> logs=new ArrayList<Log>();
	public void addLog(Log log){
		logs.add(log);
	}
	public ArrayList<Log> getLogs(){
		return logs;
	}
}

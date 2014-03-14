package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Git extends BackendInterface{

	
	public static boolean init(){
		System.out.println(runCommand("pwd"));;
	    return true;
	}
	
	
	/*public static TempFile getFile(String url){
		
	}*/
}





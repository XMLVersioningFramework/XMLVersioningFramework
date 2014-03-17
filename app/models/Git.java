package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


//use JGIT
public class Git extends BackendInterface{
	static String baseURL="./backends/git/";
    static String repoURL=baseURL+"repo";
	
	public static boolean init(){
		//remove the current repo
		System.out.println("init");
		String remmove="rm -rf "+repoURL;
		System.out.println(runCommand(remmove));
		String mkdir="mkdir "+repoURL;
		System.out.println(mkdir);
		System.out.println(runCommand(mkdir));
		//init repo
		System.out.println(runCommand("git init "+repoURL));
	    return true;
	}
	
	
	/*public static TempFile getFile(String url){
		
	}*/
}





package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BackendHandlerInterface {
	
	public boolean init(){
		throw new UnsupportedOperationException();
	}
	public static String commitAFile(TempFile tf) {
		throw new UnsupportedOperationException();
	}
	public static TempFile getFile(String url){
		throw new UnsupportedOperationException();
	}

	
	public static String runCommand(String s){
		String returnMsg="";
		Process p =null;
		try {
			p = Runtime.getRuntime().exec(s );
			p.waitFor();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));
	 
	    String line = "";			
	    try {
			while ((line = reader.readLine())!= null) {
				returnMsg+=line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return returnMsg;
	}
}

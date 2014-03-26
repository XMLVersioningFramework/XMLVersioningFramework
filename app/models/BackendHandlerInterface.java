package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class BackendHandlerInterface {
	
	public abstract Object getRepository();

	public abstract boolean init();

	public abstract String commitAFile(TempFile tf);

	public abstract TempFile getFile(String url);
	
	public abstract ArrayList<String> getWorkingDirFiles();

	public static String runCommand(String s) {
		String returnMsg = "";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(s);
			p.waitFor();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				returnMsg += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnMsg;
	}


}

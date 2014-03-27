package models;

public class UserHandler {
	
	public static String getUserName(int i){
		String[]  userArray = new String[3];
		userArray[0] = "Alice";
		userArray[1] = "Bob";
		userArray[2] = "Charly";
		
		return userArray[i];
	}
	public static String getUserEmail(int i){
		String[]  userArray = new String[3];
		userArray[0] = "Alice@company.com";
		userArray[1] = "Bob@company.com";
		userArray[2] = "Charly@company.com";
		
		return userArray[i];
	}
}
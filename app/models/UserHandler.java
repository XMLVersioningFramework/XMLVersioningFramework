package models;

public class UserHandler {

	public static String getUserName(int userId) {
		String[] userArray = new String[3];
		userArray[0] = "Alice";
		userArray[1] = "Bob";
		userArray[2] = "Charly";

		return userArray[userId];
	}

	public static String getUserEmail(int userId) {
		String[] userArray = new String[3];
		userArray[0] = "Alice@company.com";
		userArray[1] = "Bob@company.com";
		userArray[2] = "Charly@company.com";

		return userArray[userId];
	}
}

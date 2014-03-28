package models;

import java.util.ArrayList;

public class UserHandler {
	static ArrayList<User> users =new ArrayList<User>();
	
	public UserHandler(){
		users =new ArrayList<User>();
		users.add(new User("Alice","Alice@company.com"));
		users.add(new User("Bob","Bob@company.com"));
		users.add(new User("Charly","Charly@company.com"));
	}
	public static User getUser(int i){
		return users.get(i);
	}
}

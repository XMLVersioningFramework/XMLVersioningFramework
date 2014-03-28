/**
 * 
 */
package models;

/**
 *
 */
public class User {

	private String userName;
	private String userEmail;
	
	public User(int userId) {
		this.userName = UserHandler.getUserName(userId);
		this.userEmail = UserHandler.getUserEmail(userId);
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userEmail
	 */
	public String getUserEmail() {
		return userEmail;
	}

	/**
	 * @param userEmail the userEmail to set
	 */
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

}

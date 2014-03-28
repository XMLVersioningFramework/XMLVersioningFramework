/**
 * 
 */
package models;

/**
 *
 */
public class User {

	private String name;
	private String email;
	
	public User(String tname,String temail) {
		this.name=tname;
		this.email=temail;
	}

	/**
	 * @return the userName
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the userEmail
	 */
	public String getEmail() {
		return email;
	}


}

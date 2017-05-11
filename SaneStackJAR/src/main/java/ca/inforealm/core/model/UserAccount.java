package ca.inforealm.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * Basic representation of an authorizable user.
 * 
 * @author Jason Halliday
 * @created April 4, 2008
 */
@Entity
@Table(name = "user_account")
public class UserAccount extends Actor {

	private String username;

	@Column(name = "md5_password")
	private String md5password;

	private String email;

	// ======================================================================

	public UserAccount() {
		super();
	}

	public UserAccount(Long id) {
		super(id);
	}

	// ======================================================================
	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#getUsername()
	 */
	@DisplayTitle
	public String getUsername() {
		return username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#getMd5password()
	 */
	public String getMd5password() {
		return md5password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#setMd5password(java.lang.String)
	 */
	public void setMd5password(String md5password) {
		this.md5password = md5password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#getEmail()
	 */
	public String getEmail() {
		return email;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.inforealm.core.model.ee.User#setEmail(java.lang.String)
	 */
	public void setEmail(String email) {
		this.email = email;
	}

}

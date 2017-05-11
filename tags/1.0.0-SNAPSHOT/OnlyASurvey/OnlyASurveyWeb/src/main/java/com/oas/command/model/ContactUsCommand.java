package com.oas.command.model;


/**
 * A message from one of our users submitted via the Contact Us feature.
 * 
 * @author xhalliday
 * @Since November 15, 2008
 */
public class ContactUsCommand {

	/** User's email address, for anonymous users. */
	private String email;

	/** The user's message to us. */
	private String message;

	// ======================================================================

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}

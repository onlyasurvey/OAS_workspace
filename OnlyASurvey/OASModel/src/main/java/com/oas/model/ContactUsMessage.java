package com.oas.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A message from one of our users submitted via the Contact Us feature.
 * 
 * @author xhalliday
 * @Since November 15, 2008
 */
@Entity
@Table(schema = "oas", name = "contact_us_message")
public class ContactUsMessage extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4583141403018050415L;

	/** Account Owner, if the user was logged in when the message was sent. */
	@OneToOne
	@JoinColumn(name = "owner_id")
	private AccountOwner accountOwner;

	/** User's email address, for anonymous users. */
	private String email;

	/** The user's message to us. */
	private String message;

	// ======================================================================

	public ContactUsMessage() {
	}

	public ContactUsMessage(String email, String message) {
		setEmail(email);
		setMessage(message);
	}

	// ======================================================================

	/**
	 * @return the accountOwner
	 */
	public AccountOwner getAccountOwner() {
		return accountOwner;
	}

	/**
	 * @param accountOwner
	 *            the accountOwner to set
	 */
	public void setAccountOwner(AccountOwner accountOwner) {
		this.accountOwner = accountOwner;
	}

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

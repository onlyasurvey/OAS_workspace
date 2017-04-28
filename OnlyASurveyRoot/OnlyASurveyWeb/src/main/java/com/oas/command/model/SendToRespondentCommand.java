package com.oas.command.model;

/**
 * Command used to send Emails to respondents
 * 
 * @author jfchenier
 * @since March 17th, 2009
 */
public class SendToRespondentCommand {

	/** "From" address. */
	private String fromAddress;

	/** Subject line */
	private String subject;

	/** Message text. */
	private String message;

	/**
	 * Accessor.
	 * 
	 * @return the fromAddress
	 */
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 * Accessor.
	 * 
	 * @param fromAddress
	 *            the fromAddress to set
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 * Accessor.
	 * 
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Accessor.
	 * 
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Accessor.
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Accessor.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}

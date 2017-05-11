package com.oas.command.model;

/**
 * Command used to send Emails to respondents
 * 
 * @author jfchenier
 * @since March 17th, 2009
 */
public class SendToRespondentCommand {

	/** user input for email */
	private String subject;

	private String message;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

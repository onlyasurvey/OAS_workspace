package com.oas.model.invitations;

/**
 * Status of a Mail Out.
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
public enum MailOutStatus {

	/** Unsent. */
	UNSENT,

	/** Being processed now. */
	SENDING,

	/** Sent successfully. */
	SENT,

	/** Error occurred. */
	ERROR;
}

package com.oas.model;

/**
 * Enum indicating the status of an invitation to a respondent.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public enum InvitationStatusType {

	/** No messages sent yet for this invitation. */
	UNSENT,

	/** Put into the queue for sending. */
	QUEUED,

	/** Initial invitation email has been sent to respondent. */
	SENT,

	/** At least one reminder email has been sent to respondent. */
	REMINDED,

	/** Started responding but not yet closed. */
	STARTED,

	/** Respondent has responded. */
	RESPONDED,

	/** An error while sending. */
	ERROR;

}

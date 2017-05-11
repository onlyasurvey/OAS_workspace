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

	/** Initial invitation email has been sent to respondent. */
	SENT,

	/** At least one reminder email has been sent to respondent. */
	REMINDED,

	/** Respondent has responded. */
	RESPONDED;

}

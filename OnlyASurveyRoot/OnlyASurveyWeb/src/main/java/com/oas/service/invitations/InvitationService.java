package com.oas.service.invitations;

import java.util.List;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Invitation;
import com.oas.model.Survey;

/**
 * Service for managing and sending invitations and reminders.
 * 
 * @author xhalliday
 * @author jchenier
 * @since March 9, 2009
 */
public interface InvitationService extends AbstractServiceInterface {

	/**
	 * Get a paged list of Invitations for the specified Survey.
	 * 
	 * @param survey
	 * @param page
	 * @param count
	 * @return
	 */
	List<Invitation> getPagedList(Survey survey, int page, int count);

	/**
	 * Store many invitations.
	 * 
	 * @param survey
	 * @param emailAddresses
	 * 
	 * @return
	 */
	List<Invitation> createInvitations(Survey survey, List<String> emailAddresses);

	/**
	 * Tokenizes user email addresses input
	 * 
	 * @param userData
	 * @return
	 */
	List<String> tokenizeUserEmailData(String userData);

	/**
	 * Returns a list of valid email addresses
	 * 
	 * @param userData
	 * @return
	 */
	List<String> validateUserEmailList(List<String> userList);

	/**
	 * Store one invitation.
	 * 
	 * @param survey
	 * @param emailAddress
	 * 
	 * @return
	 */
	Invitation createInvitation(Survey survey, String emailAddress);

	/**
	 * Purge an invitation.
	 * 
	 * @param invitation
	 */
	void purge(Invitation invitation);

	/**
	 * Get invitation
	 * 
	 * @param id
	 * @return
	 */
	Invitation getInvitation(long id);

	/**
	 * Send invitations to all respondents who are NOT in the RESPONDED state.
	 * 
	 * @param survey
	 * @return
	 */
	boolean sendAllInvitations(Survey survey, String subject, String message);

	/**
	 * Send invitations to all respondents who are in the UNSENT state.
	 * 
	 * @param survey
	 * @return
	 */
	boolean sendNewInvitations(Survey survey, String subject, String message);

	/**
	 * Send reminders to all respondents who are in the SENT or REMINDED state.
	 * 
	 * @param survey
	 * @return
	 */
	boolean sendReminders(Survey survey, String subject, String message);

	// ======================================================================
	// COUNT FUNCTION
	// ======================================================================

	/**
	 * Count the invitations for the Survey.
	 * 
	 * @param survey
	 * @return
	 */
	int countInvitations(Survey survey);

	/**
	 * Count how many invitations have been sent to the current list of
	 * invitation respondents
	 * 
	 * @param survey
	 * @return
	 */
	int countSentInvitations(Survey survey);

	// ======================================================================

	/**
	 * Count how many invitation responses have been received for the current
	 * list of invitation respondents
	 * 
	 * @param survey
	 * @return
	 */
	int countInvitationResponses(Survey survey);

	// ======================================================================

	/**
	 * Get Respondent Email List of a particular Survey
	 * 
	 * @param survey
	 * 
	 * @return
	 */
	public List<String> getRespondentEmailList(Survey survey);

}

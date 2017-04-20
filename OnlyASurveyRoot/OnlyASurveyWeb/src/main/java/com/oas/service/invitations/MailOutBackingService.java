package com.oas.service.invitations;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Invitation;
import com.oas.model.invitations.InvitationMailQueue;

/**
 * Service that backs the {@link MailOutService} - this exists to be able to
 * make atomic changes, whereas the Mail Out Service itself is
 * non-transactional.
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
public interface MailOutBackingService extends AbstractServiceInterface {

	/**
	 * Finds an {@link InvitationMailQueue} that is not yet sent, set's it's
	 * status to SENDING, and returns a <strong>detached copy</strong> of it.
	 * 
	 * @return Detached InvitationMailQueue
	 */
	InvitationMailQueue findUnsentMailQueueAndMarkAsSending();

	/**
	 * Mark the {@link InvitationMailQueue} item as sent successfully.
	 * 
	 * @param item
	 *            The item to update.
	 */
	void markSent(InvitationMailQueue item);

	/**
	 * Mark the {@link InvitationMailQueue} item as having an error.
	 * 
	 * @param item
	 *            The item to update.
	 * @param errorString
	 *            String indicating the cause or nature of the error.
	 */
	void markError(InvitationMailQueue item, String errorString);

	/**
	 * Replace placeholders with real links.
	 * 
	 * @param input
	 *            Source message
	 * @param survey
	 *            Target survey
	 * @return Replaced string
	 */
	public String replaceSurveyVarWithLink(String input, Invitation invitation);
}

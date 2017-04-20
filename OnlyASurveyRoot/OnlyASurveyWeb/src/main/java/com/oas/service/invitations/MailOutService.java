package com.oas.service.invitations;

import ca.inforealm.core.service.AbstractServiceInterface;

/**
 * Service for sending invitations and reminders (Mail Outs).
 * 
 * <p>
 * This is a <strong>non-transactional</strong> service which depends on a
 * backing service to perform atomic changes to the backend. See
 * {@link MailOutBackingService}.
 * </p>
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
public interface MailOutService extends AbstractServiceInterface {

	/**
	 * Process one Invitation Mail Queue.
	 */
	void processOneItem();
}

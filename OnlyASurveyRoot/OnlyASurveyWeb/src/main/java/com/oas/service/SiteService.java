package com.oas.service;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.ContactUsCommand;

/**
 * Services in the "site scope" - ie, things unrelated to the core business, but
 * supporting the public-facing web site, like Contact Us.
 * 
 * @author xhalliday
 * @since November 15, 2008
 */
public interface SiteService extends AbstractServiceInterface {

	/**
	 * Adds a message from an end user via the Contact Us feature.
	 * 
	 * @param command
	 */
	public void addContactMessage(ContactUsCommand command);

}

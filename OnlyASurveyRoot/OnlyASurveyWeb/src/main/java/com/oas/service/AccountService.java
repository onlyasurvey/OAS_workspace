package com.oas.service;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.SupportedLanguage;

/**
 * Service for managing User Accounts and Account Owners.
 * 
 * @author xhalliday
 * @since October 29, 2008
 */
public interface AccountService extends AbstractServiceInterface {

	/**
	 * Set the current user's language to the one specified. Requires a current
	 * user.
	 * 
	 * @param language
	 */
	public void setUserLanguage(SupportedLanguage language);

	/**
	 * Change the current user's default survey language list.
	 * 
	 * @param list
	 */
	// public void setDefaultSurveyLanguages(Collection<SupportedLanguage>
	// list);
	/**
	 * Determine if the given email address already exists in the system.
	 * 
	 * @param email
	 * @return
	 */
	public boolean emailAlreadyExists(String email);

	/**
	 * Find the AccountOwner that the current user represents.
	 * 
	 * @param account
	 * @return
	 */
	// public AccountOwner findOwnerOfAccount();
	/**
	 * Find the AccountOwner that the specified user represents.
	 * 
	 * @param account
	 * @return
	 */
	// public AccountOwner findOwnerOfAccount(UserAccount account);
}

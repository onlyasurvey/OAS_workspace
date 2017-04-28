package com.oas.service.impl;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.AccountOwner;
import com.oas.model.SupportedLanguage;
import com.oas.service.AccountService;

/**
 * Service for managing User Accounts and Account Owners.
 * 
 * @author xhalliday
 * @since October 29, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AccountServiceImpl extends AbstractServiceImpl implements AccountService {

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setUserLanguage(SupportedLanguage language) {

		Assert.notNull(language);

		// don't let a user sneak a new language in
		Assert.notNull(language.getId());

		// must be run with an authenticated user
		requireSecureContext();

		// get the current user details out of session
		// NOTE the current AccountOwner object in session is never modified,
		// only the persistent copy, because locale resolution is session-based
		Long currentUserId = getCurrentUser().getId();
		Assert.notNull(currentUserId);

		// load the record relating to `whoami`
		AccountOwner userToChange = get(AccountOwner.class, currentUserId);

		// make the change
		userToChange.setLanguage(language);

		//
		// getHibernateTemplate().saveOrUpdate(user);
		persist(userToChange);

	}

	@Override
	@Unsecured
	public boolean emailAlreadyExists(String email) {

		Assert.hasText(email);

		// TODO potentially inefficient - should be a COUNT()
		Collection<UserAccount> list = find("from UserAccount where email=?", email);

		return list.size() != 0;
	}

}

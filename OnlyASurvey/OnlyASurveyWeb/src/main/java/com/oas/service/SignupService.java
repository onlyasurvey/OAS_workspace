package com.oas.service;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.SignupCommand;
import com.oas.model.AccountOwner;

public interface SignupService extends AbstractServiceInterface {

	/**
	 * Store the given signup data.
	 */
	public AccountOwner storeSignup(SignupCommand command, String remoteIp);

}

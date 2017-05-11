package com.oas.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.RoleService;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.command.model.SignupCommand;
import com.oas.model.AccountOwner;
import com.oas.model.SupportedLanguage;
import com.oas.service.SignupService;
import com.oas.service.SupportedLanguageService;

/**
 * Service for interacting with surveys.
 * 
 * @author Jason Halliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SignupServiceImpl extends AbstractServiceImpl implements SignupService {

	@Autowired
	private RoleService roleService;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	private Md5PasswordEncoder encoder = new Md5PasswordEncoder();

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED)
	public AccountOwner storeSignup(SignupCommand command, String remoteIp) {

		// this would be an invalid state
		Assert.isTrue(!SecurityUtil.isSecureContext(), "user already logged in");

		// sanity check key input - front-end validators ensure this data is
		// valid, so these checks would be illegal argument exceptions
		Assert.notNull(command);
		Assert.notNull(command.getUsername());
		Assert.notNull(command.getPassword());
		Assert.isTrue(command.getPassword().length == 2);
		Assert.notNull(command.getPassword()[0]);
		Assert.isTrue(command.getPassword()[0].equals(command.getPassword()[1]));
		Assert.notNull(command.getEmail());
		Assert.isTrue(command.getEmail().length == 2);
		Assert.notNull(command.getEmail()[0]);
		Assert.isTrue(command.getEmail()[0].equals(command.getEmail()[1]));

		// new Account Owner
		AccountOwner retval = new AccountOwner();

		// user account data
		retval.setUsername(command.getUsername().toLowerCase());
		String md5password = encoder.encodePassword(command.getPassword()[0], null);
		retval.setMd5password(md5password);

		// contact info
		retval.setFirstname(command.getFirstname());
		retval.setLastname(command.getLastname());
		retval.setOrganization(command.getOrganization());
		retval.setTelephone(command.getTelephone());
		retval.setEmail(command.getEmail()[0]);
		retval.setLearnedAbout(command.getLearnedAbout());

		// language info
		SupportedLanguage userLanguage = supportedLanguageService.findByCode(LocaleContextHolder.getLocale().getISO3Language());
		Assert.notNull(userLanguage);
		retval.setLanguage(userLanguage);

		// misc data
		retval.setIpOnJoin(remoteIp);
		retval.setLastLogin(new Date());
		retval.setGovernment(command.isGovernment());
		retval.setNewsletterFlag(command.isNews());

		// store user in backend
		persist(retval);

		// add user role for the app
		roleService.assignRole(retval, GlobalRoles.ROLE_USER);

		return retval;
	}

}

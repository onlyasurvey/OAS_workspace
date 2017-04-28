package com.oas.util.autoinstall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.support.autoinstall.ApplicationAutoInstaller;

import com.oas.model.SupportedLanguage;

/**
 * Performs auto-registration and installation of an OnlyASurvey installation.
 * 
 * NOT enabled as it won't be found by the component scan. Currently unfinished;
 * adding supported languages ends up with detached object errors; need to see
 * if app runs OK without magic -1000 and -1001 IDs.
 * 
 * @author xhalliday
 * @since May 2, 2009
 */
// @Component
// @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class AutoInstaller implements ApplicationAutoInstaller {

	/** Generic DAO. */
	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	/** {@inheritDoc} */
	@Override
	public Application inforealmRegistration() {

		if (true) {
			throw new UnsupportedOperationException("auto-registration not supported");
		}

		// the main application model
		Application retval = new Application();
		retval.setIdentifier("OAS");

		// roles
		addRoles(retval);

		// configuration items
		addConfigurationItems(retval);

		// save
		dataAccessObject.persist(retval);

		//
		return retval;
	}

	/** {@inheritDoc} */
	@Override
	public void customInstallation() {

		if (true) {
			throw new UnsupportedOperationException("auto-registration not supported");
		}

		//
		// createSupportedLanguages();
		// magic numbers
		// SupportedLanguage english = new SupportedLanguage(-1000L, "eng");
		// SupportedLanguage french = new SupportedLanguage(-1001L, "fra");
		SupportedLanguage english = new SupportedLanguage(null, "eng");
		SupportedLanguage french = new SupportedLanguage(null, "fra");

		// persist so they have an ID
		dataAccessObject.persist(english);
		dataAccessObject.persist(french);

		// add labels in both languages
		addEnglishNames(english, french);
		addFrenchNames(english, french);

		// persist to save names
		dataAccessObject.persist(english);
		dataAccessObject.persist(french);
	}

	// ======================================================================

	private void addRoles(Application application) {
		application.getRoleDefinitions().add(new RoleDefinition(application, "ROLE_USER"));
		application.getRoleDefinitions().add(new RoleDefinition(application, "ROLE_ENTERPRISE_ADMIN"));
	}

	private void addConfigurationItems(Application application) {
		application.getConfigurationItems().add(new ConfigurationItem(application, "publicHostname", "string", "www.FIXME.com"));
		application.getConfigurationItems().add(
				new ConfigurationItem(application, "publicContentFilesystemPrefix", "string", "/home/oaspubliccontent/public/"));
	}

	private void addEnglishNames(SupportedLanguage english, SupportedLanguage french) {
		english.addObjectName(english, "English");
		french.addObjectName(english, "French");
	}

	private void addFrenchNames(SupportedLanguage english, SupportedLanguage french) {
		english.addObjectName(english, "Anglais");
		french.addObjectName(english, "FIXME-UNICODE");
	}
}

package ca.inforealm.core;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.GlobalRoles;

abstract public class AbstractTestDataCreatingBaseTest extends AbstractBaseTest {

	@Override
	public void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		log.info("AbstractTestDataCreatingBaseTest is setting up in transaction");

		// set locale context
		setEnglish();

		// identify the application, overriding any applicationIdentifier in
		// application context, because it can be changed by tests but that
		// change persists between tests, which is
		String applicationIdentifier = "TestCaseApp#" + getMBUN();

		Application application = new Application();
		application.setIdentifier(applicationIdentifier);

		//
		// configuration items
		//
		ConfigurationItem item1 = new ConfigurationItem(application, OFFLINE_FOR_MAINTENANCE, "boolean", "false");
		application.getConfigurationItems().add(item1);

		getHibernateTemplate().persist(application);
		getHibernateTemplate().flush();

		RoleDefinition roleUser = createRoleDefinition(ROLE_USER, application);
		getHibernateTemplate().persist(roleUser);
		RoleDefinition roleAppAdmin = createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, application);
		getHibernateTemplate().persist(roleAppAdmin);

		application.getRoleDefinitions().add(roleUser);
		application.getRoleDefinitions().add(roleAppAdmin);

		// load the newly-created Application model
		getSaneContext().setApplicationIdentifier(applicationIdentifier);
		getSaneContext().reloadAllModels();

		//
		// reload the strings, which will clear the in-memory map since this new
		// application has no strings.
		//
		if (getResourceStringService() != null) {
			getResourceStringService().reloadResources();
		}
	}
}

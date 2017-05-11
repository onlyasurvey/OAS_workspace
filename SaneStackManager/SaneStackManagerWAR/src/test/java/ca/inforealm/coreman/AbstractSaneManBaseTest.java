package ca.inforealm.coreman;

import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.coreman.service.ConfigurationManagementService;

abstract public class AbstractSaneManBaseTest extends AbstractTestDataCreatingBaseTest {
	protected static final String[] CONFIG_LOCATIONS = new String[] { "file:src/main/resources/applicationContext.xml",
			"file:src/main/resources/saneStackManagerServlet-servlet.xml",
			// "file:src/main/resources/applicationContext-services.xml",
			"classpath*:/applicationContext-sane.xml"
	// , "file:src/main/resources/applicationContext-acegi-security.xml"
	};

	@Override
	protected String[] getConfigLocations() {
		return CONFIG_LOCATIONS;
	}

	@Autowired
	ConfigurationManagementService configurationManagementService;

	protected ConfigurationManagementService getConfigurationManagementService() {
		return configurationManagementService;
	}

	protected ConfigurationItem createConfigItem(Application application, String identifier, String value) {
		return getConfigurationManagementService().save(application.getId(), identifier, value);
	}

}

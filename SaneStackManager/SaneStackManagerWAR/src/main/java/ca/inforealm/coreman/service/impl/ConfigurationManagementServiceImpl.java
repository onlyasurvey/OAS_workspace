package ca.inforealm.coreman.service.impl;

import java.util.Collection;

import org.springframework.security.annotation.Secured;
import org.springframework.stereotype.Service;

import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.core.service.impl.AbstractServiceImpl;
import ca.inforealm.coreman.service.ApplicationManagementService;
import ca.inforealm.coreman.service.ConfigurationManagementService;

/**
 * Allows the SADMAN application to manage all application data.
 * 
 * @author Jason Mroz
 */
@Service
public class ConfigurationManagementServiceImpl extends AbstractServiceImpl implements ConfigurationManagementService {

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public ConfigurationItem get(Long applicationId, String identifier) {
		requireSecureContext();

		Collection<ConfigurationItem> list = find("from ConfigurationItem where application.id = ? and identifier = ?", new Object[] { applicationId,
				identifier });

		return unique(list);
	}

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public Collection<ConfigurationItem> getConfigurationItems(Long applicationId) {
		requireSecureContext();
		Collection<ConfigurationItem> list = find("from ConfigurationItem where application.id = ?", applicationId);
		return list;
	}

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public ConfigurationItem save(Long applicationId, String identifier, String value) {
		return save(applicationId, identifier, value, "string");
	}

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public ConfigurationItem save(Long applicationId, String identifier, String value, String valueType) {

		// must be in a secure context
		requireSecureContext();

		// not initialized
		ConfigurationItem retval;

		// find any existing
		ConfigurationItem existing = get(applicationId, identifier);
		if (existing == null) {
			// new item
			retval = new ConfigurationItem();
			retval.setApplication(applicationManagementService.load(applicationId));
		} else {
			// existing item
			retval = existing;
		}

		// save changes
		retval.setIdentifier(identifier);
		retval.setValueType(valueType);
		retval.setValue(value);

		//
		persist(retval);

		return retval;
	}

	// ======================================================================

	private ApplicationManagementService applicationManagementService;

	public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {
		this.applicationManagementService = applicationManagementService;
	}
}
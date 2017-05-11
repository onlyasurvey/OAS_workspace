package ca.inforealm.coreman.service;

import java.util.Collection;

import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.service.AbstractServiceInterface;

/**
 * Provides management APIs for configuration items by application.
 * 
 * @author Jason Mroz
 * @created July 11, 2008
 */
public interface ConfigurationManagementService extends AbstractServiceInterface {

	/**
	 * Get all CIs for the given application
	 * 
	 * @return
	 */
	public abstract Collection<ConfigurationItem> getConfigurationItems(Long applicationId);

	/**
	 * Load a CI for the application by it's identifier.
	 * 
	 * @param applicationId
	 * @param identifier
	 * @return
	 */
	public abstract ConfigurationItem get(Long applicationId, String identifier);

	/**
	 * Save an item, possibly overwriting an existing value.
	 * 
	 * @param applicationId
	 * @param identifier
	 * @param value
	 * @param valueType
	 * @return
	 */
	public abstract ConfigurationItem save(Long applicationId, String identifier, String value);

	/**
	 * Save an item, possibly overwriting an existing value.
	 * 
	 * @param applicationId
	 * @param identifier
	 * @param value
	 * @param valueType
	 * @return
	 */
	public abstract ConfigurationItem save(Long applicationId, String identifier, String value, String valueType);

}
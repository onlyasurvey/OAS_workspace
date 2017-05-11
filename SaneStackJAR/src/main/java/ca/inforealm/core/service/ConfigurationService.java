package ca.inforealm.core.service;

import java.util.Collection;

import ca.inforealm.core.model.ConfigurationItem;

/**
 * Service for managing application configuration.
 * 
 * TODO performDifferentialReload
 * 
 * @author Jason Mroz
 */
public interface ConfigurationService extends AbstractServiceInterface {

	/**
	 * Reload all configuration items.
	 */
	public void reloadConfiguration();

	/**
	 * Return the configuration item matching the identifier, if any.
	 * 
	 * @param identifier
	 * @return
	 */
	public ConfigurationItem getConfigurationItem(String identifier);

	/**
	 * Is there a value for the configuration item matching the given
	 * identifier?
	 * 
	 * @param identifier
	 * @param language
	 *            (Optional) Language to restrict to.
	 * @return boolean
	 */
	public boolean hasConfigurationItem(String identifier);

	/**
	 * Retrieve all configuration items for the application.
	 * 
	 * @return
	 */
	public Collection<ConfigurationItem> getConfigurationItems();

	/**
	 * Set (and possibly overwrite) zero or one configuration item value for the
	 * item defined by the identifier in this application.
	 * 
	 * @param identifier
	 * @param singleUnilingualValue
	 * @param language
	 *            (Optional) Language to restrict to.
	 */
	public void setConfigurationItem(String identifier, String value);

	/**
	 * Set (and possibly overwrite) zero or one configuration item value for the
	 * item defined by the identifier in this application.
	 * 
	 * @param identifier
	 * @param value
	 * @param valueType
	 *            "string", "boolean", "number", "url", null
	 */
	public void setConfigurationItem(String identifier, String value, String valueType);
}

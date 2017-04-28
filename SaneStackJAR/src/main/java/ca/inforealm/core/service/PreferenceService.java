package ca.inforealm.core.service;

import java.util.Map;

/**
 * Defines a service for accessing user preferences in a specific application
 * context, obtained via saneContext.
 * 
 * Requires a properly configured SaneConext.
 */
public interface PreferenceService extends AbstractServiceInterface {

	/**
	 * Return a collection of all user preferences for the current user in this
	 * application.
	 * 
	 * @return
	 */
	public Map<String, String> getPreferences();

	/**
	 * Update all of the current user's preferences for this application.
	 * 
	 * @return
	 */
	public void setPreferences(Map<String, String> preferences);

	/**
	 * Determine if the current user has the named preference.
	 * 
	 * @param identifier
	 */
	public boolean hasPreference(String identifier);

	/**
	 * Return the value of the current user's preference.
	 * 
	 * @param identifier
	 * @return String preference (may be empty), or null if no such preference
	 *         is set.
	 */
	public String getPreference(String identifier);

	/**
	 * Set a preference, overwriting any existing value. Empty strings are
	 * stored, but if value is null then the preference value is removed
	 * completely.
	 * 
	 * @param identifier
	 * @param value
	 * @return
	 */
	public void setPreference(String identifier, String value);

}

package ca.inforealm.core;

import java.util.Collection;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.model.RoleDefinition;

public interface SaneContext {

	/**
	 * Reload all used domain model objects.
	 */
	public abstract void reloadAllModels();

	/**
	 * Reload the applicationModel based on applicationIdentifier. Ensure that
	 * this is called if you change applicationIdentifier
	 */
	public abstract void reloadApplicationModel();

	/**
	 * Reload role definitions.
	 */
	public abstract void reloadRoleDefinitions();

	/**
	 * Reload role definitions.
	 */
	public abstract void reloadPreferenceDefinitions();

	/**
	 * Returns the Application model connected to the current Hibernate session;
	 * not a reference to this.applicationIdentifier (they are neither == nor
	 * .equals()).
	 * 
	 * @return Application model
	 */
	public abstract Application getMergedApplicationModel();

	public abstract void assertRoleExists(String identifier);

	public abstract RoleDefinition getRoleDefinition(String identifier);

	public abstract PreferenceDefinition getPreferenceDefinition(String identifier);

	/**
	 * @return the roleDefinitions
	 */
	public abstract Collection<RoleDefinition> getRoleDefinitions();

	/**
	 * @return the preferenceDefinitions
	 */
	public abstract Collection<PreferenceDefinition> getPreferenceDefinitions();

	/**
	 * @return the applicationIdentifier
	 */
	public abstract String getApplicationIdentifier();

	/**
	 * @param applicationIdentifier
	 *            the applicationIdentifier to set
	 */
	public abstract void setApplicationIdentifier(String applicationIdentifier);

	/**
	 * @return the applicationModel
	 */
	public abstract Application getApplicationModel();

	/**
	 * @param deferApplicationModelLoading
	 *            the deferApplicationModelLoading to set
	 */
	public abstract void setDeferApplicationModelLoading(boolean deferApplicationModelLoading);

}
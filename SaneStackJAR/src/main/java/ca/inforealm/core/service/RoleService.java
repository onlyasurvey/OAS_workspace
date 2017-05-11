package ca.inforealm.core.service;

import java.util.Collection;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.RoleDefinition;

/**
 * Application service for interacting with roles.
 * 
 * @author Jason Mroz
 * 
 */
public interface RoleService extends AbstractServiceInterface {

	/**
	 * Throw an AccessDeniedException if the current user does not have the
	 * given role in this application.
	 * 
	 * @param identifier
	 */
	public void assertHasRole(String identifier);

	/**
	 * Determine if the current user has the given role for this application.
	 * 
	 * @param identifier
	 * @return
	 */
	public boolean hasRole(String identifier);

	/**
	 * Get all roles the current user has in this application as a collection of
	 * strings matching RoleDefinition.identifier
	 * 
	 * @return
	 */
	public Collection<String> getRoles();

	/**
	 * Retrieve the (cached) role definition specified in the context of the
	 * current application.
	 * 
	 * @throws IllegalArgumentException
	 *             if the role doesn't exist
	 * @param identifier
	 * @return RoleDefinition
	 */
	public RoleDefinition getRoleDefinition(String identifier);

	/**
	 * Retrieve all of the (cached) roles defined for the current application.
	 * 
	 * @return
	 */
	public Collection<RoleDefinition> getAllRoleDefinitions();

	/**
	 * Assign a role to the current user for the this application. Requires that
	 * the user be authenticated, but does not require any roles to be assigned
	 * yet.
	 */
	public void assignRole(String identifier);

	/**
	 * Assign a role to the specified user for the this application. Does not
	 * require any pre-existing authentication.
	 */
	public void assignRole(Actor actor, String identifier);
}

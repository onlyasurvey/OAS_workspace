package ca.inforealm.coreman.service;

import java.util.Collection;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.RoleDefinition;

public interface RoleManagementService {

	/**
	 * Assert that the current user has the RoleDefinition.roleIdentifier for
	 * the given RoleDefinition.application.id.
	 * 
	 * @throws IllegalStateException
	 *             if user is missing the role
	 */
	public abstract void assertHasApplicationRole(Long applicationId, String roleIdentifier);

	/**
	 * Load any role definition.
	 * 
	 * @param id
	 * @return
	 */
	public abstract RoleDefinition load(Long id);

	/**
	 * Load all users who have the given role in the application.
	 * 
	 * @param applicationId
	 * @param roleIdentifier
	 */
	public abstract Collection<Actor> getRoleMembers(Long applicationId, String roleIdentifier);

	/**
	 * Assign the given actor to the role.
	 * 
	 * @param actorId
	 * @param roleId
	 */
	public abstract void assignRole(Actor actor, RoleDefinition role);

	/**
	 * Revoke a role from a user.
	 * 
	 * @param actorId
	 * @param role
	 */
	public abstract void revokeRole(Actor actor, RoleDefinition role);

}
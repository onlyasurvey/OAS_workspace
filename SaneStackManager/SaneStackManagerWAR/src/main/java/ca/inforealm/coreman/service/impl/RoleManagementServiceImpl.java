package ca.inforealm.coreman.service.impl;

import java.util.Collection;

import org.springframework.security.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;
import ca.inforealm.coreman.service.RoleManagementService;

/**
 * Allows the SADMAN application to manage all application data.
 * 
 * @author Jason Mroz
 */
public class RoleManagementServiceImpl extends AbstractServiceImpl implements RoleManagementService {

	/**
	 * Assert that the current user has the RoleDefinition.roleIdentifier for
	 * the given RoleDefinition.application.id.
	 * 
	 * @throws IllegalStateException
	 *             if user is missing the role
	 */
	@Unsecured
	public void assertHasApplicationRole(Long applicationId, String roleIdentifier) {

		Assert.notNull(applicationId);
		Assert.hasText(roleIdentifier);

		try {
			// security
			requireSecureContext();

			// get relevant data
			Object[] queryParams = new Object[] { roleIdentifier, applicationId, getCurrentUser() };
			Collection<ActorRole> retval = find("from ActorRole ar where ar.role.identifier = ? AND ar.role.application.id = ? and actor = ?", queryParams);

			// sanity checks
			Assert.state(retval != null, "find should never return null (possible bug)");
			Assert.state(retval.size() == 1, "assertHasApplicationRole: current user is missing a required role");
		} catch (IllegalStateException e) {
			throw new AccessDeniedException("missing role in application: " + roleIdentifier, e);
		}
	}

	@Override
	@Unsecured
	public RoleDefinition load(Long id) {

		Assert.notNull(id);

		// load data
		RoleDefinition retval = (RoleDefinition) get(RoleDefinition.class, id);

		// sanity checks: illegal argument exception since it's input that was
		// bad
		Assert.notNull(retval, "find should never return null (possible bug)");

		return retval;
	}

	@Override
	@Unsecured
	public Collection<Actor> getRoleMembers(Long applicationId, String roleIdentifier) {

		Assert.notNull(applicationId);
		Assert.hasText(roleIdentifier);

		// security
		requireSecureContext();

		// get relevant data
		Object[] queryParams = new Object[] { roleIdentifier, applicationId };
		Collection<Actor> retval = find("select ar.actor from ActorRole ar where ar.role.identifier = ? AND ar.role.application.id = ?", queryParams);

		// sanity checks: illegal state because find() should never return null
		Assert.state(retval != null, "find should never return null (possible bug)");

		return retval;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Unsecured
	public void assignRole(Actor actor, RoleDefinition role) {

		Assert.notNull(actor);
		Assert.notNull(role);

		if (getActorRole(actor, role) != null) {
			// already assigned
			return;
		}

		//
		ActorRole ar = new ActorRole(actor, role);
		getHibernateTemplate().persist(ar);

		Assert.notNull(ar.getId());
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Unsecured
	public void revokeRole(Actor actor, RoleDefinition role) {
		// load the existing record
		ActorRole subject = getActorRole(actor, role);
		if (subject == null) {
			// no such assignment
			return;
		}

		// assignment exists: delete it
		getHibernateTemplate().delete(subject);
	}

	// ======================================================================

	/**
	 * Return the ActorRole link from the backend, if any, that represents the
	 * two passed objects.
	 */
	protected ActorRole getActorRole(Actor actor, RoleDefinition role) {

		Assert.notNull(actor, "missing actor");
		Assert.notNull(role, "missing role");

		ActorRole retval = null;

		Collection<ActorRole> list = find("from ActorRole where actor = ? and role = ?", new Object[] { actor, role });

		// may be empty but should never be null
		Assert.notNull(list, "error getting list");

		if (list.size() > 0) {
			retval = list.iterator().next();
		}

		return retval;
	}

}

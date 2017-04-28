package ca.inforealm.core.service.impl;

import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.RoleService;
import ca.inforealm.core.service.dao.RoleServiceDAO;

/**
 * Role service for determine user's roles in the current application.
 * 
 * @author Jason Mroz
 * 
 */
@Transactional
public class RoleServiceImpl extends AbstractServiceImpl implements RoleService, InitializingBean {

	/** SANE context. */
	@Autowired
	private SaneContext saneContext;

	// ======================================================================

	/** Redefines the DAO for this object to expect a role service specific one. */
	/**
	 * We use a custom DAO, so override the method here.
	 * 
	 */
	protected RoleServiceDAO getCustomDAO() {
		return (RoleServiceDAO) super.getDataAccessObject();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataAccessObject, "DAO missing");
		Assert.isInstanceOf(RoleServiceDAO.class, dataAccessObject, "DAO is of wrong type: RoleServiceDAO expected");
	}

	@Override
	@Unsecured
	public void assertHasRole(String identifier) {

		try {
			// must be a valid role
			assertValidRole(identifier);

			// this is a state exception, because caller is asserting a
			// particular
			// security state
			Assert.state(hasRole(identifier), "required role missing: " + identifier);
		} catch (IllegalStateException e) {
			throw new AccessDeniedException("missing required role: " + identifier, e);
		}
	}

	@Override
	@Unsecured
	public Collection<String> getRoles() {

		// ensure we're operating in a secure context
		requireSecureContext();

		// simple delegation to DAO
		return getCustomDAO().getRoles(getCurrentUser(), saneContext.getApplicationModel());
	}

	@Override
	@Unsecured
	public boolean hasRole(final String identifier) {

		// ensure we're operating in a secure context but DO NOT require that
		// the current user have roles, since the caller may be checking that
		requireSecureContext(false);

		// must be a valid role
		assertValidRole(identifier);

		// simple delegation to DAO
		return getCustomDAO().hasRole(getCurrentUser(), saneContext.getApplicationModel(), identifier);
	}

	// ======================================================================

	@Override
	@Unsecured
	public void assignRole(String identifier) {

		// must have a valid security context but no pre-existing role
		// assignments are required
		requireSecureContext(false);

		// delegate
		assignRole(getCurrentUser(), identifier);
	}

	@Override
	@Unsecured
	public void assignRole(Actor actor, String identifier) {

		// must be a valid role
		assertValidRole(identifier);

		// source data
		RoleDefinition role = saneContext.getRoleDefinition(identifier);

		// simple delegation to DAO
		getCustomDAO().assignRole(actor, role);
	}

	// ======================================================================

	/**
	 * Get a role definition for an identifier from the SANE context.
	 * 
	 * @return RoleDefinition
	 */
	@Override
	@Unsecured
	public RoleDefinition getRoleDefinition(String identifier) {
		return saneContext.getRoleDefinition(identifier);
	}

	/**
	 * Get all role definitions from the SANE context as it was last loaded.
	 * 
	 * @return Collection<RoleDefinition>
	 */
	@Override
	@Unsecured
	public Collection<RoleDefinition> getAllRoleDefinitions() {
		return saneContext.getRoleDefinitions();
	}

	// ======================================================================

	/**
	 * Ensures that the given role identifier is not empty and that a
	 * corresponding role is defined for this application.
	 */
	protected void assertValidRole(String identifier) {

		Assert.hasText(identifier, "identifier required");
		RoleDefinition role = getRoleDefinition(identifier);
		Assert.notNull(role, "no such role exists: " + identifier);
		Assert.state(identifier.equals(role.getIdentifier()), "BUG");
	}

}

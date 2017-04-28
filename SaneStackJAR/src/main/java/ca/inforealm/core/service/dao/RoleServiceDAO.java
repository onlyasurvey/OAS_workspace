package ca.inforealm.core.service.dao;

import java.util.Collection;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;

public interface RoleServiceDAO {

	public abstract Collection<String> getRoles(final Actor user, final Application application);

	public abstract boolean hasRole(final Actor actor, final Application application, final String identifier);

	public abstract void assignRole(final Actor actor, final RoleDefinition role);

}
package ca.inforealm.core.security.dao;

import java.util.Collection;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;

public interface UserDetailsDAO {

	public abstract UserAccount findUserByUsernameOrEmail(String username);

	public abstract Collection<RoleDefinition> findUserRoles(Application application, Actor actor);

}
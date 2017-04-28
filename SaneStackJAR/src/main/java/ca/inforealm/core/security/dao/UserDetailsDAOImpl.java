package ca.inforealm.core.security.dao;

import java.util.Collection;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.persistence.impl.DataAccessObjectImpl;

public class UserDetailsDAOImpl extends DataAccessObjectImpl implements UserDetailsDAO {

	protected static final String QUERY_USERNAME = "from UserAccount where lower(username)=lower(?)";
	protected static final String QUERY_EMAIL = "from UserAccount where lower(email)=(?)";
	protected static final String QUERY_USER_ROLES = "select a.role from ActorRole a where a.role.application = ? and a.actor = ?";

	/* (non-Javadoc)
	 * @see ca.inforealm.core.security.dao.UserDetailsDAO#findUserByUsernameOrEmail(java.lang.String)
	 */
	public UserAccount findUserByUsernameOrEmail(String username) {

		UserAccount realUser = null;
		String theQuery = QUERY_USERNAME;

		if (username.contains("@")) {
			// email
			theQuery = QUERY_EMAIL;
		}

		// find the user
		Collection<UserAccount> list = find(theQuery, username);
		if (list.size() == 1) {
			realUser = list.iterator().next();
		}

		return realUser;
	}

	/* (non-Javadoc)
	 * @see ca.inforealm.core.security.dao.UserDetailsDAO#findUserRoles(ca.inforealm.core.model.Application, ca.inforealm.core.model.Actor)
	 */
	public Collection<RoleDefinition> findUserRoles(Application application, Actor actor) {
		return find(QUERY_USER_ROLES, new Object[] { application, actor });
	}
}

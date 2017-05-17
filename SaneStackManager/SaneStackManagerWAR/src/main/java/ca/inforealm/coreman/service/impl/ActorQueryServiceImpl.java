package ca.inforealm.coreman.service.impl;

import java.util.Collection;

import org.springframework.security.annotation.Secured;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.core.service.impl.AbstractServiceImpl;
import ca.inforealm.coreman.service.ActorQueryService;

/**
 * Allows the SADMAN application to manage all application data.
 * 
 * @author Jason Mroz
 */
public class ActorQueryServiceImpl extends AbstractServiceImpl implements ActorQueryService {

	protected final static String QUERY_FIND_USER_BY_ANY = "from UserAccount where username = ? or email = ?";

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public Collection<UserAccount> findUserByAny(String query) {

		Object[] params = new Object[] { query, query };
		Collection<UserAccount> retval = find(QUERY_FIND_USER_BY_ANY, params);
		return retval;
	}
}

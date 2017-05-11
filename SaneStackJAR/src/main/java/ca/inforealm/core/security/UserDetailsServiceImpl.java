package ca.inforealm.core.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.dao.UserDetailsDAO;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

/**
 * Implementation of UserDetailsService that populates GrantedAuthority[] with
 * those roles that the user has for the current SANE application.
 * 
 */
@Transactional
public class UserDetailsServiceImpl extends AbstractServiceImpl implements UserDetailsService {

	/** SANE context. */
	@Autowired
	private SaneContext saneContext;

	/**
	 * We use a custom DAO, so override the method here.
	 */
	protected UserDetailsDAO getCustomDAO() {
		return (UserDetailsDAO) super.getDataAccessObject();
	}

	@Override
	@Unsecured
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		//
		if (!StringUtils.hasLength(username)) {
			return null;
		}

		// sanity
		Assert.hasLength(username);
		Assert.state(username.length() < 256, "huge username supplied: not valid");
		Assert.notNull(dataAccessObject, "DAO unconfigured");

		// our return value
		RealUserWrapper retval = null;

		// a real backend user
		UserAccount realUser = getCustomDAO().findUserByUsernameOrEmail(username);

		// build acegi's required object
		if (realUser != null) {
			GrantedAuthorityImpl[] ga;
			int gaIndex = 0;

			// query for roles in this application
			Application application = saneContext.getApplicationModel();

			Collection<RoleDefinition> roles = getCustomDAO().findUserRoles(application, realUser);
			Assert.notNull(roles, "should at least be an empty collection");

			ga = new GrantedAuthorityImpl[roles.size()];

			// populate array of roles -- in this application
			for (RoleDefinition role : roles) {
				String authority = role.getIdentifier();
				// log.debug("granting " + authority + " to user " +
				// realUser.getUsername());
				ga[gaIndex++] = new GrantedAuthorityImpl(authority);
			}

			// done
			retval = new RealUserWrapper(realUser.getUsername(), realUser.getMd5password(), true, true, true, true, ga);
			retval.setRealUser(realUser);

		} else {
			// no such user

			// chop string to avoid huge input from a user DOSing a server since
			// $username is likely from them and mildly (if at all) filtered
			int maxlen = username.length();
			if (maxlen > 50) {
				maxlen = 50;
			}
			String debugUsername = username.substring(0, maxlen);
			log.debug("no user found: " + debugUsername);
		}

		return retval;
	}
}

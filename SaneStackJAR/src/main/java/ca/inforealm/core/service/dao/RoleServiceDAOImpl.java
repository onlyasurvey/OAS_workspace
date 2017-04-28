package ca.inforealm.core.service.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.persistence.impl.DataAccessObjectImpl;

public class RoleServiceDAOImpl extends DataAccessObjectImpl implements RoleServiceDAO {

	/**
	 * Query to retrieve all role definitions pertaining to the current user in
	 * this application.
	 */
	private static final String QUERY_GET_ROLES = "select ar.role from ActorRole ar where ar.actor = ? and ar.role.application = ?";

	/**
	 * Query to determine if the given user has a role in the current
	 * application.
	 */
	private static final String QUERY_HAS_ROLE = "select count(ar) from ActorRole ar where "
			+ "ar.actor = ? and ar.role.application = ? and ar.role.identifier = ?";

	// ======================================================================

	/* (non-Javadoc)
	 * @see ca.inforealm.core.service.dao.RoleServiceDAO#getRoles(ca.inforealm.core.model.Actor, ca.inforealm.core.model.Application)
	 */
	public Collection<String> getRoles(final Actor user, final Application application) {

		Collection<String> retval = new ArrayList<String>();

		Object[] queryParams = new Object[] { user, application };
		Collection<RoleDefinition> list = find(QUERY_GET_ROLES, queryParams);

		for (RoleDefinition role : list) {
			retval.add(role.getIdentifier());
		}

		return retval;
	}

	/* (non-Javadoc)
	 * @see ca.inforealm.core.service.dao.RoleServiceDAO#hasRole(ca.inforealm.core.model.Actor, ca.inforealm.core.model.Application, java.lang.String)
	 */
	public boolean hasRole(final Actor actor, final Application application, final String identifier) {

		long count = (Long) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				// TODO Auto-generated method stub
				Long retval = (Long) session.createQuery(QUERY_HAS_ROLE).setParameter(0, actor).setParameter(1, application)
						.setParameter(2, identifier).uniqueResult();
				return retval;
			}
		});

		return (0 != count);
	}

	/* (non-Javadoc)
	 * @see ca.inforealm.core.service.dao.RoleServiceDAO#assignRole(ca.inforealm.core.model.Actor, ca.inforealm.core.model.RoleDefinition)
	 */
	public void assignRole(final Actor actor, final RoleDefinition role) {

		// source data
		Assert.notNull(actor, "no actor specified");
		Assert.notNull(role, "no role definition specified");

		// join data
		ActorRole subject = new ActorRole(actor, role);
		getHibernateTemplate().persist(subject);

		// sanity
		Assert.notNull(subject.getId(), "no ID was assigned during persist");
	}
}

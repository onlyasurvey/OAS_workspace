package ca.inforealm.coreman.service.impl;

import java.util.List;

import org.springframework.security.annotation.Secured;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.core.service.impl.AbstractServiceImpl;
import ca.inforealm.coreman.service.ApplicationManagementService;

/**
 * Allows the SADMAN application to manage all application data.
 * 
 * @author Jason Mroz
 */
public class ApplicationManagementServiceImpl extends AbstractServiceImpl implements ApplicationManagementService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.service.ApplicationManagementService#getApplicationsForAdmin()
	 */
	@Secured( { GlobalRoles.ROLE_USER })
	public List<Application> getApplicationsForAdmin() {

		requireSecureContext();

		final String QUERY = "select distinct(ar.role.application) from ActorRole ar where ar.actor.id = ? and ar.role.identifier = ? order by ar.role.application.identifier";

		final Object[] params = new Object[] { getCurrentUser().getId(), GlobalRoles.ROLE_APPLICATION_ADMIN };
		List<Application> retval = (List<Application>) getHibernateTemplate().find(QUERY, params);

		return retval;
	}

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public Application load(Long id) {
		requireSecureContext();

		List<Application> list = (List<Application>) getHibernateTemplate().find("from Application where id = ?", id);

		// check data
		Assert.notNull(list, "list is null (possible bug)");
		Assert.state(list.size() == 1);

		// done
		return list.get(0);
	}
}

package com.oas.service.maintenance.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jfree.util.Log;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Response;
import com.oas.service.maintenance.ResponseMaintenanceService;

/**
 * Implementation of {@link ResponseMaintenanceService}.
 * 
 * @author xhalliday
 * @since 2009-10-16
 */
public class ResponseMaintenanceServiceImpl extends AbstractServiceImpl implements ResponseMaintenanceService {

	/** Number of days old a partial response is before being deleted. */
	/* package */static final int PARTIAL_RESPONSE_EXPIRES_DAYS = 90;

	/** Maximum number of partial responses to delete in one batch. */
	/* package */static final int MAXIMUM_PARTIAL_RESPONSE_BATCH_SIZE = 100;

	// ======================================================================

	/** {@inheritDoc} */
	@Override
	@Transactional
	@Unsecured
	public void cleanUpPartialResponses() {

		log.info("Starting clean-up of partial responses");

		List<Response> list = getBatchOfPartialResponsesToDelete();
		Assert.notNull(list);

		log.info("Found " + list.size() + " partial responses to delete");
		Assert.isTrue(list.size() <= MAXIMUM_PARTIAL_RESPONSE_BATCH_SIZE, "unexpect amount of responses to delete: "
				+ list.size());

		for (Response response : list) {
			log.info("Deleting partial response #" + response.getId() + " (created: " + response.getCreated()
					+ "): it has expired");
			delete(response);
		}

		// paranoia: flush changes immediately
		getHibernateTemplate().flush();

		log.info("Finished clean-up of partial responses: " + list.size() + " deleted");
	}

	// ======================================================================

	/**
	 * Get a batch of {@link Response}s to delete.
	 * 
	 * @return List<Response>
	 */
	/* package */List<Response> getBatchOfPartialResponsesToDelete() {

		// determines the lastest date that may be used to delete a response
		Date newestDateToDelete = getNewestPartialResponseDateToDelete();
		Assert.isTrue(newestDateToDelete.before(new Date()), "sanity check failed: newest partial delete date is in the future");

		DetachedCriteria crit = DetachedCriteria.forClass(Response.class);

		crit.add(Restrictions.eq("closed", false));
		crit.add(Restrictions.lt("created", newestDateToDelete));

		// oldest first
		crit.addOrder(Order.asc("created"));

		//
		Criteria exec = crit.getExecutableCriteria(getHibernateTemplate().getSessionFactory().getCurrentSession());

		// limited buffer
		exec.setMaxResults(MAXIMUM_PARTIAL_RESPONSE_BATCH_SIZE);

		@SuppressWarnings("unchecked")
		List<Response> retval = exec.list();

		Log.info("Returning " + retval.size() + " partial responses to delete");

		//
		return retval;
	}

	/**
	 * Determines the newest dateCreated of a {@link Response} to deleted.
	 * 
	 * @return {@link Date}
	 */
	/* package */Date getNewestPartialResponseDateToDelete() {

		// now()
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(new Date());

		// roll back X days
		calendar.add(Calendar.DAY_OF_YEAR, PARTIAL_RESPONSE_EXPIRES_DAYS * -1);

		//
		Date retval = calendar.getTime();
		log.info("Returning " + retval + " as newest response date to delete for partial responses");
		return retval;
	}
}

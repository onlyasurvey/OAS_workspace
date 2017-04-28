package com.oas.service.enterprise;

import java.util.Collection;
import java.util.List;

import com.oas.model.AccountOwner;
import com.oas.model.ContactUsMessage;
import com.oas.model.Survey;
import com.oas.model.enterprise.QuickStats;

/**
 * Provides "dashboard" functionality for the Enterprise package.
 * 
 * @author xhalliday
 * @since October 19, 2008
 */
public interface EnterpriseDashboardService extends AbstractEnterpriseService {

	/**
	 * Return all <code>ContactUsMessage</code>s that exist in the system.
	 * 
	 * @param page
	 *            Page to start with
	 * @param count
	 *            Number of records to return
	 * 
	 * @return Collection of <code>ContactUsMessage</code>s
	 */
	Collection<ContactUsMessage> findContactUsMessages(int page, int count);

	/**
	 * Determine how many <code>ContactUsMessage</code>s exist.
	 * 
	 * @return int count
	 */
	int countContactUsMessages();

	/**
	 * List account owners, with the paging values specified, in alphabetical
	 * order by username.
	 * 
	 * @param page
	 *            Page to start with
	 * @param count
	 *            Number of records to return
	 * 
	 * @return Collection of <code>AccountOwner</code>s
	 */
	Collection<AccountOwner> findAccountOwners(int page, int count);

	/**
	 * Determine how many <code>AccountOwner</code>s exist.
	 * 
	 * @return
	 */
	int countAccountOwners();

	/**
	 * Find all Surveys owned by the given user.
	 * 
	 * @param owner
	 * @param includeDeleted
	 * @return
	 */
	List<Survey> findSurveysFor(AccountOwner owner, boolean includeDeleted);

	/**
	 * Gather enterprise-stats-at-a-glance for a dashboard view.
	 * 
	 * @return {@link QuickStats}
	 */
	QuickStats getQuickStats();
}

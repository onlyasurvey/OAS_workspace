package com.oas.service.enterprise.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.AccountOwner;
import com.oas.model.ContactUsMessage;
import com.oas.model.Survey;
import com.oas.model.enterprise.QuickStats;
import com.oas.service.DomainModelService;
import com.oas.service.enterprise.EnterpriseDashboardService;
import com.oas.util.EnterpriseRoles;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class EnterpriseDashboardServiceImpl extends AbstractServiceImpl implements EnterpriseDashboardService {

	/** Service for general domain model operations. */
	@Autowired
	private DomainModelService domainModelService;

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public Collection<ContactUsMessage> findContactUsMessages(int page, int count) {
		return domainModelService.getPagedList("from ContactUsMessage order by created desc", page, count);
	}

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public int countContactUsMessages() {
		// 
		return ((Long) unique(find("select count(*) from ContactUsMessage"))).intValue();
	}

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public Collection<AccountOwner> findAccountOwners(int page, int count) {

		Collection<AccountOwner> retval = domainModelService.getPagedList("from AccountOwner order by joinDate asc", page, count);
		return retval;
	}

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public int countAccountOwners() {
		// 
		int retval = ((Long) unique(find("select count(*) from AccountOwner"))).intValue();
		return retval;
	}

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public List<Survey> findSurveysFor(AccountOwner owner, boolean includeDeleted) {
		String query = "from Survey where owner = ?";
		if (!includeDeleted) {
			query += " and deleted = false";
		}

		return find(query, owner);
	}

	// ======================================================================

	@Override
	@Secured(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN)
	public QuickStats getQuickStats() {

		QuickStats retval = (QuickStats) unique(find("from QuickStats"));
		Assert.notNull(retval, "enterprise-quick-stats should always be available");

		return retval;
	}

	// ======================================================================
}

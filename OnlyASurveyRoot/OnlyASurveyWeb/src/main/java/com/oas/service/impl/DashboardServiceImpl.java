package com.oas.service.impl;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Survey;
import com.oas.model.report.SurveySummary;
import com.oas.service.DashboardService;

/**
 * Service for interacting with surveys.
 * 
 * @author Jason Halliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DashboardServiceImpl extends AbstractServiceImpl implements DashboardService {

	@ValidUser
	public Collection<Survey> findSurveys() {

		return find("from Survey where owner = ? and deleted = false order by created desc", getCurrentUser());
	}

	@ValidUser
	public Collection<SurveySummary> findSurveySummaries() {

		return find("from SurveySummary where survey.owner = ? order by survey.created desc", getCurrentUser());
	}
}

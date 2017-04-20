package com.oas.service;

import java.util.Collection;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Survey;
import com.oas.model.report.SurveySummary;

public interface DashboardService extends AbstractServiceInterface {

	/**
	 * Find all surveys for the current user that are not deleted.
	 */
	Collection<Survey> findSurveys();

	/**
	 * Find all ResponsesTotal summaries for the current user. This will load
	 * the same surveys as findSurveys() but also provides summary info. Only
	 * includes Surveys that are not deleted
	 * 
	 */
	Collection<SurveySummary> findSurveySummaries();

}

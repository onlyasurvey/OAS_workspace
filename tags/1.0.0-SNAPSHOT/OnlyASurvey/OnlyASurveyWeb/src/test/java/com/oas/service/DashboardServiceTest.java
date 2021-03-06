package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.report.SurveySummary;

public class DashboardServiceTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private DashboardService dashboardService;

	@Test
	public void testFindSurveys() {

		getHibernateTemplate().persist(scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser()));

		Collection<Survey> list = dashboardService.findSurveys();
		assertNotNull(list);
		assertTrue("should have 1 survey", list.size() > 0);
		assertFalse("SANITY: huge dataset returned REFACTOR", list.size() > 20);
	}

	@Test
	public void testFindSurveySummaries() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.addHistoricalResponseData(survey);

		Collection<SurveySummary> list = dashboardService.findSurveySummaries();
		assertNotNull(list);
		assertTrue("should have 1 survey (scenario data changed?)", list.size() == 1);
		assertEquals("unexpected count (scenario data changed?)", Long.valueOf(5L), list.iterator().next().getCount());
	}
}

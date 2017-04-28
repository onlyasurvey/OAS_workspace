package com.oas.controller.dashboard.report;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.report.ResponsesPerMonth;

public class ReportsPageControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ReportsPageController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void testDoShowSurveyList_Success() {
		scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ModelAndView mav = controller.doShowSurveyList();

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull("missing survey list", mav.getModel().get("list"));

		@SuppressWarnings("unchecked")
		Collection<Survey> list = (Collection<Survey>) mav.getModel().get("list");

		assertEquals("should have 1 survey", 1, list.size());
	}

	@Test
	public void testDoShowReportList_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");
		ModelAndView mav = controller.doShowReportList(request);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull("missing survey", mav.getModel().get("survey"));

		@SuppressWarnings("unchecked")
		List<ResponsesPerMonth> byMonth = (List<ResponsesPerMonth>) mav.getModel().get("byMonth");
		assertNotNull("Missing byMonth data", byMonth);
		assertTrue("should have some historical data", byMonth.size() > 1);
	}

}

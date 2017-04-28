package com.oas.controller.dashboard.report;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.report.ResponsesPerDay;

public class ResponsesPerDayControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ResponsesPerDayController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void getPng_Success() throws IOException {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		// extra historical-like
		scenarioDataUtil.addHistoricalResponseData(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		ModelAndView mav = controller.doReport(request, response);
		assertNull("should NOT be mav: it streams a PNG", mav);
		assertFalse("should NOT be empty buffer", 0 == response.getContentAsByteArray().length);
	}

	@Test
	public void doReportDetails_Success() throws IOException {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		// extra historical-like
		scenarioDataUtil.addHistoricalResponseData(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");

		// invoke report
		ModelAndView mav = controller.doReportDetails(request);
		assertHasViewNamePart(mav, "responsesPerDay");
		List<ResponsesPerDay> list = (List<ResponsesPerDay>) mav.getModel().get("data");
		assertNotNull("no data", list);
		assertTrue("no items in list", list.size() > 0);
		int total = 0;
		for (ResponsesPerDay day : list) {
			total += day.getCount();
		}
		assertTrue("data is all zeros", total > 0);
	}

	@Test
	public void getPng_Success_WithZeroData() throws IOException {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey initialSurvey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		getHibernateTemplate().persist(initialSurvey);
		flushAndClear();

		Survey survey = surveyService.findNonDeletedSurvey(initialSurvey.getId());
		assertNotNull(survey);
		assertEquals("should have zero responses", Integer.valueOf(0), surveyService.countResponses(survey));

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		ModelAndView mav = controller.doReport(request, response);
		assertNull("should NOT be mav: it streams a PNG", mav);
		assertFalse("should NOT be empty buffer", 0 == response.getContentAsByteArray().length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPng_Fails_InvalidId() throws IOException {
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/-9238.html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		controller.doReport(request, response);
	}

	@Test(expected = AccessDeniedException.class)
	public void getPng_Security_Fail_NoUser() throws IOException {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		controller.doReport(request, response);
	}
}

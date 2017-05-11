package com.oas.controller.dashboard.report;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;

public class ResponsesPerDayControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ResponsesPerDayController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void testGetPng_Success() throws IOException {
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
	public void testGetPng_Success_WithZeroData() throws IOException {
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

	@Test
	public void testGetPng_Fails_InvalidId() throws IOException {
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/-9238.html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		try {
			controller.doReport(request, response);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testGetPng_Security_Fail_NoUser() throws IOException {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// invoke report
		try {
			controller.doReport(request, response);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
}

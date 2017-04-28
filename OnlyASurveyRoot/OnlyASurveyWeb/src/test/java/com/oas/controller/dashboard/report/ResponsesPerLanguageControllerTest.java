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
import com.oas.controller.dashboard.report.graph.ResponsesPerLanguageController;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;

/**
 * Basic functional test of the controller.
 * 
 * @author xhalliday
 * @since December 4, 2008
 */
public class ResponsesPerLanguageControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ResponsesPerLanguageController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	private Survey getTestSurvey(boolean authenticateOwner, boolean persist) {

		Survey survey;
		if (authenticateOwner) {
			survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), persist);
		} else {
			survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), persist);
		}

		if (survey.getSupportedLanguages().size() < 2) {
			// this controller's purpose is to split things by language, we need
			// >1 of them
			survey.getSupportedLanguages().clear();
			survey.getSupportedLanguages().add(supportedLanguageService.findByCode("eng"));
			survey.getSupportedLanguages().add(supportedLanguageService.findByCode("fra"));
		}

		if (persist) {
			persist(survey);
		}

		return survey;
	}

	@Test
	public void testGetPng_Success() throws IOException {
		Survey survey = getTestSurvey(true, true);
		// extra historical-like
		scenarioDataUtil.addHistoricalResponseData(survey);
		flushAndClear();

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
		Survey initialSurvey = getTestSurvey(true, false);
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

		// invoke report
		try {
			controller.doReport(request, new MockHttpServletResponse());
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testGetPng_Security_Fail_NotOwner() throws IOException {
		Survey survey = getTestSurvey(false, true);
		createAndSetSecureUserWithRoleUser();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + survey.getId() + ".html");

		// invoke report
		try {
			controller.doReport(request, new MockHttpServletResponse());
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
}

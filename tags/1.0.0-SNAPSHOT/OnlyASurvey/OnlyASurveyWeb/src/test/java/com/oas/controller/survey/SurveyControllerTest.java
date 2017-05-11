package com.oas.controller.survey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;

public class SurveyControllerTest extends AbstractOASBaseTest {

	@Autowired
	private SurveyController controller;

	@Test
	public void testDisplaySurvey_Success() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false);

		// these MUST be true to respond to a survey
		survey.setPaidFor(true);
		survey.setPublished(true);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertEquals("wrong view", "/survey/view", mav.getViewName());
	}

	@Test
	public void testDisplaySurvey_Fail_NotPublished() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request);

		survey.setPaidFor(true);
		survey.setPublished(false);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull("errors missing", mav.getModel().get("errors"));
		assertEquals("wrong view", "/survey/error", mav.getViewName());
	}

	@Test
	public void testDisplaySurvey_Fail_Unpaid() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request);

		survey.setPaidFor(false);
		survey.setPublished(true);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull("errors missing", mav.getModel().get("errors"));
		assertEquals("wrong view", "/survey/error", mav.getViewName());
	}

	// TODO displaySurvey fails if closed, not published, etc

	// ======================================================================

	@Test
	public void testCreateResponse_Success() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.createResponse(request);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertEquals("should have no view name", null, mav.getViewName());
		assertNotNull(mav.getView());
		assertTrue("not a redirect", RedirectView.class.isAssignableFrom(mav.getView().getClass()));

		// the action redirects to the first question: ensure that the redirect
		// does contain that ID
		Question question = surveyService.findFirstQuestion(survey);
		assertNotNull(question);

		// this assert is for if the action doesn't redirect to a Welcome
		// message, which is how it currently works
		// assertTrue("redirect needs to contain first question's ID",
		// ((RedirectView) mav.getView()).getUrl().contains("=" +
		// question.getId()));

		assertIsRedirect(mav);
	}
}

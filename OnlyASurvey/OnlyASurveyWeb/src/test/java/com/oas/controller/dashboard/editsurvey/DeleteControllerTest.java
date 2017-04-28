package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.controller.dashboard.DeleteController;
import com.oas.model.BaseObject;
import com.oas.model.Question;
import com.oas.model.Survey;

public class DeleteControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private DeleteController controller;

	// ======================================================================

	private ModelAndView doTestDeleteSurvey(boolean includeResponseData, boolean confirmation, boolean expectDeletion,
			boolean expectRedirect, String expectedUrlOrView) {

		Survey survey = null;
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		if (includeResponseData) {
			survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		} else {
			survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
			getHibernateTemplate().persist(survey);
		}

		Long surveyId = survey.getId();

		// flush all changes
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		if (confirmation) {
			// simulate the button press
			request.setParameter("_save", "");
		}

		// delete and expect no errors
		ModelAndView retval = controller.deleteItem(request);

		if (expectDeletion) {
			Survey loaded = surveyService.findNonDeletedSurvey(surveyId);
			assertNull("should have been deleted", loaded);
		}

		if (expectRedirect) {
			assertIsRedirect("unexpected view or non-redirect", retval, expectedUrlOrView);
		}

		return retval;
	}

	// ======================================================================

	private ModelAndView doTestDeleteQuestion(boolean includeResponseData, boolean confirmation, boolean expectDeletion,
			boolean expectRedirect, String expectedUrlOrView) {

		Survey survey = null;

		if (includeResponseData) {
			survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		} else {
			// when createTypicalScenario1 is called with persist=false, no
			// responses are created; otherwise some are
			survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
			getHibernateTemplate().persist(survey);
		}
		flushAndClear();

		// store in DB
		Long surveyId = survey.getId();

		// bit of a hack, but this is a test, and it works
		if (expectedUrlOrView.contains("${surveyId")) {
			expectedUrlOrView = expectedUrlOrView.replace("${surveyId}", surveyId.toString());
		}

		// depends on scenario data
		Question question = survey.getQuestions().get(0);
		assertNotNull("scenario data did not provide a question", question);

		// flush all changes
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		flushAndClear();

		if (confirmation) {
			// simulate the button press
			request.setParameter("_save", "");
		}

		// delete and expect no errors
		ModelAndView retval = controller.deleteItem(request);
		flushAndClear();

		if (expectDeletion) {
			Question loaded = surveyService.findQuestionById(surveyId);
			assertNull("should have been deleted", loaded);
		}

		if (expectRedirect) {
			assertIsRedirect("unexpected view or non-redirect", retval, expectedUrlOrView);
		}

		return retval;
	}

	// ======================================================================

	/**
	 * Success path for deleting a survey with no responses.
	 */
	@Test
	public void testDeleteSurvey_Success_ConfirmedAndNoResponses() {
		doTestDeleteSurvey(false, true, true, true, "/html/db/db.html");
	}

	/**
	 * Success path for showing the confirmation request before deleting
	 * something that has no response data.
	 */
	@Test
	public void testDeleteSurvey_Success_UnconfirmedAndNoResponses() {
		doTestDeleteSurvey(false, false, false, false, "/dashboard/delete/confirmation");
	}

	/**
	 * Error path for deleting a survey that does have responses.
	 */
	@Test
	public void testDeleteSurvey_Fail_HasResponses() {
		doTestDeleteSurvey(true, true, false, false, "/dashboard/delete/hasResponseDataError");
	}

	/**
	 * Error path for deleting a survey that does have responses.
	 */
	@Test
	public void testDeleteSurvey_Fail_NotOwner() {
		// some user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		// new, OTHER user
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// simulate the button press
		request.setParameter("_save", "");

		try {
			controller.deleteItem(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	/**
	 * Success path for deleting a Question with no responses.
	 */
	@Test
	public void testDeleteQuestion_Success_ConfirmedAndNoResponses() {
		doTestDeleteQuestion(false, true, true, true, "/html/db/mgt/ql/${surveyId}.html");
	}

	/**
	 * Success path for showing the confirmation request before deleting
	 * something that has no response data.
	 */
	@Test
	public void testDeleteQuestion_Success_UnconfirmedAndNoResponses() {
		doTestDeleteQuestion(false, false, false, false, "/dashboard/delete/confirmation");
	}

	/**
	 * Error path for deleting a Question that does have responses.
	 */
	@Test
	public void testDeleteQuestion_Fail_HasResponses() {
		doTestDeleteQuestion(true, true, false, false, "/dashboard/delete/hasResponseDataError");
	}

	/**
	 * Error path for deleting a Question that does have responses.
	 */
	@Test
	public void testDeleteQuestion_Fail_NotOwner() {
		// some user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		// new, OTHER user
		createAndSetSecureUserWithRoleUser();

		// depends on scenario data
		Question question = survey.getQuestions().iterator().next();
		assertNotNull("scenario data did not provide a question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

		// simulate the button press
		request.setParameter("_save", "");

		try {
			controller.deleteItem(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	/**
	 * If user goes Delete Survey | Cancel they get returned to the Manage
	 * Surveys list.
	 */
	@Test
	public void testCancel_Success_Survey() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/someUrl/" + survey.getId() + ".html");
		request.addParameter("_cancel", "");
		ModelAndView mav = controller.deleteItem(request);
		assertNotNull(mav);
		assertIsRedirect("expected redirect", mav, "/html/db/db.html");
	}

	/**
	 * If user goes Delete Question | Cancel they get returned to the Manage
	 * Surveys list.
	 */
	@Test
	public void testCancel_Success_Question() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		persist(survey);

		assertNotNull("survey data must have questions", survey.getQuestions());
		assertTrue("survey data must have questions", survey.getQuestions().size() > 0);
		Question question = survey.getQuestions().get(0);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/someUrl/" + question.getId() + ".html");
		request.addParameter("_cancel", "");
		ModelAndView mav = controller.deleteItem(request);
		assertNotNull(mav);
		assertIsRedirect("expected redirect", mav, "/html/db/mgt/ql/" + survey.getId() + ".html");
	}

	// ======================================================================

	@Test
	public void testDelete_Fail_UnknownType() {
		BaseObject object = new BaseObject();
		persist(object);
		assertNotNull("failed to persist test data", object.getId());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + object.getId() + ".html");

		try {
			controller.deleteItem(request);
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

}

package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;

abstract public class AbstractCreateQuestionControllerTest extends AbstractOASBaseTest {

	@Autowired
	protected CreateQuestionController createController;

	@Autowired
	protected EditQuestionController editController;

	// ======================================================================

	/**
	 * Invoke the method that will show the Create Question form.
	 * 
	 * @param request
	 * @return
	 */
	abstract public ModelAndView invokeCreateFormMethod(MockHttpServletRequest request);

	/**
	 * Invoke the Create Submit method.
	 * 
	 * @param request
	 */
	abstract public ModelAndView invokeCreateSubmitMethod(MockHttpServletRequest request) throws Exception;

	// ======================================================================

	/**
	 * Invoke the method that will show the Edit Question form.
	 * 
	 * @param request
	 * @return
	 */
	abstract public ModelAndView invokeEditFormMethod(MockHttpServletRequest request) throws Exception;

	/**
	 * Invoke the Edit Submit method.
	 * 
	 * @param request
	 */
	abstract public ModelAndView invokeEditSubmitMethod(MockHttpServletRequest request) throws Exception;

	// ======================================================================

	/**
	 * Set parameters into the request such that it would pass validation.
	 * 
	 * @param request
	 */
	abstract public void addValidRequestParameters(MockHttpServletRequest request);

	/**
	 * Return an existing (persistent) Question of the appropriate type for the
	 * test, used for update tests.
	 * 
	 * @return
	 */
	abstract public Question getPersistentQuestionOfAppropriateType(Survey survey);

	/**
	 * Set parameters into the request such that it would fail validation. This
	 * does not need to be exhaustive; that's the job of the validator's test,
	 * this just needs to force a fail.
	 * 
	 * @param request
	 */
	abstract public void addInvalidRequestParameters(MockHttpServletRequest request);

	/**
	 * Called after a successful Create Submit test.
	 * 
	 * @param request
	 * @param mav
	 * @param survey
	 */
	public void onCreateSubmitSuccess(HttpServletRequest request, ModelAndView mav, Survey survey) {
	}

	/**
	 * Called after a successful Edit Submit test.
	 * 
	 * @param request
	 * @param mav
	 * @param survey
	 */
	public void onEditSubmitSuccess(HttpServletRequest request, ModelAndView mav, Survey survey) {
	}

	// ======================================================================
	// CREATE QUESTION
	// ======================================================================

	@Test
	public void testCreateQuestionDoSelectType_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		ModelAndView mav = getCreateQuestionController().doSelectType(request);
		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getViewName());
		assertNotNull("no survey in model", mav.getModel().get("survey"));
		assertNotNull("no supportedLanguages in model", mav.getModel().get("supportedLanguages"));
	}

	// ======================================================================
	// Select Type tests - SEE TODO
	// ======================================================================
	/**
	 * TODO move into "Other" test case of the controller
	 */
	@Test
	public void testCreateQuestionDoSelectType_Security_FailIfNotOwner() {
		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// set the current user as not the owner above
		createAndSetSecureUserWithRoleUser();

		try {
			getCreateQuestionController().doSelectType(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException ace) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testCreateQuestionReferenceData_Security_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		try {
			// controller.referenceData(request);
			getCreateQuestionController().referenceData(request);
		} catch (AccessDeniedException e) {
			// caught here specifically to distinguish between
			// AccessDeniedExceptions (fail) and all others (error)
			fail("threw unexpected security exception");
		}
	}

	@Test
	public void testCreateQuestionReferenceData_Security_Fail_NoSecurityContext() throws Exception {
		// never gets to parse URL, no test data needed: this test ensures that
		// regardless of data, if there is no valid context (user + 1+ roles)
		// then nothing works.
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		try {
			getCreateQuestionController().referenceData(request);
			fail("should have thrown: no context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testCreateQuestionReferenceData_Security_Fail_NotOwner() throws Exception {

		// some OTHER user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// new current user
		createAndSetSecureUserWithRoleUser();
		try {
			getCreateQuestionController().referenceData(request);
			fail("should have thrown: not owner");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================
	// Form related
	// ======================================================================

	@Test
	public void testCreateQuestion_FormSuccess() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		ModelAndView mav = invokeCreateFormMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getViewName());
		assertNotNull("no survey in model", mav.getModel().get("survey"));
		assertNotNull("no supportedLanguages in model", mav.getModel().get("supportedLanguages"));
	}

	@Test
	public void testCreateQuestionForm_Security_FailIfNotOwner() {
		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// set the current user as not the owner above
		createAndSetSecureUserWithRoleUser();

		try {
			invokeCreateFormMethod(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException ace) {
			// expected
		}
	}

	// ======================================================================
	// Form-Submit related
	// ======================================================================

	@Test
	public void testCreateQuestion_Submit_Success() throws Exception {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		// Question question = getPersistentQuestionOfAppropriateType(survey);
		// assertNotNull("should have a first question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// add valid parameters
		addValidRequestParameters(request);

		request.setParameter("map[eng]", NAME_EN + "new");

		// invoke submit
		ModelAndView mav = invokeCreateSubmitMethod(request);
		assertNull("should have no errors", mav.getModel().get("errors"));
		assertTrue("should be a redirect", RedirectView.class.isAssignableFrom(mav.getView().getClass()));

		//
		flushAndClear();

		// reload and validate
		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
		// last question
		Question loadedQuestion = loaded.getQuestions().get(loaded.getQuestions().size() - 1);
		// Question loaded = surveyService.findQuestionById(question.getId());
		assertEquals("should have updated report name", NAME_EN + "new", loadedQuestion.getDisplayTitle());

		// callback
		onCreateSubmitSuccess(request, mav, survey);
	}

	/**
	 * Applicable to all question types - they need a name.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateQuestion_Submit_Fail_Validation_Names() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// first populate with valid parameters
		addValidRequestParameters(request);

		// next ignore these two:
		request.setParameter("map[eng]", "");
		request.setParameter("map[fra]", "");

		// invoke submit
		ModelAndView mav = invokeCreateSubmitMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no model", mav.getModel());
		assertNull("should have no view", mav.getView());

		// expect errors
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have errors", 0 != errors.getErrorCount());
	}

	/**
	 * Gets some invalid parameters from the implementing test to ensure the
	 * validator is called. Not exhaustive; that's the job of the validator's
	 * test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateQuestion_Submit_Fail_GenericValidation() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// add blatently invalid parameters
		addInvalidRequestParameters(request);

		ModelAndView mav = invokeCreateSubmitMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no model", mav.getModel());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have errors", 0 != errors.getErrorCount());

		assertNull("should have no view", mav.getView());
	}

	@Test
	public void testCreateQuestion_Submit_Security_FailIfNotOwner() throws Exception {
		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + survey.getId() + ".html");

		// set the current user as not the owner above
		createAndSetSecureUserWithRoleUser();

		try {
			invokeCreateSubmitMethod(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException ace) {
			// expected
		}
	}

	// ======================================================================
	//
	// EDIT QUESTION
	//
	// ======================================================================
	@Test
	public void testEditQuestionReferenceData_Security_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		try {
			// controller.referenceData(request);
			getEditQuestionController().referenceData(request);
		} catch (AccessDeniedException e) {
			// caught here specifically to distinguish between
			// AccessDeniedExceptions (fail) and all others (error)
			fail("threw unexpected security exception");
		}
	}

	@Test
	public void testEditQuestionReferenceData_Security_Fail_NoSecurityContext() throws Exception {
		// never gets to parse URL, no test data needed: this test ensures that
		// regardless of data, if there is no valid context (user + 1+ roles)
		// then nothing works.
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		try {
			getEditQuestionController().referenceData(request);
			fail("should have thrown: no context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testEditQuestionReferenceData_Security_Fail_NotOwner() throws Exception {

		// requires auth because it calls addQuestion
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		clearSecurityContext();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// new current user - NOT the owner above
		createAndSetSecureUserWithRoleUser();
		try {
			getEditQuestionController().referenceData(request);
			fail("should have thrown: not owner");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================
	// Form related
	// ======================================================================

	@Test
	public void testEditQuestion_FormSuccess() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		ModelAndView mav = invokeEditFormMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getViewName());
		assertNotNull("no survey in model", mav.getModel().get("survey"));
		assertNotNull("no supportedLanguages in model", mav.getModel().get("supportedLanguages"));
	}

	@Test
	public void testEditQuestionForm_Security_FailIfNotOwner() throws Exception {
		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		clearSecurityContext();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// set the current user as not the owner above
		createAndSetSecureUserWithRoleUser();

		try {
			invokeEditFormMethod(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException ace) {
			// expected
		}
	}

	// ======================================================================
	// Form-Submit related
	// ======================================================================

	@Test
	public void testEditQuestion_Submit_Success() throws Exception {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		assertNotNull("should have a first question", question);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// add valid parameters
		addValidRequestParameters(request);

		request.setParameter("map[eng]", NAME_EN + "new");

		// invoke submit
		ModelAndView mav = invokeEditSubmitMethod(request);
		assertNull("should have no errors", mav.getModel().get("errors"));
		assertTrue("should be a redirect", RedirectView.class.isAssignableFrom(mav.getView().getClass()));

		//
		flushAndClear();

		// reload and validate
		Question loaded = surveyService.findQuestionById(question.getId());
		assertEquals("should have updated name", NAME_EN + "new", loaded.getDisplayTitle());

		// callback
		onEditSubmitSuccess(request, mav, survey);
	}

	/**
	 * Applicable to all question types - they need a name.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEditQuestion_Submit_Fail_Validation_Names() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// first populate with valid parameters
		addValidRequestParameters(request);

		// next ignore these two:
		request.setParameter("map[eng]", "");
		request.setParameter("map[fra]", "");

		// invoke submit
		ModelAndView mav = invokeEditSubmitMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no model", mav.getModel());
		assertNull("should have no view", mav.getView());

		// expect errors
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have errors", 0 != errors.getErrorCount());
	}

	/**
	 * Gets some invalid parameters from the implementing test to ensure the
	 * validator is called. Not exhaustive; that's the job of the validator's
	 * test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEditQuestion_Submit_Fail_GenericValidation() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// add blatently invalid parameters
		addInvalidRequestParameters(request);

		ModelAndView mav = invokeEditSubmitMethod(request);
		assertNotNull("no mav", mav);
		assertNotNull("no model", mav.getModel());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have errors", 0 != errors.getErrorCount());

		assertNull("should have no view", mav.getView());
	}

	@Test
	public void testEditQuestion_Submit_Security_FailIfNotOwner() throws Exception {
		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getPersistentQuestionOfAppropriateType(survey);
		clearSecurityContext();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/somePrefix/" + question.getId() + ".html");

		// set the current user as not the owner above
		createAndSetSecureUserWithRoleUser();

		try {
			invokeEditSubmitMethod(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException ace) {
			// expected
		}
	}

	// ======================================================================

	/**
	 * @return the Create Question controller
	 */
	public CreateQuestionController getCreateQuestionController() {
		return createController;
	}

	/**
	 * @return the Edit Question controller
	 */
	public EditQuestionController getEditQuestionController() {
		return editController;
	}

}

package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

public class ManageSurveyControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ManageSurveyController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void referenceData_Success() {
		// create some other user and log in
		UserAccount owner = createAndSetSecureUserWithRoleUser();

		// create scenario data
		Survey survey = scenarioDataUtil.createTypicalScenario1(owner);
		getHibernateTemplate().persist(survey);
		flushAndClear();
		survey = (Survey) getHibernateTemplate().get(Survey.class, survey.getId());

		String invokeUrl = "/somePrefix/" + survey.getId() + ".html";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", invokeUrl);

		Map map = controller.referenceData(request);
		assertNotNull("no object name map", map.get("objectNameMap"));
		assertNotNull("no survey set", map.get("survey"));
	}

	@Test
	public void main_Success() {
		// create some other user and log in
		UserAccount owner = createAndSetSecureUserWithRoleUser();

		// create scenario data
		Survey survey = scenarioDataUtil.createTypicalScenario1(owner);
		getHibernateTemplate().persist(survey);
		flushAndClear();
		survey = (Survey) getHibernateTemplate().get(Survey.class, survey.getId());

		String invokeUrl = "/somePrefix/" + survey.getId() + ".html";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", invokeUrl);
		MockHttpServletResponse response = new MockHttpServletResponse();

		ModelAndView mav = controller.doMain(request, response);
		assertNotNull(mav);
		assertNotNull(mav.getModel());

		Survey loaded = (Survey) mav.getModel().get("survey");
		assertNotNull(loaded);
		assertNotNull(loaded.getId());
		assertEquals(survey.getId(), loaded.getId());
		assertEquals(survey.getCreated(), loaded.getCreated());

	}

	@Test
	public void main_FailsIfNotLoggedIn() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		try {
			controller.doMain(request, response);
			fail("should have thrown security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void main_FailsIfNotOwner() {
		// create a test user that is the real owner
		UserAccount otherUser = createTestUser();

		// create some other user and log in
		createAndSetSecureUserWithRoleUser();

		// create scenario data where a user that is NOT the current user owns
		// the specified survey
		Survey survey = scenarioDataUtil.createTypicalScenario1(otherUser);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		String invokeUrl = "/somePrefix/" + survey.getId() + ".html";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", invokeUrl);
		MockHttpServletResponse response = new MockHttpServletResponse();

		try {
			controller.doMain(request, response);
			fail("should have thrown security exception for " + invokeUrl);
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void changeTextSubmit_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		request.setParameter("0", "thanksMessage");
		request.setParameter("map[eng]", NAME_EN);

		ModelAndView mav = controller.changeTextSubmit(request);
		assertNotNull(mav);
		assertIsRedirect("expected redirect", mav);
	}

	/**
	 * These properties can be empty while the survey is a draft.
	 */
	@Test
	public void changeTextSubmit_Success_Empty() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		request.setParameter("0", "thanksMessage");

		// if there is nothing in the request, then we get an illegalArgument
		// error because there's ALWAYS at least one language - set an empty,
		// non-null argument
		foreachLanguage(survey, new LanguageCallback() {
			@Override
			public void doCallback(SupportedLanguage language) {
				request.addParameter("map[" + language.getIso3Lang() + "]", "");
			}
		});

		ModelAndView mav = controller.changeTextSubmit(request);
		assertNotNull(mav);
		assertNotRedirect(mav);

		Errors errors = (Errors) mav.getModel().get("errors");

		// will be in several times, one per language
		assertHasError(errors, "error.objectText.missingInLanguage");
	}

	// ======================================================================

	@Test
	public void changeTextForm_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		request.setParameter("0", "thanksMessage");

		ModelAndView mav = controller.changeTextForm(request);
		assertNotNull(mav);
		assertHasViewNamePart(mav, "changeTextView");
		assertModelHasSurvey(mav, survey);
		assertModelHasAttribute(mav, "command");
	}
}

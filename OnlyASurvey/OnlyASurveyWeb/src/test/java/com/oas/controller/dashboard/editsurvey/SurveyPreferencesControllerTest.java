package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.service.SupportedLanguageService;

public class SurveyPreferencesControllerTest extends AbstractOASBaseTest {

	/** Not a valid SupportedLanguage ID. */
	private static final Long INVALID_LANGUAGE_ID = -2382032L;

	@Autowired
	private SurveyPreferencesController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void testFormView() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ModelAndView mav = controller.doForm(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));

		assertNotNull(mav);
		assertEquals("expected form view", "/dashboard/manage/surveyLanguagesForm", mav.getViewName());

		Map<String, Object> model = getModel(mav);
		assertNotNull("expected survey", model.get("survey"));
		assertNotNull("expected command", model.get("command"));
		assertNotNull("expected list of supported languages", model.get("supportedLanguages"));
	}

	@Test
	public void testSubmit_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getSurveyLanguages().clear();
		persist(survey);
		flushAndClear();

		int allLanguagesSize = -1;

		// enable all languages
		{
			assertEquals("should have zero languages", 0, survey.getSurveyLanguages().size());
			Long[] allLanguages = (Long[]) supportedLanguageService.getSupportedLanguageIds().toArray(new Long[0]);
			assertTrue("expected > 1 supported language in the system", allLanguages.length > 1);
			allLanguagesSize = allLanguages.length;

			IdListCommand command = new IdListCommand(allLanguages);
			ModelAndView mav = controller.doSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"),
					command);

			assertNotNull(mav);
			assertIsRedirect(mav);
		}

		//
		flushAndClear();

		// should now have all languages enabled
		{
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
			assertTrue("sanity check failed", allLanguagesSize > 1);
			assertEquals("should have all languages", allLanguagesSize, loaded.getSurveyLanguages().size());
		}

		//
		flushAndClear();

		// enable just English
		{
			IdListCommand command = new IdListCommand(new Long[] { supportedLanguageService.findByCode("eng").getId() });
			ModelAndView mav = controller.doSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"),
					command);
			assertNotNull(mav);
			assertIsRedirect(mav);
		}

		//
		flushAndClear();

		// should now just have 1 supported language
		{
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
			assertEquals("should have 1 language", 1, loaded.getSurveyLanguages().size());
		}
	}

	@Test
	public void testSubmit_Cancel() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		// set NO values
		IdListCommand command = new IdListCommand();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.setParameter("_cancel", "");
		ModelAndView mav = controller.doSubmit(request, command);

		assertIsRedirect("expected redirect", mav, "/html/db/mgt/" + survey.getId() + ".html");
	}

	@Test
	public void testSubmit_Fail_SelectAtLeastOne() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		// set NO values
		IdListCommand command = new IdListCommand();
		ModelAndView mav = controller.doSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"), command);

		assertNotNull(mav);
		assertNotRedirect(mav);
	}

	@Test
	public void testSubmit_Fail_InvalidArgument() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		// illegal argument
		IdListCommand command = new IdListCommand(new Long[] { supportedLanguageService.findByCode("eng").getId(),
				INVALID_LANGUAGE_ID });

		ModelAndView mav = controller.doSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"), command);

		assertNotNull(mav);
		assertNotRedirect(mav);
	}
}

package com.oas.controller.dashboard;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.NameObjectCommand;
import com.oas.model.BaseObject;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.service.SupportedLanguageService;

/**
 * Handles naming of surveys and survey-related data.
 * 
 * @author xhalliday
 * @since September 18, 2008
 */
public class NameObjectControllerTest extends AbstractOASBaseTest {

	@Autowired
	private NameObjectController controller;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void testReferenceData_DoMain_Success_HasNames() throws Exception {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		survey.addObjectName(supportedLanguageService.findByCode("fra"), NAME_FR);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/foo/" + survey.getId() + ".html");

		ModelAndView mav = controller.doMain(request);
		assertNotNull(mav);

		Collection<SupportedLanguage> languages = (Collection<SupportedLanguage>) mav.getModel().get("languageList");
		BaseObject baseObject = (BaseObject) mav.getModel().get("subject");

		assertNotNull("no languages data", languages);
		assertNotNull("no object data", baseObject);
		assertTrue("must have at least two languages", languages.size() >= 2);
		assertEquals("should have two names", 2, baseObject.getObjectNames().size());
	}

	@Test
	public void testGetTargetSurvey_Survey() {
		Survey survey = new Survey();
		assertEquals("should be same object (not equals, but ==)", survey, controller.getTargetSurvey(survey));
	}

	@Test
	public void testGetTargetSurvey_Question() {
		Survey survey = new Survey();
		Question question = new Question(survey);
		survey.getQuestions().add(question);

		assertEquals("should be same object (not equals, but ==)", survey, controller.getTargetSurvey(question));
	}

	@Test
	public void testGetTargetSurvey_Choice() {
		Survey survey = new Survey();
		ChoiceQuestion question = new ChoiceQuestion(survey);
		Choice choice = new Choice(question, 1L);
		question.getChoices().add(choice);
		survey.getQuestions().add(question);

		assertEquals("should be same object (not equals, but ==)", survey, controller.getTargetSurvey(choice));
	}

	@Test
	public void testGetTargetSurvey_Fail_UnknownType() {
		try {
			// no specific subclass, unsupported type is illegal
			controller.getTargetSurvey(new BaseObject());
			fail("expected throw");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testSubmitMethod_Success() throws Exception {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		getHibernateTemplate().persist(survey);

		NameObjectCommand command = new NameObjectCommand();
		// command.setNId(survey.getId());
		Map<String, String> map = new HashMap<String, String>();
		map.put("eng", NAME_EN);
		map.put("fra", NAME_FR);
		command.setM(map);

		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo/" + survey.getId() + ".html");
		ModelAndView mav = controller.doSubmit(request, command);
		assertNotNull(mav);
		assertTrue("should be a redirect", RedirectView.class.isAssignableFrom(mav.getView().getClass()));

		flushAndClear();

		// load from DB and validate
		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
		assertNotNull(loaded);
		assertEquals(survey.getId(), loaded.getId());

		setEnglish();
		assertEquals("incorrect English name", NAME_EN, loaded.getDisplayTitle());

		setFrench();
		assertEquals("incorrect French name", NAME_FR, loaded.getDisplayTitle());

	}

}

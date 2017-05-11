package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.springframework.security.AccessDeniedException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.IdListCommand;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.ScenarioDataUtil;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;
import com.oas.util.Constants;
import com.oas.util.QuestionTypeCode;

public class SurveyServiceTest extends AbstractOASBaseTest {

	@Test
	public void save() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		assertNotNull(survey);
		assertNotNull(survey.getId());
	}

	// ======================================================================

	@Test
	public void cloneQuestion_Fail_SurveyChangeDisallowed() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		BooleanQuestion question = new BooleanQuestion(survey);
		question.setAllowOtherText(false);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);
		survey.addQuestion(question);

		// published surveys can't be changed
		survey.setPublished(true);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		try {
			surveyService.cloneQuestion(question);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void addManyChoices_Success() {

		Survey survey = scenarioDataUtil.createMonthlyReportTestSurvey(createAndSetSecureUserWithRoleUser());
		ChoiceQuestion targetQuestion = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("scenario defect: no choice questions", targetQuestion);

		final int addCount = 8;
		int initialCount = targetQuestion.getChoices().size();

		// input to the service call
		List<ChoiceCommand> command = new ArrayList<ChoiceCommand>();
		for (int i = 0; i < addCount; i++) {
			command.add(new ChoiceCommand(createNameMap()));
		}

		// persist
		surveyService.addManyChoices(targetQuestion, command);
		flushAndClear();

		ChoiceQuestion loaded = get(ChoiceQuestion.class, targetQuestion.getId());
		assertNotNull("unable to reload question", loaded);
		assertNotNull("no choices!", loaded.getChoices());

		int newCount = loaded.getChoices().size();

		assertEquals("unexpected new count", initialCount + addCount, newCount);
	}

	// ======================================================================

	@Test
	public void deleteQuestion_Fail_ChangeDisallowed() {

		createAndSetSecureUserWithRoleUser();

		Survey survey = new Survey();
		survey.setPublished(true);
		Question question = new TextQuestion(survey);

		try {
			surveyService.deleteQuestion(question);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void deleteChoice_Success_WithResponseData() {

		int RESP_COUNT = 10;

		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		// if test gets slow, it's because of this number: speed up code, don't
		// remove the count
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		scenarioDataUtil.createResponseData(survey, RESP_COUNT);

		flushAndClear();

		long originalCount = countBaseObjects();

		// Question question = getFirstQuestionOfType(load(Survey.class,
		// survey.getId()), TextQuestion.class);
		Survey loaded = (Survey) unique(find("from Survey where id=?", survey.getId()));
		ChoiceQuestion question = getFirstQuestionOfType(loaded, ChoiceQuestion.class);
		assertNotNull("scenario data missing a ChoiceQuestion", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("scenario data missing a Choice", choice);

		surveyService.deleteChoice(choice);
		flushAndClear();

		assertNull("should not exist", unique(find("from Choice where id=?", choice.getId())));

		long newCount = countBaseObjects();

		// the original count, minus the number of responses generated (one
		// ChoiceAnswer per), minus 1 for the choice itself
		assertEquals("expected BaseObject count to drop", originalCount - RESP_COUNT - 1, newCount);
	}

	@Test
	public void deleteQuestion_Success_WithResponseData() {

		int RESP_COUNT = 10;

		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		// if test gets slow, it's because of this number: speed up code, don't
		// remove the count
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		scenarioDataUtil.createResponseData(survey, RESP_COUNT);

		flushAndClear();

		long originalCount = countBaseObjects();

		// Question question = getFirstQuestionOfType(load(Survey.class,
		// survey.getId()), TextQuestion.class);
		Survey loaded = (Survey) unique(find("from Survey where id=?", survey.getId()));
		Question question = getFirstQuestionOfType(loaded, TextQuestion.class);
		assertNotNull("scenario data missing a TextQuestion", question);

		surveyService.deleteQuestion(question);
		flushAndClear();

		assertNull("should not exist", unique(find("from Question where id=?", question.getId())));

		long newCount = countBaseObjects();

		// the original count, minus the number of responses generated (one
		// TextAnswer per), minus 1 for the question itself
		assertEquals("expected BaseObject count to drop", originalCount - RESP_COUNT - 1, newCount);
	}

	@Test
	public void cloneQuestion_Success_Boolean() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		BooleanQuestion question = new BooleanQuestion(survey);
		question.setAllowOtherText(false);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);
		survey.addQuestion(question);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		surveyService.cloneQuestion(question);
		flushAndClear();

		// load the new question, which is the last question
		BooleanQuestion clone = (BooleanQuestion) surveyService.findLastQuestion(survey);
		basicCloneAssertions(question, clone);
	}

	@Test
	public void cloneQuestion_Success_Text() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		TextQuestion question = new TextQuestion(survey);
		question.setAllowOtherText(false);
		question.setFieldDisplayLength(12);
		question.setNumRows(13);
		question.setMaximumLength(14);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);
		survey.addQuestion(question);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		surveyService.cloneQuestion(question);
		flushAndClear();

		// load the new question, which is the last question
		TextQuestion clone = (TextQuestion) surveyService.findLastQuestion(survey);
		basicCloneAssertions(question, clone);
		assertEquals("expected same numRows", question.getNumRows(), clone.getNumRows());
		assertEquals("expected same maximumLength", question.getMaximumLength(), clone.getMaximumLength());
		assertEquals("expected same fieldDisplayLength", question.getFieldDisplayLength(), clone.getFieldDisplayLength());
	}

	@Test
	public void cloneQuestion_Success_Choice() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		ChoiceQuestion question = new ChoiceQuestion(survey);
		question.setAllowOtherText(false);
		question.setUnlimited(true);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);

		question.addChoice(new Choice(question, 1L));
		question.addChoice(new Choice(question, 2L));
		Choice choice3 = new Choice(question, 3L);
		choice3.addObjectName(supportedLanguageService.findByCode("eng"), "some choice");
		question.addChoice(choice3);

		survey.addQuestion(question);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		surveyService.cloneQuestion(question);
		flushAndClear();

		// load the new question, which is the last question
		ChoiceQuestion clone = (ChoiceQuestion) surveyService.findLastQuestion(survey);
		basicCloneAssertions(question, clone);
		assertEquals("expected same unlimited value", question.isUnlimited(), clone.isUnlimited());

		Collection<Choice> choiceList = clone.getChoices();
		assertNotNull("expected choices", choiceList);
		assertEquals("expected 3 choices", 3, choiceList.size());
	}

	@Test
	public void cloneQuestion_Success_Choice_ConstantSum() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		ChoiceQuestion question = new ChoiceQuestion(survey);
		question.setAllowOtherText(false);
		question.setUnlimited(true);
		question.setMaximumSum(100);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);

		question.addChoice(new Choice(question, 1L));
		question.addChoice(new Choice(question, 2L));
		Choice choice3 = new Choice(question, 3L);
		choice3.addObjectName(supportedLanguageService.findByCode("eng"), "some choice");
		question.addChoice(choice3);

		survey.addQuestion(question);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		surveyService.cloneQuestion(question);
		flushAndClear();

		// load the new question, which is the last question
		ChoiceQuestion clone = (ChoiceQuestion) surveyService.findLastQuestion(survey);
		basicCloneAssertions(question, clone);
		assertEquals("expected same unlimited value", question.isUnlimited(), clone.isUnlimited());

		Collection<Choice> choiceList = clone.getChoices();
		assertNotNull("expected choices", choiceList);
		assertEquals("expected 3 choices", 3, choiceList.size());

		// summing nature
		assertTrue("expected question to be isSummingQuestion()", question.isSummingQuestion());
		assertEquals("expected summing question type code", QuestionTypeCode.CONSTANT_SUM, question.getQuestionTypeCode());
	}

	@Test
	public void cloneQuestion_Success_Choice_SelectList() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		ChoiceQuestion question = new ChoiceQuestion(survey);
		question.setAllowOtherText(false);
		question.setUnlimited(false);
		question.setStyle(Constants.STYLE_SELECT);

		question.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN);
		question.setDisplayOrder(1000L);

		question.addChoice(new Choice(question, 1L));
		question.addChoice(new Choice(question, 2L));
		Choice choice3 = new Choice(question, 3L);
		choice3.addObjectName(supportedLanguageService.findByCode("eng"), "some choice");
		question.addChoice(choice3);

		survey.addQuestion(question);
		persist(survey);

		// can't clear here to avoid detached entities
		getHibernateTemplate().flush();

		surveyService.cloneQuestion(question);
		flushAndClear();

		// load the new question, which is the last question
		ChoiceQuestion clone = (ChoiceQuestion) surveyService.findLastQuestion(survey);
		basicCloneAssertions(question, clone);
		assertEquals("expected same unlimited value", question.isUnlimited(), clone.isUnlimited());

		Collection<Choice> choiceList = clone.getChoices();
		assertNotNull("expected choices", choiceList);
		assertEquals("expected 3 choices", 3, choiceList.size());

		// select list nature
		assertEquals("expected summing question type code", QuestionTypeCode.SELECT, question.getQuestionTypeCode());
	}

	@Test
	public void cloneQuestion_Fail_UnknownType() {
		try {
			createAndSetSecureUserWithRoleUser();
			// new Question() is not valid here: can only clone a concrete type
			surveyService.cloneQuestion(new Question());
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void cloneQuestion_Security_Fail_NoUser() {
		try {
			// no security context
			surveyService.cloneQuestion(new Question());
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void cloneQuestion_Security_Fail_WrongUser() {
		try {
			// some owner
			Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
			// some other user invoking the service
			surveyService.cloneQuestion(new Question(survey));
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	private void basicCloneAssertions(Question source, Question target) {
		assertNotNull("invalid source question", source);
		assertNotNull("null clone", target);
		assertEquals("expected clone to have same allowOtherText", source.isAllowOtherText(), target.isAllowOtherText());
		assertEquals("expected clone to have same required", source.isRequired(), target.isRequired());
		assertEquals("expected clone to have same numRows", source.getDisplayTitle(), target.getDisplayTitle());
		assertEquals("expected clone to have same typeCode", source.getQuestionTypeCode(), target.getQuestionTypeCode());
	}

	// ======================================================================

	@Test
	public void setLanguages_Fail_ChangeDisallowed() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getSurveyLanguages().clear();

		// can't change published
		survey.setPublished(true);

		persist(survey);

		// a clear here will make Survey detached
		getHibernateTemplate().flush();

		// enable all languages

		assertEquals("should have zero languages", 0, survey.getSurveyLanguages().size());
		@SuppressWarnings("cast")
		Long[] allLanguages = (Long[]) supportedLanguageService.getSupportedLanguageIds().toArray(new Long[0]);
		assertTrue("expected > 1 supported language in the system", allLanguages.length > 1);

		IdListCommand command = new IdListCommand(allLanguages);
		try {
			surveyService.setSurveyLanguages(survey, command);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void setLanguages() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getSurveyLanguages().clear();
		persist(survey);

		// a clear here will make Survey detached
		getHibernateTemplate().flush();

		int allLanguagesSize = -1;

		// enable all languages
		{
			assertEquals("should have zero languages", 0, survey.getSurveyLanguages().size());
			@SuppressWarnings("cast")
			Long[] allLanguages = (Long[]) supportedLanguageService.getSupportedLanguageIds().toArray(new Long[0]);
			assertTrue("expected > 1 supported language in the system", allLanguages.length > 1);
			allLanguagesSize = allLanguages.length;

			IdListCommand command = new IdListCommand(allLanguages);
			surveyService.setSurveyLanguages(survey, command);
		}

		// a clear here will make Survey detached
		getHibernateTemplate().flush();

		// should now have all languages enabled
		{
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
			assertTrue("sanity check failed", allLanguagesSize > 1);
			assertEquals("should have all languages", allLanguagesSize, loaded.getSurveyLanguages().size());
		}

		// a clear here will make Survey detached
		getHibernateTemplate().flush();

		// enable just English
		{
			IdListCommand command = new IdListCommand(new Long[] { supportedLanguageService.findByCode("eng").getId() });
			surveyService.setSurveyLanguages(survey, command);
		}

		// a clear here will make Survey detached
		getHibernateTemplate().flush();

		// should now just have 1 supported language
		{
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
			assertEquals("should have 1 language", 1, loaded.getSurveyLanguages().size());
		}
	}

	// ======================================================================

	// BUSINESS RULE CHANGE: Published Surveys can be changed
	// @Test
	// public void addQuestion_Fail_ChangeDisallowed() {
	// createAndSetSecureUserWithRoleUser();
	//
	// Survey survey =
	// scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
	// survey.setPublished(true);
	// surveyService.save(survey);
	//
	// CreateQuestionCommand command = new CreateQuestionCommand();
	// // text question
	// command.setTypeCode(QuestionTypeCode.BOOLEAN);
	// command.getMap().put("eng", NAME_EN);
	// command.getMap().put("fra", NAME_FR);
	// command.setAllowOtherText(false);
	//
	// try {
	// surveyService.addQuestion(survey, command);
	// fail("expected IllegalArgumentException");
	// } catch (IllegalArgumentException e) {
	// // expected
	// }
	// }

	@Test
	public void addQuestion_Boolean() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// text question
		command.setTypeCode(QuestionTypeCode.BOOLEAN);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setAllowOtherText(false);

		Question question = surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertTrue("incorrect type", BooleanQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(ChoiceQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(TextQuestion.class));

		assertFalse("allowOtherText incorrect", question.isAllowOtherText());
	}

	@Test
	public void addQuestion_Text() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// boolean question
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setNumRows(1);

		Question question = surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.TEXT, question.getQuestionTypeCode());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertTrue("incorrect type", TextQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(ChoiceQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
	}

	@Test
	public void addQuestion_Essay() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// boolean question
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setNumRows(4);

		Question question = surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.ESSAY, question.getQuestionTypeCode());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertTrue("incorrect type", TextQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(ChoiceQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
	}

	@Test
	public void addQuestion_Radio() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// choice question
		command.setTypeCode(QuestionTypeCode.RADIO);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setAllowOtherText(true);

		ChoiceQuestion question = (ChoiceQuestion) surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.RADIO, question.getQuestionTypeCode());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertFalse("should NOT be unlimited", question.isUnlimited());

		assertTrue("incorrect type", ChoiceQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(TextQuestion.class));

		assertTrue("allowOtherText incorrect", question.isAllowOtherText());

	}

	@Test
	public void addQuestion_Checkbox() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// choice question
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);

		ChoiceQuestion question = (ChoiceQuestion) surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.CHECKBOX, question.getQuestionTypeCode());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertTrue("should be unlimited", question.isUnlimited());

		assertTrue("incorrect type", ChoiceQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(TextQuestion.class));
	}

	@Test
	public void addQuestion_Select() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// choice question
		command.setTypeCode(QuestionTypeCode.SELECT);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setAllowOtherText(true);

		ChoiceQuestion question = (ChoiceQuestion) surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.SELECT, question.getQuestionTypeCode());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertFalse("should NOT be unlimited", question.isUnlimited());

		assertTrue("incorrect type", ChoiceQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(TextQuestion.class));

		assertTrue("allowOtherText incorrect", question.isAllowOtherText());

	}

	@Test
	public void addQuestion_Scale() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// boolean question
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);
		command.setMaximum(7L);

		Question question = surveyService.addQuestion(survey, command);
		assertNotNull(question);
		assertNotNull(question.getId());

		assertEquals("incorrect type", QuestionTypeCode.SCALE, question.getQuestionTypeCode());

		assertEquals("unexpected maximum value", Long.valueOf(7L), ((ScaleQuestion) question).getMaximum());

		setEnglish();
		assertEquals("incorrect name", NAME_EN, question.getDisplayTitle());

		setFrench();
		assertEquals("incorrect name", NAME_FR, question.getDisplayTitle());

		assertTrue("incorrect type", ScaleQuestion.class.isAssignableFrom(question.getClass()));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(TextQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(ChoiceQuestion.class));
		assertFalse("incorrect type", question.getClass().isAssignableFrom(BooleanQuestion.class));
	}

	@Test
	public void addQuestion_Fail_InvalidType() {
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		surveyService.save(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();

		// don't set question type
		command.getMap().put("eng", NAME_EN);
		command.getMap().put("fra", NAME_FR);

		try {
			surveyService.addQuestion(survey, command);
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void updateQuestion_Fail_ChangeDisallowed() {
		createAndSetSecureUserWithRoleUser();
		Survey survey = new Survey();
		survey.setPublished(false);
		Question question = new TextQuestion(survey);
		CreateQuestionCommand command = new CreateQuestionCommand();

		try {
			surveyService.updateQuestion(survey, command, question);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void save_FailsIfNotOwner() {
		UserAccount otherUser = createTestUser();
		createAndSetSecureUserWithRoleUser();

		Survey survey = scenarioDataUtil.createTypicalScenario1(otherUser);

		try {
			surveyService.save(survey);
			fail("should have thrown an AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void findBy_Id() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());
		getHibernateTemplate().persist(survey);
		flushAndClear();

		Survey found = surveyService.findNonDeletedSurvey(survey.getId());
		assertNotNull(found);
		assertNotNull(found.getId());
		assertEquals(survey.getId(), found.getId());
	}

	@Test
	public void getFirstQuestion() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());
		getHibernateTemplate().persist(survey);
		flushAndClear();

		Question question = surveyService.findFirstQuestion(survey);

		assertNotNull(question);
		assertEquals("should be same name", ScenarioDataUtil.QUESTION_1_NAME, question.getDisplayTitle());
	}

	/**
	 * assumes getFirstQuestion passes
	 */
	@Test
	public void getNextQuestion() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());
		getHibernateTemplate().persist(survey);
		flushAndClear();

		Question firstQuestion = surveyService.findFirstQuestion(survey);
		assertNotNull(firstQuestion);

		Question nextQuestion = surveyService.findQuestionAfter(firstQuestion);
		assertNotNull(nextQuestion);
		assertEquals("should be same name", ScenarioDataUtil.QUESTION_2_NAME, nextQuestion.getDisplayTitle());
	}

	// ======================================================================

	@Test
	public void publish_Success() {
		// valid test data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		// payment required and done
		survey.setPaidFor(true);

		Errors errors = surveyService.publishSurvey(survey);
		assertNotNull(errors);
		assertEquals("should have no errors", 0, errors.getErrorCount());
	}

	@Test
	public void publish_FailOnPaymentRequired() {
		// valid test data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		// payment required but not present
		survey.setPaidFor(false);

		Errors errors = surveyService.publishSurvey(survey);
		assertNotNull(errors);
		assertEquals("should have errors", 1, errors.getErrorCount());
	}

	/**
	 * Test that the ready-for-publishing validator is invoked to prevent
	 * invalid data from being published.
	 */
	// @Test
	// public void publish_FailOnNotValidForPublishing() {
	// // TODO correct when validator is re-enabled on service
	// }
	// ======================================================================
	@Test
	public void confirmPayment_Success() {
		// valid test data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		// payment required and NOT done
		survey.setPaidFor(false);

		// no exceptions
		surveyService.confirmPayment(survey);

		// reload fresh
		flushAndClear();
		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());

		assertTrue("expected paidFor to be true", loaded.isPaidFor());
	}

	@Test
	public void confirmPayment_Fail_AlreadyPaidFor() {
		// valid test data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		survey.setPaidFor(true);

		try {
			surveyService.confirmPayment(survey);
			fail("expected illegal argument exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void confirmPayment_SecurityFail_NoUser() {
		// valid test data with OTHER user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());

		// no user is set

		try {
			surveyService.confirmPayment(survey);
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void confirmPayment_SecurityFail_WrongUser() {
		// valid test data with OTHER user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());

		// new user (not owner above) makes change
		createAndSetSecureUserWithRoleUser();

		try {
			surveyService.confirmPayment(survey);
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void adjustQuestionOrder_Fail_ChangeDisallowed() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.setPublished(true);
		persist(survey);

		Question sibling = survey.getQuestions().get(2); // I'm going DOWN
		Question subject = survey.getQuestions().get(3); // I'm going UP

		assertNotNull("unable to get a subject question to base test on", subject);
		assertNotNull("unable to get a subject question to base test on", sibling);

		long originalDisplayOrder = subject.getDisplayOrder();
		long targetDisplayOrder = sibling.getDisplayOrder();

		// sanity
		assertEquals("insane display order.", targetDisplayOrder, originalDisplayOrder - 1L);

		//
		try {
			surveyService.moveQuestionUp(survey, subject);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

		//
		try {
			surveyService.moveQuestionDown(survey, subject);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void adjustQuestionOrder_Up_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		Question sibling = survey.getQuestions().get(2); // I'm going DOWN
		Question subject = survey.getQuestions().get(3); // I'm going UP

		assertNotNull("unable to get a subject question to base test on", subject);
		assertNotNull("unable to get a subject question to base test on", sibling);

		long originalDisplayOrder = subject.getDisplayOrder();
		long targetDisplayOrder = sibling.getDisplayOrder();

		// sanity
		assertEquals("insane display order.", targetDisplayOrder, originalDisplayOrder - 1L);

		//
		surveyService.moveQuestionUp(survey, subject);

		// fresh new day
		flushAndClear();

		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
		assertTrue("subject question should now be ordered as requested", loaded.getQuestions().get(2).getId().equals(
				subject.getId()));
		assertTrue("sibling question should now be ordered as subject originally was", loaded.getQuestions().get(3).getId()
				.equals(sibling.getId()));
	}

	@Test
	public void adjustQuestionOrder_Down_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		Question subject = survey.getQuestions().get(2); // I'm going DOWN
		Question sibling = survey.getQuestions().get(3); // I'm going UP

		assertNotNull("unable to get a subject question to base test on", subject);
		assertNotNull("unable to get a subject question to base test on", sibling);

		long originalDisplayOrder = subject.getDisplayOrder();
		long targetDisplayOrder = sibling.getDisplayOrder();

		// sanity
		assertEquals("insane display order.", targetDisplayOrder, originalDisplayOrder + 1L);

		//
		surveyService.moveQuestionDown(survey, subject);

		// fresh new day
		flushAndClear();

		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
		assertTrue("subject question should now be ordered as requested", loaded.getQuestions().get(2).getId().equals(
				sibling.getId()));
		assertTrue("sibling question should now be ordered as subject originally was", loaded.getQuestions().get(3).getId()
				.equals(subject.getId()));
	}

	// ======================================================================

	@Test
	public void hasResponses_IsTrue() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		assertTrue("expected true", surveyService.hasNonDeletedResponses(survey));
	}

	@Test
	public void hasResponses_IsFalse() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		getHibernateTemplate().persist(survey);

		assertFalse("expected false", surveyService.hasNonDeletedResponses(survey));
	}

	// ======================================================================

	@Test
	public void deleteSurvey_NoResponses_Success() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		getHibernateTemplate().persist(survey);
		Long surveyId = survey.getId();

		// flush all changes
		flushAndClear();

		// delete and expect no errors
		surveyService.deleteSurvey(surveyService.findNonDeletedSurvey(surveyId));
		flushAndClear();

		Survey loaded = surveyService.findNonDeletedSurvey(surveyId);
		assertNull("should have been deleted", loaded);
	}

	// ======================================================================

	@Test
	public void deleteResponseData_NoResponses_Success() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		getHibernateTemplate().persist(survey);
		Long surveyId = survey.getId();

		// flush all changes
		flushAndClear();

		// delete and expect no errors

		surveyService.deleteResponseData(get(Survey.class, surveyId));
		flushAndClear();

		assertEquals("should have no responses", Integer.valueOf(0), surveyService.countResponses(survey));
	}

	/**
	 * Add a large number of responses and then call deleteResponseData(Survey),
	 * to ensure that cascades (slash whatever impl in use at the time) work and
	 * that performance is acceptable at Survey scope.
	 */
	@Test
	public void deleteResponseData_ForSurvey_HasResponses_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		// flush all changes
		flushAndClear();
		int originalCount = surveyService.countResponses(survey);
		assertTrue("test/scenario defect: must have response data to start", originalCount > 0);

		{
			// delete and expect no errors
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
			surveyService.deleteResponseData(loaded);
			flushAndClear();

			assertEquals("should have no responses", Integer.valueOf(0), surveyService.countResponses(loaded));
		}

	}

	/**
	 * Add a large number of responses and then call
	 * deleteResponseData(Question), to ensure that cascades (slash whatever
	 * impl in use at the time) work and that performance is acceptable at
	 * Question scope.
	 */
	@Test
	public void deleteResponseData_ForQuestion_HasResponses_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.createResponseData(survey, 100);

		// flush all changes
		flushAndClear();
		int originalCount = surveyService.countResponses(survey);
		assertTrue("test/scenario defect: must have response data to start", originalCount > 0);

		{
			// reload, delete and expect no errors
			Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());

			Question question = getFirstQuestionOfType(loaded, ChoiceQuestion.class);

			long count = (Long) unique(find("select count(a) from ChoiceAnswer a where question=?", question));
			assertTrue("test data's first question has zero answers: cannot continue", count > 0);

			surveyService.purgeResponseData(question);
			flushAndClear();

			long newCount = (Long) unique(find("select count(a) from ChoiceAnswer a where question=?", question));
			assertEquals("should have no responses", 0, newCount);
		}

	}

	// ======================================================================

	protected Template getValidTemplate(Survey survey, SupportedLanguage supportedLanguage) {
		Template template = new Template(survey, supportedLanguage);
		template.setBeforeContent("<html>");
		template.setAfterContent("</html>");
		template.setBaseUrl("http://www.localhost.com");
		template.setImportedFromUrl("http://www.localhost.com/eng/welcome.html");
		template.setTemplateType(TemplateType.CLF2_COMMENTS);

		return template;
	}

	protected void assertApplyTemplateSuccess(Template sourceTemplate, Template destinationTemplate) {

		assertNotNull("no source template", sourceTemplate);
		assertNotNull("no destination template", destinationTemplate);

		//
		assertEquals(sourceTemplate.getBeforeContent(), destinationTemplate.getBeforeContent());
		assertEquals(sourceTemplate.getAfterContent(), destinationTemplate.getAfterContent());
		assertEquals(sourceTemplate.getBaseUrl(), destinationTemplate.getBaseUrl());
		assertEquals(sourceTemplate.getImportedFromUrl(), destinationTemplate.getImportedFromUrl());
		assertEquals(sourceTemplate.getSupportedLanguage().getId(), destinationTemplate.getSupportedLanguage().getId());
		assertEquals(sourceTemplate.getTemplateType(), destinationTemplate.getTemplateType());
	}

	@Test
	public void applyTemplate_Success() {

		// target
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getTemplates().clear();
		persist(survey);

		assertTrue("expected no template in scenario data", survey.getTemplates().isEmpty());

		SupportedLanguage language = supportedLanguageService.findByCode("eng");

		// some user input
		Template template = getValidTemplate(survey, language);

		// under test
		surveyService.applyTemplate(survey, template);
		flushAndClear();

		{
			// reload fresh and verify
			Survey loadedSurvey = load(Survey.class, survey.getId());
			Template loaded = loadedSurvey.getTemplates().get(language);

			assertApplyTemplateSuccess(template, loaded);
		}

		// apply changes
		template.setBeforeContent("before");
		template.setAfterContent("after");
		surveyService.applyTemplate(load(Survey.class, survey.getId()), template);
		flushAndClear();

		{
			// reload fresh and verify
			Survey loadedSurvey = load(Survey.class, survey.getId());
			Template loaded = loadedSurvey.getTemplates().get(language);

			assertApplyTemplateSuccess(template, loaded);
		}
	}

	// ======================================================================

	@Test
	public void attachLogo_And_getLogoData_Success() throws IOException {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getLogos().clear();
		persist(survey);

		final String altText = "My Company Logo";
		final SupportedLanguage language = supportedLanguageService.findByCode("eng");

		// 
		int payloadLength = -1;
		Long logoId = null;
		String payloadAsString = null;

		{
			// load sample image data
			byte[] payload = loadPayloadFromTestFile();
			assertNotNull(payload);
			assertTrue(payload.length > 0);

			// set for later
			payloadLength = payload.length;
			payloadAsString = new String(payload);

			//
			Errors errors = new BindException(survey, "survey");

			// method under test
			SurveyLogo logo = surveyService.attachLogo(errors, survey, language, SurveyLogo.PositionType.LEFT, "image/png",
					altText, payload);

			assertNotNull("no logo stored", logo);

			logoId = logo.getId();

			assertNotNull("no logo ID", logoId);
			assertHasText("no payload as string", payloadAsString);

			assertTrue(logo.getWidth() > 0);
			assertTrue(logo.getHeight() > 0);
			assertTrue(logo.getSize() > 0);
			assertEquals("unexpected content type", "image/png", logo.getContentType());
			assertEquals("unexpected alt text", altText, logo.getAltText());

			assertTrue("expected zero errors", errors.hasErrors() == false);
		}

		assertTrue("test defect: payloadLength was never set", payloadLength != -1);
		assertNotNull("test defect: logoId was never set", logoId);
		assertNotNull("test defect: payloadAsString was never set", payloadAsString);

		flushAndClear();

		//
		// reload and validate via get()
		{
			SurveyLogo loaded = get(SurveyLogo.class, logoId);
			assertNotNull(loaded);
			assertEquals(altText, loaded.getAltText());

			// payload is encoded
			assertHasText("no payload", loaded.getPayload());
			assertTrue(loaded.getPayload().length() > payloadLength);

			// test vs. loaded object
			assertEquals("expected same payload data", payloadAsString.substring(0, 50), new String(Base64.decodeBase64(loaded
					.getPayload().getBytes())).substring(0, 50));
		}

		//
		// reload and validate via getLogoData()
		{
			byte[] loadedPayload = surveyService.getLogoData(survey, language, SurveyLogo.PositionType.LEFT);

			// test vs getLogoData
			assertEquals("expected same payload data", payloadAsString, new String(loadedPayload));
		}
	}

	@Test
	public void getLogosForLanguage_Success() {

		final SupportedLanguage language = supportedLanguageService.findByCode("eng");
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.getLogos().clear();
		persist(survey);

		{
			//
			SurveyLogo leftLogo = new SurveyLogo(survey, language);
			leftLogo.setAltText("alt");
			leftLogo.setWidth(300);
			leftLogo.setHeight(200);
			leftLogo.setSize(10240);
			leftLogo.setContentType("image/png");
			leftLogo.setPayload("bogus payload");
			leftLogo.setPosition(SurveyLogo.PositionType.LEFT);
			leftLogo.setUploadTime(new Date());

			//
			SurveyLogo rightLogo = new SurveyLogo(survey, language);
			rightLogo.setAltText("alt");
			rightLogo.setWidth(300);
			rightLogo.setHeight(200);
			rightLogo.setSize(10240);
			rightLogo.setContentType("image/png");
			rightLogo.setPayload("bogus payload");
			rightLogo.setPosition(SurveyLogo.PositionType.RIGHT);
			rightLogo.setUploadTime(new Date());

			//
			survey.getLogos().add(leftLogo);
			survey.getLogos().add(rightLogo);
			persist(survey);
		}

		flushAndClear();

		Map<SurveyLogo.PositionType, SurveyLogo> map = surveyService.getLogosForLanguage(survey, language);

		assertNotNull(map);
		{
			SurveyLogo leftLogo = map.get(SurveyLogo.PositionType.LEFT);
			SurveyLogo rightLogo = map.get(SurveyLogo.PositionType.RIGHT);

			assertNotNull(leftLogo);
			assertNotNull(rightLogo);

			assertEquals(SurveyLogo.PositionType.LEFT, leftLogo.getPosition());
			assertEquals(SurveyLogo.PositionType.RIGHT, rightLogo.getPosition());
		}
	}

	private byte[] loadPayloadFromTestFile() throws IOException {
		final String filename = "src/test/resources/logos/logo1.png";

		File file = new File(filename);
		assertTrue("cannot find test PNG: " + filename, file.exists());

		// buffer
		byte[] retval = new byte[(int) file.length()];

		FileInputStream fis = new FileInputStream(file);
		int nr = fis.read(retval);
		assertEquals("unable to read entire file", file.length(), nr);

		return retval;
	}

	@Test
	public void attachLogo_Fail_BogusImageData() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		final String altText = "My Company Logo";
		final SupportedLanguage language = supportedLanguageService.findByCode("eng");
		Errors errors = new BindException(survey, "survey");

		SurveyLogo logo = surveyService.attachLogo(errors, survey, language, SurveyLogo.PositionType.LEFT, "image/png", altText,
				"bogus data here".getBytes());
		assertIsNull("expected attachment to fail", logo);
		assertTrue("expected errors to exist", errors.hasErrors());
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void attachLogo_Security_Fail_NoUser() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		surveyService.attachLogo(new BindException(survey, "survey"), survey, supportedLanguageService.findByCode("eng"),
				SurveyLogo.PositionType.LEFT, "image/jpeg", "woot", new byte[] { 1, 2, 3 });

	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void attachLogo_Security_Fail_WrongUser() {
		// current user
		createAndSetSecureUserWithRoleUser();

		// owned by some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		//
		surveyService.attachLogo(new BindException(survey, "survey"), survey, supportedLanguageService.findByCode("eng"),
				SurveyLogo.PositionType.LEFT, "image/jpeg", "woot", new byte[] { 1, 2, 3 });
	}

	// ======================================================================

	@Test
	public void isChangeAllowed() {
		Survey survey = new Survey();

		//
		survey.setPublished(true);
		assertFalse("published surveys can't be changed", surveyService.isChangeAllowed(survey));

		//
		survey.setPublished(false);
		assertTrue("unpublished surveys can't be changed", surveyService.isChangeAllowed(survey));
	}
}

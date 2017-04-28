package com.oas.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.answer.BooleanAnswer;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.keys.ResponseChoiceOrderKey;
import com.oas.model.keys.ResponseQuestionOrderKey;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;
import com.oas.model.report.ResponsesPerDay;
import com.oas.model.report.SurveySummary;
import com.oas.model.report.calendar.breakdown.ScaleDailyBreakdown;
import com.oas.model.report.calendar.breakdown.ScaleMonthlyBreakdown;
import com.oas.model.report.calendar.value.TextCalendarValue;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;
import com.oas.model.util.QuestionTypeCode;
import com.oas.model.util.QuestionTypeConstants;
import com.oas.service.ReportingService;
import com.oas.service.SupportedLanguageService;

public class DomainModelExercisesTest extends AbstractOASBaseTest {

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private ReportingService reportingService;

	@Test
	public void typicalScenario1_GeneralTest() {

		// create test data
		Long testSurveyId = null;
		{
			// do not yet persist
			Survey testSurvey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);

			// rename object
			testSurvey.addObjectName(supportedLanguageService.findByCode("eng"), NAME_EN + getMBUN());

			// store
			persist(testSurvey);

			// add question rules since createTypicalScenario1 won't do it when
			// persist=false
			scenarioDataUtil.addDefaultScenario1Rules(testSurvey);

			// clear out session so find/load/etc are always fresh
			flushAndClear();

			//
			testSurveyId = testSurvey.getId();
		}

		// verify various parts of the model work

		Survey survey = get(Survey.class, testSurveyId);
		Response response = null;

		assertNotNull(survey);
		assertEquals("unexpected number of questions", 5, survey.getQuestions().size());

		// flags
		{
			assertFalse("unexpected flag value", survey.isPublished());
			assertFalse("unexpected flag value", survey.isDeleted());
			assertFalse("unexpected flag value", survey.isPaidFor());
			assertTrue("unexpected flag value", survey.isChangeAllowed());
		}

		// templating
		{
			for (SupportedLanguage language : survey.getSupportedLanguages()) {
				Template template = survey.getTemplates().get(language);

				assertEquals("expected matching object", survey, template.getSurvey());
				assertEquals("expected matching type", TemplateType.OAS_COMMENTS, template.getTemplateType());
				assertFalse("unexpected value", template.isClf2Template());
				assertNotNull("expecting text", template.getBeforeContent());
				assertNotNull("expecting text", template.getAfterContent());
				assertNotNull("expecting text", template.getHashAtImport());
				assertNotNull("expecting url", template.getBaseUrl());
				assertNotNull("expecting url", template.getImportedFromUrl());
			}
		}

		{
			List<SurveyLogo> list = find("from SurveyLogo where survey=?", survey);
			assertNotNull("failed to load logos - null", list);
			assertTrue("no logo data", list.size() > 0);

			// pull one out and validate what we can
			SurveyLogo logo = list.get(0);
			assertEquals(survey, logo.getSurvey());
			assertTrue(logo.getWidth() > 0);
			assertTrue(logo.getHeight() > 0);
			assertTrue(logo.getSize() > 0);
			assertNotNull(logo.getUploadTime());
			assertNotNull(logo.getPayload());
			assertNotNull(logo.getAltText());
			assertNotNull(logo.getPosition());
			assertNotNull(logo.getContentType());
			assertTrue(survey.getSupportedLanguages().contains(logo.getSupportedLanguage()));
		}

		// survey language validation
		{
			@SuppressWarnings("unchecked")
			Collection<SurveyLanguage> list = getHibernateTemplate().find("from SurveyLanguage r where r.survey = ?", survey);
			assertNotNull(list);
			assertTrue("should have some languages", list.size() >= 2);
		}

		//
		// CREATE DEFAULT RESPONSE
		//
		// note this could be refactored out; it is here because the
		// functionality was refactored out: Response is no longer referenced by
		// Survey. BugzID: 6
		//
		scenarioDataUtil.addDefaultScenario1Response(survey);

		// response validation
		{
			@SuppressWarnings("unchecked")
			Collection<Response> list = getHibernateTemplate()
					.find("from Response r where r.survey = ? order by id desc", survey);
			assertNotNull(list);
			assertEquals("should have one response for survey #" + survey.getId(), 1, list.size());

			// get first response from the list, since it's ordered id desc
			response = list.iterator().next();

			// this test expects the response to be closed because it depends on
			// some of the reporting views
			response.closeResponse();
			persist(response);
		}

		// question history
		{
			Question lastShown = responseService.getLastShownQuestion(response);
			assertEquals("unexpected Last Shown Question", survey.getQuestions().get(4), lastShown);

			ChoiceQuestion question = (ChoiceQuestion) survey.getQuestions().get(2);
			assertTrue(question.isChoiceQuestion());

			List<Choice> expected = question.getChoices();
			List<Choice> found = new ArrayList<Choice>();

			// be lazy: get all
			List<ResponseChoiceHistory> list = responseService.getQuestionChoiceHistory(response, question);

			for (ResponseChoiceHistory history : list) {
				if (history.getId().getChoice().getQuestion().getId().equals(question.getId())) {
					found.add(history.getId().getChoice());
				}
			}

			assertTrue("expected all choices to have been shown", found.containsAll(expected));
		}

		// answer validation
		{
			// Collection<Answer> list = response.getAnswers();
			Collection<Answer> list = find("from Answer where response = ?", response);

			assertEquals(4, list.size());

			boolean hasText = false;
			boolean hasChoice = false;
			boolean hasBoolean = false;

			for (Answer answer : list) {

				assertFalse("test data sets allowOtherText=false on all objects", answer.getQuestion().isAllowOtherText());

				if (answer.isBooleanAnswer()) {
					hasBoolean = true;
					assertEquals("Boolean Question 1", answer.getQuestion().getDisplayTitle());

					assertTrue(((BooleanAnswer) answer).getValue());
					assertEquals("true", ((BooleanAnswer) answer).getSimpleValue());
				}

				if (answer.isChoiceAnswer()) {
					hasChoice = true;
					assertEquals("Choice Question 1", answer.getQuestion().getDisplayTitle());

					ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
					assertNotNull(choiceAnswer);

					Choice choice = choiceAnswer.getValue();
					assertNotNull(choice);
					assertEquals(answer.getDisplayTitle(), answer.getSimpleValue());
					assertEquals(choice.getDisplayTitle(), choiceAnswer.getDisplayTitle());
					assertEquals("Fancy Choice 2", choiceAnswer.getSimpleValue());
					assertEquals("Fancy Choice 2", choice.getDisplayTitle());
				}

				// TODO this does NOT account for the essay question that's been
				// added
				if (answer.isTextAnswer()) {
					hasText = true;

					TextQuestion text = (TextQuestion) answer.getQuestion();

					assertEquals("Text Question 1", text.getDisplayTitle());

					assertEquals("should be correct size", 40, text.getFieldDisplayLength());
					assertEquals("should be correct size", 50, text.getMaximumLength());
					// always 1 for a generic text question
					assertEquals("should be correct size", 1, text.getNumRows());

					assertEquals("textAnswer", ((TextAnswer) answer).getValue());
					assertEquals("textAnswer", ((TextAnswer) answer).getSimpleValue());

					// test the TextMonthlyValue view
					Collection<TextCalendarValue> tmvList = reportingService.getTextResponsesPerMonth(answer.getQuestion(),
							new Date());
					assertNotNull("should have textMonthValue list", tmvList);
					assertTrue("should have textMonthValue list size > 0", tmvList.size() > 0);
					for (TextCalendarValue tmv : tmvList) {
						assertEquals("incorrect survey", survey.getId(), tmv.getSurvey().getId());
						assertEquals("incorrect question", answer.getQuestion().getId(), tmv.getId().getQuestion().getId());
						assertEquals("incorrect response", response.getId(), tmv.getId().getResponse().getId());

						assertEquals("incorrect date (month-precision)", DateUtils.truncate(new Date(), Calendar.MONTH), tmv
								.getId().getMonth());

						assertEquals("incorrect value", "textAnswer", tmv.getValue());
					}
				}
			}

			// must have one of each
			assertTrue("must have a boolean answer", hasBoolean);
			assertTrue("must have a choice answer", hasChoice);
			assertTrue("must have a text answer", hasText);
		}

		// question rules
		{
			// single show rule
			{
				Question q1 = survey.getQuestions().get(0);

				List<EntryRule> entry = find("from EntryRule where question = ?", q1);
				assertNotEmpty(entry);
				assertEquals(EntryRuleType.DEFAULT, entry.get(0).getRuleType());
				assertEquals(EntryRuleAction.SHOW_QUESTION, entry.get(0).getAction());
				assertEquals(0, entry.get(0).getApplyOrder());
				assertEquals(q1.getId(), entry.get(0).getQuestion().getId());
			}

			// two rules
			{
				Question q2 = survey.getQuestions().get(1);

				List<EntryRule> list = find("from EntryRule where question = ? order by applyOrder", q2);
				assertEquals(2, list.size());

				EntryRule entry1 = list.get(0);
				EntryRule entry2 = list.get(1);

				assertNotNull(entry1);
				assertNotNull(entry2);

				// 
				assertEquals(EntryRuleType.DEFAULT, entry1.getRuleType());
				assertEquals(EntryRuleAction.SHOW_QUESTION, entry1.getAction());
				assertEquals(0, entry1.getApplyOrder());
				assertEquals(0, list.indexOf(entry1));
				assertIsNull(entry1.getOtherObject());

				// 
				assertEquals(EntryRuleType.OTHER_ANSWERED, entry2.getRuleType());
				assertEquals(EntryRuleAction.SKIP_QUESTION, entry2.getAction());
				assertEquals(1, entry2.getApplyOrder());
				assertEquals(1, list.indexOf(entry2));
				assertNotNull(entry2.getOtherObject());

				//
				assertEquals(q2.getId(), entry1.getQuestion().getId());
				assertEquals(q2.getId(), entry2.getQuestion().getId());
			}

			// one exit rule
			{
				Question q4 = survey.getQuestions().get(4);

				List<ExitRule> exit = find("from ExitRule where question = ?", q4);
				assertEquals(1, exit.size());

				ExitRule rule = exit.get(0);

				assertNotEmpty(exit);
				assertEquals(ExitRuleType.DEFAULT, rule.getRuleType());
				assertEquals(ExitRuleAction.FORCE_FINISH, rule.getAction());
				assertEquals(0, rule.getApplyOrder());
				assertEquals(q4.getId(), rule.getQuestion().getId());

				assertIsNull(rule.getChoice());
				assertIsNull(rule.getJumpToQuestion());
			}
		}

	}

	@Test
	public void surveySummary_View() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.addHistoricalResponseData(survey);
		flushAndClear();

		Collection<SurveySummary> list = find("from SurveySummary where survey.id = ?", survey.getId());

		assertNotNull("no data", list);
		assertTrue("expected a result", list.size() > 0);
		assertTrue("expected only 1 result", list.size() == 1);

		SurveySummary total = list.iterator().next();
		assertNotNull(total);
		assertEquals("wrong survey", survey.getId(), total.getSurvey().getId());
		assertNotNull("no count data", total.getCount());

		// only counts historical data: the response in createTypicalScenario1
		// is not closed
		assertEquals("unexpected total count", Long.valueOf(4L), total.getCount());
	}

	@Test
	public void responsesPerDay_View() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.addDefaultScenario1Response(survey);
		scenarioDataUtil.addHistoricalResponseData(survey);
		flushAndClear();

		Collection<ResponsesPerDay> list = reportingService.getResponseRatePerDay(survey, 7);

		assertNotNull("no data", list);
		assertEquals("should be 7 days of data", 7, list.size());

		boolean atLeastOneNonZero = false;

		for (ResponsesPerDay item : list) {
			assertEquals("should be same survey", survey.getId(), item.getId().getSurvey().getId());
			assertNotNull("should have some count", item.getCount());

			if (item.getCount() > 0) {
				atLeastOneNonZero = true;
			}
		}

		assertTrue("all data is zero count: not valid", atLeastOneNonZero);
	}

	@Test
	public void scaleDailyBreakdown_View() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.firstResponse(survey);
		response.closeResponse();
		persist(response);
		flushAndClear();

		Collection<ScaleDailyBreakdown> list = find("from ScaleDailyBreakdown where survey = ?", survey);

		assertNotNull("no data", list);
		assertTrue("expected a result", list.size() > 0);
		assertTrue("expected only 1 result (scenario data has 1 scale question?)", list.size() == 1);

		ScaleDailyBreakdown total = list.iterator().next();
		assertNotNull(total);
		assertEquals("wrong survey", survey.getId(), total.getSurvey().getId());
		assertNotNull("no count data", total.getId().getAnswerValue());
		assertNotNull("no count data", total.getCount());

		assertEquals("unexpected answer value", Long.valueOf(5L), total.getId().getAnswerValue());
		assertEquals("unexpected total count", Long.valueOf(1L), total.getCount());
	}

	@Test
	public void scaleMonthlyBreakdown_View() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.firstResponse(survey);
		response.closeResponse();
		persist(response);
		flushAndClear();

		Collection<ScaleMonthlyBreakdown> list = find("from ScaleMonthlyBreakdown where survey = ?", survey);

		assertNotNull("no data", list);
		assertTrue("expected a result", list.size() > 0);
		assertTrue("expected only 1 result (scenario data has 1 scale question?)", list.size() == 1);

		ScaleMonthlyBreakdown total = list.iterator().next();
		assertNotNull(total);
		assertEquals("wrong survey", survey.getId(), total.getSurvey().getId());
		assertNotNull("no count data", total.getId().getAnswerValue());
		assertNotNull("no count data", total.getCount());

		assertEquals("unexpected answer value", Long.valueOf(5L), total.getId().getAnswerValue());
		assertEquals("unexpected total count", Long.valueOf(1L), total.getCount());
	}

	// @Test
	// public void testTextResponsesPerMonth() {
	// Survey survey =
	// scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(),
	// false);
	// scenarioDataUtil.addHistoricalResponseData(survey, true);
	// flushAndClear();
	//
	// }

	/**
	 * Test expected default return values for un(der)populated objects
	 */
	@Test
	public void defaultEmptyValues_BooleanQuestion() {
		BooleanQuestion bq = new BooleanQuestion();
		assertEquals("", bq.getDisplayTitle());
	}

	@Test
	public void defaultEmptyValues_ChoiceQuestion() {
		ChoiceQuestion cq = new ChoiceQuestion();
		assertEquals("", cq.getDisplayTitle());
	}

	@Test
	public void defaultEmptyValues_TextQuestion() {
		TextQuestion tq = new TextQuestion();
		assertEquals("", tq.getDisplayTitle());
	}

	@Test
	public void isBooleanQuestion() {
		Question q = new BooleanQuestion();
		assertTrue(q.isBooleanQuestion());
		assertFalse(q.isChoiceQuestion());
		assertFalse(q.isTextQuestion());
		assertFalse(q.isScaleQuestion());
	}

	@Test
	public void isChoiceQuestion() {
		Question q = new ChoiceQuestion();
		assertFalse(q.isBooleanQuestion());
		assertTrue(q.isChoiceQuestion());
		assertFalse(q.isTextQuestion());
		assertFalse(q.isScaleQuestion());
	}

	@Test
	public void isTextQuestion() {
		Question q = new TextQuestion();
		assertFalse(q.isBooleanQuestion());
		assertFalse(q.isChoiceQuestion());
		assertTrue(q.isTextQuestion());
		assertFalse(q.isScaleQuestion());
	}

	@Test
	public void isScaleQuestion() {
		Question q = new ScaleQuestion();
		assertFalse(q.isBooleanQuestion());
		assertFalse(q.isChoiceQuestion());
		assertFalse(q.isTextQuestion());
		assertTrue(q.isScaleQuestion());
	}

	// ======================================================================

	@Test
	public void isBooleanAnswer() {
		Answer q = new BooleanAnswer();
		assertTrue(q.isBooleanAnswer());
		assertFalse(q.isChoiceAnswer());
		assertFalse(q.isTextAnswer());
	}

	@Test
	public void isChoiceAnswer() {
		Answer q = new ChoiceAnswer();
		assertFalse(q.isBooleanAnswer());
		assertTrue(q.isChoiceAnswer());
		assertFalse(q.isTextAnswer());
	}

	@Test
	public void isTextAnswer() {
		Answer q = new TextAnswer();
		assertFalse(q.isBooleanAnswer());
		assertFalse(q.isChoiceAnswer());
		assertTrue(q.isTextAnswer());
	}

	@Test
	public void baseObjectGetDisplayTitle_DefaultInOnlyLanguage() {
		setEnglish();
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("fra"), "frenchValue");
		getHibernateTemplate().persist(survey);

		assertEquals("should get French value because it's the only one", "frenchValue", survey.getDisplayTitle());
	}

	@Test
	public void baseObjectGetDisplayTitle_DefaultInFirstLanguage() {
		setEnglish();
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("fra"), "frenchValue");
		getHibernateTemplate().persist(survey);

		assertEquals("should get French value because it's the first one", "frenchValue", survey.getDisplayTitle());
	}

	@Test
	public void baseObjectGetDisplayTitle_ExactMatchInMany_English() {
		setEnglish();
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("fra"), "frenchValue");
		survey.addObjectName(supportedLanguageService.findByCode("eng"), "englishValue");
		getHibernateTemplate().persist(survey);

		assertEquals("should get English value because it's an exact match", "englishValue", survey.getDisplayTitle());
	}

	@Test
	public void baseObjectGetDisplayTitle_ExactMatchInMany_French() {
		setFrench();
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("eng"), "englishValue");
		survey.addObjectName(supportedLanguageService.findByCode("fra"), "frenchValue");
		getHibernateTemplate().persist(survey);

		assertEquals("should get French value because it's an exact match", "frenchValue", survey.getDisplayTitle());
	}

	@Test
	public void baseAnswerFailsSimpleValue() {
		Answer a = new Answer();
		try {
			a.getSimpleValue();
			fail("base Answer is supposed to protect against calls to it's getSimpleAnswer method: it should never get invoked");
		} catch (RuntimeException re) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void baseObject_isSurvey_True() {
		assertTrue(new Survey().isSurveyType());
		assertTrue(new Question().isQuestionType());
		assertTrue(new Choice().isChoiceType());
	}

	@Test
	public void baseObject_isSurvey_False() {
		assertFalse(new Question().isSurveyType());
		assertFalse(new Choice().isQuestionType());
		assertFalse(new Survey().isChoiceType());
	}

	// ======================================================================

	/**
	 * This test ensures that question.getChoices() always returns Choices
	 * ordered by their displayOrder
	 */
	@Test
	public void questionChoice_DisplayOrderEnforced() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.addObjectName(supportedLanguageService.findByCode("eng"), "frenchValue");

		ChoiceQuestion question = new ChoiceQuestion(survey);
		question.setDisplayOrder(1L);
		question.addObjectName(supportedLanguageService.findByCode("eng"), "test");

		// add choices in random order, so that by default a query returns them
		// in that order
		question.addChoice(new Choice(question, 4L));
		question.addChoice(new Choice(question, 1L));
		question.addChoice(new Choice(question, 0L));
		question.addChoice(new Choice(question, 3L));
		question.addChoice(new Choice(question, 2L));

		survey.addQuestion(question);
		getHibernateTemplate().persist(survey);

		// clear persistence session
		flushAndClear();

		// reload
		Survey loaded = surveyService.findNonDeletedSurvey(survey.getId());
		assertNotNull(loaded);
		assertNotNull(loaded.getQuestions());
		// should be 1 question
		ChoiceQuestion loadedQuestion = (ChoiceQuestion) loaded.getQuestions().iterator().next();
		assertNotNull(loadedQuestion);
		assertNotNull(loadedQuestion.getChoices());
		assertEquals(5, loadedQuestion.getChoices().size());

		// basic validation
		assertEquals("wrong object loaded, or getDisplayTitle mangled", loaded.getDisplayTitle(), survey.getDisplayTitle());

		Long lastOrder = 0L;
		for (Choice choice : loadedQuestion.getChoices()) {
			assertEquals("wrong order", new Long(lastOrder++), choice.getDisplayOrder());
		}
	}

	// ======================================================================

	@Test
	public void supportedLanguageEquals_Null() {
		SupportedLanguage subject = new SupportedLanguage();
		assertFalse(subject.equals(null));
	}

	@Test
	public void supportedLanguageEquals_WrongClass() {
		SupportedLanguage subject = new SupportedLanguage();
		// never equal to another class, eg., string
		assertFalse(subject.equals("not the same class"));
	}

	// ======================================================================

	@Test
	public void responseQuestionOrderKey_Equals_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = firstResponse(survey);
		Question question = survey.getQuestions().get(0);

		ResponseQuestionOrderKey key1 = new ResponseQuestionOrderKey(response, question, 1);
		assertTrue("equals on same object failed", key1.equals(key1));

		{
			ResponseQuestionOrderKey key2 = new ResponseQuestionOrderKey(response, question, 1);
			assertTrue("equals on objects with same values failed", key1.equals(key2));
		}

		{
			ResponseQuestionOrderKey key3 = new ResponseQuestionOrderKey(response, question, 2);
			assertFalse("equals on objects with different values failed (expected !equals)", key1.equals(key3));
		}

		assertFalse("equals failed when a non-key was used (expected !equals)", key1.equals("this is not a key"));
	}

	@Test
	public void responseQuestionOrderKey_Hashcode_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = firstResponse(survey);
		Question question = survey.getQuestions().get(0);

		ResponseQuestionOrderKey key1 = new ResponseQuestionOrderKey(response, question, 1);

		{
			ResponseQuestionOrderKey key2 = new ResponseQuestionOrderKey(response, question, 1);
			assertTrue("hashcode on objects with same values failed", key1.hashCode() == key2.hashCode());
		}

		{
			ResponseQuestionOrderKey key3 = new ResponseQuestionOrderKey(response, question, 2);
			assertFalse("hashcode on objects with different values failed", key1.hashCode() == key3.hashCode());
		}

		assertFalse("hashcode failed when a non-key was used", key1.hashCode() == "this is not a key".hashCode());
	}

	// ======================================================================

	@Test
	public void responseChoiceOrderKey_Equals_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = firstResponse(survey);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull(question);
		Choice choice = question.getChoices().get(0);
		assertNotNull(choice);

		ResponseChoiceOrderKey key1 = new ResponseChoiceOrderKey(response, choice, 1);
		assertTrue("equals on same object failed", key1.equals(key1));

		{
			ResponseChoiceOrderKey key2 = new ResponseChoiceOrderKey(response, choice, 1);
			assertTrue("equals on objects with same values failed", key1.equals(key2));
		}

		{
			ResponseChoiceOrderKey key3 = new ResponseChoiceOrderKey(response, choice, 2);
			assertFalse("equals on objects with different values failed (expected !equals)", key1.equals(key3));
		}

		assertFalse("equals failed when a non-key was used (expected !equals)", key1.equals("this is not a key"));
	}

	@Test
	public void responseChoiceOrderKey_Hashcode_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = firstResponse(survey);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull(question);
		Choice choice = question.getChoices().get(0);
		assertNotNull(choice);

		ResponseChoiceOrderKey key1 = new ResponseChoiceOrderKey(response, choice, 1);

		{
			ResponseChoiceOrderKey key2 = new ResponseChoiceOrderKey(response, choice, 1);
			assertTrue("hashcode on objects with same values failed", key1.hashCode() == key2.hashCode());
		}

		{
			ResponseChoiceOrderKey key3 = new ResponseChoiceOrderKey(response, choice, 2);
			assertFalse("hashcode on objects with different values failed", key1.hashCode() == key3.hashCode());
		}

		assertFalse("hashcode failed when a non-key was used", key1.hashCode() == "this is not a key".hashCode());
	}

	// ======================================================================

	@Test
	public void getQuestionTypeCode_Text() {
		TextQuestion q = new TextQuestion();
		q.setNumRows(1);

		assertEquals(QuestionTypeCode.TEXT, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Essay() {
		TextQuestion q = new TextQuestion();
		q.setNumRows(2);

		assertEquals(QuestionTypeCode.ESSAY, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Boolean() {
		BooleanQuestion q = new BooleanQuestion();

		assertEquals(QuestionTypeCode.BOOLEAN, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Checkbox() {
		ChoiceQuestion q = new ChoiceQuestion();
		q.setUnlimited(true);

		assertEquals(QuestionTypeCode.CHECKBOX, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Radio() {
		ChoiceQuestion q = new ChoiceQuestion();
		q.setUnlimited(false);

		assertEquals(QuestionTypeCode.RADIO, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Select() {
		ChoiceQuestion q = new ChoiceQuestion();
		q.setUnlimited(false);
		q.setStyle(QuestionTypeConstants.STYLE_SELECT);
		assertEquals(QuestionTypeCode.SELECT, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_ConstantSum() {
		ChoiceQuestion q = new ChoiceQuestion();
		q.setUnlimited(true);
		q.setMaximumSum(100);

		assertEquals(QuestionTypeCode.CONSTANT_SUM, q.getQuestionTypeCode());
	}

	@Test
	public void getQuestionTypeCode_Scale() {
		ScaleQuestion q = new ScaleQuestion();

		assertEquals(QuestionTypeCode.SCALE, q.getQuestionTypeCode());
	}

	@Test
	public void scaleQuestion_PossibleValues() {
		long[] expected = new long[] { 2L, 3L, 4L };
		ScaleQuestion question = new ScaleQuestion(new Survey(), 2L, 4L);
		long[] possibleValues = question.getPossibleValues();

		for (int i = 0; i < 3; i++) {
			assertEquals("expected same numbers", expected[i], possibleValues[i]);
		}

		// assertTrue("expected same values array",
		// Arrays.asList(possibleValues).containsAll(Arrays.asList(expected)));
	}

	@Test
	public void entryExitRules_IsEntryExitRule_Success() {

		assertTrue(new EntryRule().isEntryRule());
		assertFalse(new EntryRule().isExitRule());

		assertTrue(new ExitRule().isExitRule());
		assertFalse(new ExitRule().isEntryRule());
	}
	/*
	 * // ======================================================================
	 * // CUSTOM REPORTS //
	 * ======================================================================
	 * 
	 * @Test public void customReport_ModelExercise() {
	 * 
	 * Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
	 * TextQuestion question1 = new TextQuestion(survey, 1, 50, 50, 0);
	 * TextQuestion question2 = new TextQuestion(survey, 1, 50, 50, 1);
	 * ChoiceQuestion question3 = new ChoiceQuestion(survey, 2L); Choice choice1
	 * = new Choice(question3, 0L); Choice choice2 = new Choice(question3, 1L);
	 * question3.addChoice(choice1); question3.addChoice(choice2); ScaleQuestion
	 * question4 = new ScaleQuestion(survey, 3L);
	 * 
	 * // survey.addQuestion(question1); survey.addQuestion(question2);
	 * survey.addQuestion(question3); survey.addQuestion(question4);
	 * 
	 * persist(survey);
	 * 
	 * CustomReport report = new CustomReport(survey);
	 * report.setClosedDateMax(new Date()); report.setClosedDateMin(new
	 * Date(report.getClosedDateMax().getTime() - 90000));
	 * report.addObjectName(getEnglish(), "Custom report test");
	 * report.addObjectResource(getEnglish(), "description",
	 * "A domain model exercise for the Custom Reports feature");
	 * 
	 * Slice slice1 = new Slice(report); Slice slice2 = new Slice(report); Slice
	 * slice3 = new Slice(report); Slice slice4 = new Slice(report);
	 * report.addSlice(slice1); report.addSlice(slice2);
	 * report.addSlice(slice3); report.addSlice(slice4);
	 * 
	 * ReportItem item1 = new ReportItem(report); ReportItem item2 = new
	 * ReportItem(report); ReportItem item3 = new ReportItem(report); ReportItem
	 * item4 = new ReportItem(report); report.addReportItem(item1);
	 * report.addReportItem(item2); report.addReportItem(item3);
	 * report.addReportItem(item4);
	 * 
	 * // // //
	 * 
	 * // if this question has any answer slice1.setType(Slice.Type.HAS_ANSWER);
	 * slice1.setQuestion(question1);
	 * 
	 * // check for a specific promo-code in question 2
	 * slice2.setType(Slice.Type.CONTAINS_TEXT);
	 * slice2.setContainsText("promo-123"); slice2.setQuestion(question2);
	 * 
	 * // specific choice selected slice3.setType(Slice.Type.CHOICES_SELECTED);
	 * slice3.setQuestion(question3); slice3.addChoice(choice1);
	 * 
	 * // specific numeric value to a question
	 * slice4.setType(Slice.Type.NUMERIC_VALUE_EQUALS);
	 * slice4.setQuestion(question4); slice4.setNumericValue(2L);
	 * 
	 * // // // item1.setType(ReportItem.Type.CHOICE_CHART);
	 * item1.setQuestion(question1);
	 * 
	 * item2.setType(ReportItem.Type.DAILY_BREAKDOWN);
	 * item2.setQuestion(question2);
	 * 
	 * item3.setType(ReportItem.Type.MONTHLY_BREAKDOWN);
	 * item3.setQuestion(question3);
	 * 
	 * item4.setType(ReportItem.Type.LANGUAGE_BREAKDOWN_CHART);
	 * item4.setQuestion(question4);
	 * 
	 * // fix-up index values report.compactLists();
	 * 
	 * // persist(report);
	 * 
	 * flushAndClear(); }
	 */
}

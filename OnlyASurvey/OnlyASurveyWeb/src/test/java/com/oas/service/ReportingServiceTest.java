package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.Actor;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.report.calendar.CalendarReport;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;
import com.oas.model.report.calendar.value.TextCalendarValue;

public class ReportingServiceTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private ReportingService service;

	private interface CalendarReportImplCallback {
		CalendarReport getCalendarReport(Survey survey);
	}

	@Test
	public void testGetResponseRatePerDay_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.addHistoricalResponseData(survey);

		// flush, clear and reload
		flushAndClear();

		Collection<Response> list = find("from Response where survey=?", survey);
		assertTrue("expected many responses back from db", list.size() > 3);
		assertTrue("excessive # responses", list.size() < 100);

		survey = surveyService.findNonDeletedSurvey(survey.getId());

		// sanity
		long lastDate = 0;
		for (Response response : list) {
			assertTrue("should be different dates from Hibernate query", response.getCreated().getTime() != lastDate);
			lastDate = response.getCreated().getTime();
		}
		assertTrue("has zero data", lastDate != 0);

		assertEquals("should have 1 day worth of data for today", 1, service.getResponseRatePerDay(survey, 1).size());
		assertEquals("should have 2 days worth of data for 2 days", 2, service.getResponseRatePerDay(survey, 2).size());
		assertEquals("should have 3 days worth of data for 3 days", 3, service.getResponseRatePerDay(survey, 3).size());

		// code will fill in any dates without data with zeros, test
		assertEquals("should have 10 days worth of data for 10 days", 10, service.getResponseRatePerDay(survey, 10).size());
	}

	@Test
	public void testGetResponseRatePerMonth_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		scenarioDataUtil.addHistoricalResponseData(survey);

		flushAndClear();

		Collection<Response> list = find("from Response where survey=?", survey);
		assertTrue("expected many responses back from db", list.size() > 3);
		assertTrue("excessive # responses", list.size() < 100);

		// flush, clear and reload
		flushAndClear();
		survey = surveyService.findNonDeletedSurvey(survey.getId());

		// sanity
		long lastDate = 0;
		for (Response response : list) {
			assertTrue("should be different dates from Hibernate query", response.getCreated().getTime() != lastDate);
			lastDate = response.getCreated().getTime();
		}
		assertTrue("has zero data", lastDate != 0);

		assertEquals("should have 1 month worth of data for this month", 1, service.getResponseRatePerMonth(survey, 1).size());
		assertEquals("should have 2 months worth of data for 2 months", 2, service.getResponseRatePerMonth(survey, 2).size());
		assertEquals("should have 3 months worth of data for 3 months", 3, service.getResponseRatePerMonth(survey, 3).size());

		// code will fill in any dates without data with zeros, test
		assertEquals("should have 10 months worth of data for 10 months", 10, service.getResponseRatePerMonth(survey, 10).size());
	}

	@Test
	public void testGetResponsesPerLanguage_Success() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		// List<Response> responseLst=find("from Response where survey = ?",
		// survey);
		// for (Response response : responseLst ) {
		// response.setClosed(true);
		// persist(response);
		// }

		// add 3 English responses
		for (int i = 0; i < 3; i++) {
			persist(new Response(survey, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP, true));
		}

		// add 2 French responses
		for (int i = 0; i < 2; i++) {
			persist(new Response(survey, new Date(), supportedLanguageService.findByCode("fra"), LOCALHOST_IP, true));
		}
		flushAndClear();

		Map<SupportedLanguage, Integer> list = service.getResponsesPerLanguage(survey);

		Integer eng = list.get(supportedLanguageService.findByCode("eng"));
		Integer fra = list.get(supportedLanguageService.findByCode("fra"));

		assertEquals("expected 60% English", new Integer(60), eng);
		assertEquals("expected 40% French", new Integer(40), fra);
	}

	// ======================================================================
	// Calendar Report
	// ======================================================================

	public void calendarReportImpl(CalendarReportImplCallback callback, int calendarPrecision) {

		Actor actor = createAndSetSecureUserWithRoleUser();

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), calendarPrecision);
		cal1.add(calendarPrecision, 0);

		// 1 month ago
		Calendar cal2 = DateUtils.truncate(Calendar.getInstance(), calendarPrecision);
		cal2.add(calendarPrecision, -1);

		// 2 months ago
		Calendar cal3 = DateUtils.truncate(Calendar.getInstance(), calendarPrecision);
		cal3.add(calendarPrecision, -2);

		// 3 months ago
		Calendar cal4 = DateUtils.truncate(Calendar.getInstance(), calendarPrecision);
		cal4.add(calendarPrecision, -3);

		Survey testData = scenarioDataUtil.createMonthlyReportTestSurvey(actor, cal1, cal2, cal3, cal4);
		Long surveyId = testData.getId();
		assertNotNull("defect in test", surveyId);

		flushAndClear();

		// load test data clean
		Survey survey = surveyService.findNonDeletedSurvey(surveyId);
		TextQuestion textQuestion = (TextQuestion) survey.getQuestions().get(0);
		ChoiceQuestion choiceQuestion = (ChoiceQuestion) survey.getQuestions().get(1);
		ScaleQuestion scaleQuestion = (ScaleQuestion) survey.getQuestions().get(2);

		//
		CalendarReport report = callback.getCalendarReport(survey);

		assertNotNull("null report", report);
		assertEquals("wrong survey", survey.getId(), report.getSurvey().getId());
		// one for text question, one for "other" text
		assertEquals("unexpected text question answer count", 2, report.getTextAnswerCounts().size());
		assertEquals("unexpected choice question answer count", 3, report.getChoiceAnswers().keySet().size());

		// check text answers
		{
			switch (calendarPrecision) {

			//
			// DAILY REPORT TEST
			//
			case Calendar.DAY_OF_MONTH:
				calendarReportImpl_DailyText(report, textQuestion, cal4);
				break;

			//
			// MONTHLY REPORT TEST
			//
			case Calendar.MONTH:
				calendarReportImpl_MonthlyText(report, textQuestion, cal4);
				break;

			//
			// UNSUPPORTED
			//
			default:
				fail("unknown calendar precision");
				break;
			}
		}

		// check multiple-choice answers
		{
			// choices are ordered by displayOrder: they're return here will
			// be predictably as it was declared above
			Choice choice1 = choiceQuestion.getChoices().get(0);
			Choice choice2 = choiceQuestion.getChoices().get(1);
			Choice choice3 = choiceQuestion.getChoices().get(2);

			for (Choice choice : choiceQuestion.getChoices()) {

				assertTrue("must have choice key", report.getChoiceAnswers().containsKey(choice));

				Map<Date, ChoiceCalendarBreakdown> map = report.getChoiceAnswers().get(choice);
				assertNotNull("no map for choice", map);

				// this test applies only to months
				// TODO add day-precision response data to scenario and test
				// it
				// here - will need to update the MONTH test below
				switch (calendarPrecision) {

				//
				// DAILY REPORT TEST
				//
				case Calendar.DAY_OF_MONTH:
					break;

				//
				// MONTHLY REPORT TEST
				//
				case Calendar.MONTH:

					if (choice1.getId().equals(choice.getId())) {
						// choice 1 was selected in 4 months
						assertEquals("unexpected count for choice#1", 4, getChoiceTotal(map));
					} else if (choice2.getId().equals(choice.getId())) {
						// choice 2 was selected in 2 months
						assertEquals("unexpected count for choice#2", 2, getChoiceTotal(map));
					} else if (choice3.getId().equals(choice.getId())) {
						// choice 3 was selected in 1 months
						assertEquals("unexpected count for choice#3", 1, getChoiceTotal(map));
					} else {
						fail("defect in test");
					}
					break;

				//
				// UNSUPPORTED
				//
				default:
					fail("unknown calendar precision");
					break;
				}
			}
		}

		// check scale questions
		{
			assertNotNull("no scale answer counts at all", report.getScaleAnswerCounts());
			assertEquals("unexpected count of scale questions", 1, report.getScaleAnswerCounts().size());

			Map<Date, Map<Long, Long>> map = report.getScaleAnswerCounts().get(scaleQuestion);
			assertNotNull("unable to get scale answer counts", map);
			assertEquals("should have 4 date units", 4, map.keySet().size());
		}
	}

	private void calendarReportImpl_DailyText(CalendarReport report, TextQuestion textQuestion, Calendar dateWithOtherText) {
		assertTrue("must have text question key", report.getTextAnswerCounts().containsKey(textQuestion));

		Map<Date, Long> map = report.getTextAnswerCounts().get(textQuestion);
		assertNotNull("no map for text question", map);

		// SCENARIO DATA - == same as # days in report
		int expectedTextCount = 4;

		assertEquals("unexpected number of text answers", expectedTextCount, map.keySet().size());

		for (Date date : map.keySet()) {
			Long count = report.getTextAnswerCounts().get(textQuestion).get(date);
			assertNotNull(count);
			int expected = 1;
			if (date.equals(dateWithOtherText)) {
				// this month had an "other" text
				expected = 2;
			}
			int actual = count.intValue();
			assertEquals("unexpected item count", expected, actual);
		}
	}

	private void calendarReportImpl_MonthlyText(CalendarReport report, TextQuestion textQuestion, Calendar dateWithOtherText) {
		assertTrue("must have text question key", report.getTextAnswerCounts().containsKey(textQuestion));

		Map<Date, Long> map = report.getTextAnswerCounts().get(textQuestion);
		assertNotNull("no map for text question", map);

		// SCENARIO DATA - == same as # months in report
		int expectedTextCount = 4;

		assertEquals("unexpected number of text answers", expectedTextCount, map.keySet().size());

		for (Date month : map.keySet()) {
			Long count = report.getTextAnswerCounts().get(textQuestion).get(month);
			assertNotNull(count);
			int expected = 1;
			if (month.equals(dateWithOtherText)) {
				// this month had an "other" text
				expected = 2;
			}
			assertEquals("unexpected item count for " + month, expected, count.intValue());
		}
	}

	@Test
	public void testDailyReport() {
		calendarReportImpl(new CalendarReportImplCallback() {
			@Override
			public CalendarReport getCalendarReport(Survey survey) {
				return service.getDailyReport(survey);
			}
		}, Calendar.DAY_OF_MONTH);
	}

	// ======================================================================
	// Monthly Report
	// ======================================================================

	@Test
	public void testMonthlyReport() {
		calendarReportImpl(new CalendarReportImplCallback() {
			@Override
			public CalendarReport getCalendarReport(Survey survey) {
				return service.getMonthlyReport(survey);
			}
		}, Calendar.MONTH);
	}

	/*
	 * @Test public void testGetMonthlyReport() {
	 * 
	 * Actor actor = createAndSetSecureUserWithRoleUser();
	 * 
	 * // this month Calendar cal1 = DateUtils.truncate(Calendar.getInstance(),
	 * Calendar.MONTH); cal1.add(Calendar.MONTH, 0);
	 * 
	 * // 1 month ago Calendar cal2 = DateUtils.truncate(Calendar.getInstance(),
	 * Calendar.MONTH); cal2.add(Calendar.MONTH, -1);
	 * 
	 * // 2 months ago Calendar cal3 =
	 * DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
	 * cal3.add(Calendar.MONTH, -2);
	 * 
	 * // 3 months ago Calendar cal4 =
	 * DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
	 * cal4.add(Calendar.MONTH, -3);
	 * 
	 * Survey testData = scenarioDataUtil.createMonthlyReportTestSurvey(actor,
	 * cal1, cal2, cal3, cal4, true); Long surveyId = testData.getId();
	 * assertNotNull("defect in test", surveyId);
	 * 
	 * flushAndClear();
	 * 
	 * // load test data clean Survey survey =
	 * surveyService.findSurveyById(surveyId); TextQuestion textQuestion =
	 * (TextQuestion) survey.getQuestions().get(0); ChoiceQuestion
	 * choiceQuestion = (ChoiceQuestion) survey.getQuestions().get(1);
	 * 
	 * MonthlyReport report = reportingService.getMonthlyReport(survey);
	 * 
	 * assertNotNull("null report", report); assertEquals("wrong survey",
	 * survey.getId(), report.getSurvey().getId()); // one for text question,
	 * one for "other" text
	 * assertEquals("unexpected text question answer count", 2,
	 * report.getTextAnswerCounts().size());
	 * assertEquals("unexpected choice question answer count", 3,
	 * report.getChoiceAnswers().keySet().size());
	 * 
	 * // check text answers { assertTrue("must have text question key",
	 * report.getTextAnswerCounts().containsKey(textQuestion));
	 * 
	 * Map<Date, Long> map = report.getTextAnswerCounts().get(textQuestion);
	 * assertNotNull("no map for text question", map);
	 * assertEquals("expected 4 items", 4, map.keySet().size());
	 * 
	 * for (Date month : map.keySet()) { Long count =
	 * report.getTextAnswerCounts().get(textQuestion).get(month);
	 * assertNotNull(count); int expected = 1; if (month.equals(cal4)) { // this
	 * month had an "other" text expected = 2; }
	 * assertEquals("unexpected item count", expected, count.intValue()); } }
	 * 
	 * // check multiple-choice answers { // choices are ordered by
	 * displayOrder: they're return here will be // predictably as it was
	 * declared above Choice choice1 = choiceQuestion.getChoices().get(0);
	 * Choice choice2 = choiceQuestion.getChoices().get(1); Choice choice3 =
	 * choiceQuestion.getChoices().get(2);
	 * 
	 * for (Choice choice : choiceQuestion.getChoices()) {
	 * 
	 * assertTrue("must have choice key",
	 * report.getChoiceAnswers().containsKey(choice));
	 * 
	 * Map<Date, ChoiceCalendarBreakdown> map =
	 * report.getChoiceAnswers().get(choice); assertNotNull("no map for choice",
	 * map);
	 * 
	 * if (choice1.getId().equals(choice.getId())) { // choice 1 was selected in
	 * 4 months assertEquals("unexpected count for choice#1", 4,
	 * getChoiceTotal(map)); } else if (choice2.getId().equals(choice.getId()))
	 * { // choice 2 was selected in 2 months
	 * assertEquals("unexpected count for choice#2", 2, getChoiceTotal(map)); }
	 * else if (choice3.getId().equals(choice.getId())) { // choice 3 was
	 * selected in 1 months assertEquals("unexpected count for choice#3", 1,
	 * getChoiceTotal(map)); } else { fail("defect in test"); } } } }
	 */
	private int getChoiceTotal(Map<Date, ChoiceCalendarBreakdown> map) {
		int count = 0;
		for (Date date : map.keySet()) {
			ChoiceCalendarBreakdown breakdown = map.get(date);
			count += breakdown.getCount();
		}
		return count;
	}

	// ======================================================================

	@Test
	public void testGetTextResponsesPerMonth_Success() {
		Actor actor = createAndSetSecureUserWithRoleUser();

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		DateUtils.truncate(cal1, Calendar.MONTH);
		cal1.add(Calendar.MONTH, 0);

		// 1 month ago
		Calendar cal2 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal2.add(Calendar.MONTH, -1);

		// 2 months ago
		Calendar cal3 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal3.add(Calendar.MONTH, -2);

		// 3 months ago
		Calendar cal4 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal4.add(Calendar.MONTH, -3);

		Survey testData = scenarioDataUtil.createMonthlyReportTestSurvey(actor, cal1, cal2, cal3, cal4);
		Long surveyId = testData.getId();
		assertNotNull("defect in test", surveyId);

		flushAndClear();

		// load test data clean
		Survey survey = surveyService.findNonDeletedSurvey(surveyId);

		// scenario specific
		TextQuestion textQuestion = (TextQuestion) survey.getQuestions().get(0);

		ArrayList<Calendar> calendarList = new ArrayList<Calendar>(4);
		calendarList.add(cal1);
		calendarList.add(cal2);
		calendarList.add(cal3);
		calendarList.add(cal4);
		for (Calendar calendar : calendarList) {
			Collection<TextCalendarValue> report = service.getTextResponsesPerMonth(textQuestion, calendar.getTime());
			assertNotNull("report missing", report);
			assertEquals("expected 1 text value per month", 1, report.size());
		}
	}

	// ======================================================================

	@Test
	public void testGetTextResponses_Success() {
		Actor actor = createAndSetSecureUserWithRoleUser();

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		DateUtils.truncate(cal1, Calendar.MONTH);
		cal1.add(Calendar.MONTH, 0);

		// 1 month ago
		Calendar cal2 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal2.add(Calendar.MONTH, -1);

		// 2 months ago
		Calendar cal3 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal3.add(Calendar.MONTH, -2);

		// 3 months ago
		Calendar cal4 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal4.add(Calendar.MONTH, -3);

		Survey testData = scenarioDataUtil.createMonthlyReportTestSurvey(actor, cal1, cal2, cal3, cal4);
		Long surveyId = testData.getId();
		assertNotNull("defect in test", surveyId);

		flushAndClear();

		// load test data clean
		Survey survey = surveyService.findNonDeletedSurvey(surveyId);

		// scenario specific
		TextQuestion textQuestion = (TextQuestion) survey.getQuestions().get(0);

		Map<Question, Collection<TextAnswer>> report = service.getTextAnswers(survey);
		assertNotNull("report missing", report);
		assertEquals("expected 2 collections (for 1 text question and 1 'other' answer to another question)", 2, report.size());

		Collection<TextAnswer> data = report.get(textQuestion);
		assertNotNull("no data for question", data);
		assertEquals("expected 4 answers for question", 4, data.size());
	}
}

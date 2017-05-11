package com.oas.controller.dashboard.report;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Actor;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.question.TextQuestion;
import com.oas.model.report.calendar.CalendarReport;
import com.oas.model.report.calendar.value.TextCalendarValue;
import com.oas.model.report.calendar.value.TextMonthlyValue;

public class CalendarBreakdownControllerTest extends AbstractOASBaseTest {

	@Autowired
	private CalendarBreakdownController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	/**
	 * Callback mechanism to make generic test impls.
	 * 
	 * @author xhalliday
	 * 
	 */
	private interface InvokeCalendarReportCallback {
		ModelAndView invoke(Survey survey);
	}

	private InvokeCalendarReportCallback getDailyReportCallback() {
		return new InvokeCalendarReportCallback() {
			@Override
			public ModelAndView invoke(Survey survey) {
				return controller.showDailyReport(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
			}
		};
	}

	private InvokeCalendarReportCallback getMonthlyReportCallback() {
		return new InvokeCalendarReportCallback() {
			@Override
			public ModelAndView invoke(Survey survey) {
				return controller.showMonthlyReport(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
			}
		};
	}

	@Test
	public void testDailyShowReport() {
		doTestShowReport(getDailyReportCallback());
	}

	@Test
	public void testMonthlyShowReport() {
		doTestShowReport(getMonthlyReportCallback());
	}

	private void doTestShowReport(InvokeCalendarReportCallback callback) {
		//		
		Actor actor = createAndSetSecureUserWithRoleUser();

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
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

		Survey survey = scenarioDataUtil.createMonthlyReportTestSurvey(actor, cal1, cal2, cal3, cal4);

		flushAndClear();

		ModelAndView mav = callback.invoke(survey);

		assertNotNull(mav);
		assertTrue("expected report view", mav.getViewName().endsWith("Breakdown"));
		assertNotNull("no model", mav.getModel());

		// basic tests - detailed ones are in the reporting service test
		CalendarReport report = (CalendarReport) mav.getModel().get("report");
		assertNotNull("no report data", report);
		assertEquals("wrong survey", survey.getId(), report.getSurvey().getId());
		assertTrue("expected choice answer data", report.getChoiceAnswers().size() > 0);
		assertTrue("expected text answer data", report.getTextAnswerCounts().size() > 0);
		assertEquals("expected correct early date", DateUtils.truncate(cal4.getTime(), Calendar.MONTH), report.getEarlyDate());
		assertEquals("expected correct late date", DateUtils.truncate(cal1.getTime(), Calendar.MONTH), report.getLateDate());

	}

	@Test
	public void testShowDailyReport_Fail_WrongUser() {
		doTestShowReport_Fail_WrongUser(getDailyReportCallback());
	}

	@Test
	public void testShowMonthlyReport_Fail_WrongUser() {
		doTestShowReport_Fail_WrongUser(getMonthlyReportCallback());
	}

	public void doTestShowReport_Fail_WrongUser(InvokeCalendarReportCallback callback) {
		//		
		Actor actor = createAndSetSecureUserWithRoleUser();

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
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

		Survey survey = scenarioDataUtil.createMonthlyReportTestSurvey(actor, cal1, cal2, cal3, cal4);

		flushAndClear();

		// change to some other user
		createAndSetSecureUserWithRoleUser();

		try {
			callback.invoke(survey);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testShowTextResponses_Daily() {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateSpec = df.format(new Date());

		doTestShowTextResponses(dateSpec);
	}

	@Test
	public void testShowTextResponses_Monthly() {

		DateFormat df = new SimpleDateFormat("yyyy-MM");
		String dateSpec = df.format(new Date());

		doTestShowTextResponses(dateSpec);
	}

	@Test
	public void testShowTextResponses_Invalid() {

		try {
			doTestShowTextResponses("i'm not a datte");
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@SuppressWarnings("unchecked")
	public void doTestShowTextResponses(String dateSpec) {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		// scenario specific
		TextQuestion textQuestion = getFirstQuestionOfType(survey, TextQuestion.class);
		assertNotNull("scenario data: unable to find a question", textQuestion);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + textQuestion.getId() + ".html");
		request.addParameter("d", dateSpec);
		ModelAndView mav = controller.showTextResponses(request);

		assertNotNull(mav);
		assertTrue("expected report view", mav.getViewName().endsWith("textResponses"));
		assertNotNull("no model", mav.getModel());

		// basic tests - detailed ones are in the reporting service test
		Collection<TextMonthlyValue> report = (Collection<TextMonthlyValue>) mav.getModel().get("report");
		assertNotNull("no report data", report);
		assertEquals("expected 1 result", 1, report.size());

		// the value
		{
			TextCalendarValue value = report.iterator().next();
			assertNotNull(value);
			assertNotNull(value.getSurvey());
			assertNotNull(value.getValue());
			assertNotNull(value.getId());
			assertNotNull(value.getId().getResponse());
			assertNotNull(value.getId().getQuestion());
			assertNotNull(value.getId().getMonth());

			assertEquals("unexpected survey", survey.getId(), value.getSurvey().getId());
		}
	}

	@Test
	public void testShowTextResponses_Fail_WrongUser_Daily() {

		doTestShowTextResponses_Fail_WrongUser("2008-12-01");
	}

	@Test
	public void testShowTextResponses_Fail_WrongUser_Monthly() {

		doTestShowTextResponses_Fail_WrongUser("2008-12");
	}

	protected void doTestShowTextResponses_Fail_WrongUser(String dateSpec) {

		// created as one user...
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		flushAndClear();

		// scenario specific
		TextQuestion textQuestion = getFirstQuestionOfType(survey, TextQuestion.class);
		assertNotNull("scenario data: unable to find a question", textQuestion);

		// ...change to some other user
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + textQuestion.getId() + ".html");
		request.addParameter("d", dateSpec);
		try {
			controller.showTextResponses(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testShowTextResponses_Fail_InvalidDate() {
		//		
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("txt", "2009-EH");
		try {
			controller.showTextResponses(request);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}

	}

}

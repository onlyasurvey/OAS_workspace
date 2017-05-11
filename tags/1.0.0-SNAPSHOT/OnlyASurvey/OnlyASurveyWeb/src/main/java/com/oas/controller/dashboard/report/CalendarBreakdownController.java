package com.oas.controller.dashboard.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.report.calendar.CalendarReport;
import com.oas.model.report.calendar.MonthlyReport;
import com.oas.model.report.calendar.value.TextCalendarValue;
import com.oas.security.SecurityAssertions;
import com.oas.service.ReportingService;

/**
 * Calendar Breakdown Report for a given calendar precision, e.g., day, month.
 * 
 * @author xhalliday
 * @since September 26, 2008
 */
@Controller
public class CalendarBreakdownController extends AbstractOASController {

	/** Service that provides report data. */
	@Autowired
	private ReportingService reportingService;

	/** Default constructor. */
	public CalendarBreakdownController() {
	}

	/**
	 * Daily Breakdown Report.
	 * 
	 * @param request
	 * @return
	 */
	@ValidUser
	@RequestMapping(value = "/db/rpt/dbd/*.html", method = RequestMethod.GET)
	public ModelAndView showDailyReport(HttpServletRequest request) {

		// load survey from URL, asserting ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// the report
		CalendarReport report = reportingService.getDailyReport(survey);

		Assert.notNull(report, "no report could be generated");
		Assert.notNull(report.getSurvey(), "no survey in report data");
		Assert.isTrue(report.getSurvey().getId().equals(survey.getId()), "wrong report data");

		// it's wide
		applyWideLayout(request);

		//
		return new ModelAndView("/reports/calendarBreakdown/dailyBreakdown", "report", report);
	}

	/**
	 * Monthly Breakdown Report.
	 * 
	 * @param request
	 * @return
	 */
	@ValidUser
	@RequestMapping(value = "/db/rpt/mbd/*.html", method = RequestMethod.GET)
	public ModelAndView showMonthlyReport(HttpServletRequest request) {

		// load survey from URL, asserting ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// the report
		MonthlyReport report = reportingService.getMonthlyReport(survey);

		Assert.notNull(report, "no report could be generated");
		Assert.notNull(report.getSurvey(), "no survey in report data");
		Assert.isTrue(report.getSurvey().getId().equals(survey.getId()), "wrong report data");

		// it's wide
		applyWideLayout(request);

		//
		return new ModelAndView("/reports/calendarBreakdown/monthlyBreakdown", "report", report);
	}

	/**
	 * Show text responses for a given date precision. ?txt can be YYYY-MM.
	 * 
	 * @param request
	 * @return
	 */
	@ValidUser
	@RequestMapping(value = "/db/rpt/txt/*.html", method = RequestMethod.GET, params = { "d" })
	public ModelAndView showTextResponses(HttpServletRequest request) {

		// question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);

		// must own survey
		SecurityAssertions.assertOwnership(question.getSurvey());

		String dateSpec = request.getParameter("d");

		Collection<TextCalendarValue> report = null;
		if (isDaySpec(dateSpec)) {
			report = reportingService.getTextResponsesPerDay(question, determineDate(dateSpec));
		} else if (isMonthSpec(dateSpec)) {
			report = reportingService.getTextResponsesPerMonth(question, determineDate(dateSpec));
		}

		Assert.notNull(report, "failed to load report: likely dateSpec was incorrect");

		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("survey", question.getSurvey());
		model.put("report", report);

		applyWideLayout(request);

		return new ModelAndView("/reports/calendarBreakdown/textResponses", model);
	}

	/**
	 * Parse a user-supplied date to a precision of one month.
	 * 
	 * @param value
	 * @return Date or null if couldn't parse one
	 */
	protected Date determineDate(String value) {
		Date retval = null;
		try {
			if (isMonthSpec(value)) {
				retval = new SimpleDateFormat("yyyy-MM").parse(value);
			}

			if (isDaySpec(value)) {
				// yyyy-mm-dd
				retval = new SimpleDateFormat("yyyy-MM-dd").parse(value);
			}

			// default null
		} catch (ParseException e) {
			// returning null
		}

		return retval;
	}

	protected boolean isMonthSpec(String value) {
		return StringUtils.hasText(value) && value.length() == 7;
	}

	protected boolean isDaySpec(String value) {
		return StringUtils.hasText(value) && value.length() == 10;
	}

}

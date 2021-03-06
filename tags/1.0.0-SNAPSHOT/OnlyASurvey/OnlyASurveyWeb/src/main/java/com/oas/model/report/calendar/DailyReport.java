package com.oas.model.report.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;
import com.oas.model.report.calendar.breakdown.ChoiceDailyBreakdown;

/**
 * A Daily report.
 * 
 * @author xhalliday
 * @since December 9, 2008
 */
public class DailyReport extends AbstractCalendarReport {

	/**
	 * Only constructor. Requires a Survey.
	 */
	public DailyReport(Survey survey) {
		super(survey);
	}

	// ======================================================================

	@Override
	public ChoiceCalendarBreakdown newChoiceCalendarBreakdown(Survey survey, Question question, Choice choice, Date date, Long count) {
		return new ChoiceDailyBreakdown(survey, question, choice, date, count);
	}

	@Override
	public DateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}
}

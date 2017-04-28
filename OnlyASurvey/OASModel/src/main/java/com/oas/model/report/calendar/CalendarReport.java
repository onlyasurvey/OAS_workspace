package com.oas.model.report.calendar;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;

/**
 * A report by some calendar increment, e.g., daily or monthly.
 * 
 * @author xhalliday
 * @since December 9, 2008
 */
public interface CalendarReport {

	/**
	 * Return the Survey for which this report was generated.
	 * 
	 * @return
	 */
	Survey getSurvey();

	Map<Choice, Map<Date, ChoiceCalendarBreakdown>> getChoiceAnswers();

	/**
	 * Return a count of textual answers for a question on a particular date.
	 * 
	 * @return
	 */
	Map<Question, Map<Date, Long>> getTextAnswerCounts();

	/**
	 * Return a count of scale answers for a question on a particular date.
	 * 
	 * [question] = { [2008-12-13] => { [scale] => [count] } }
	 * 
	 * @return
	 */
	Map<Question, Map<Date, Map<Long, Long>>> getScaleAnswerCounts();

	/**
	 * Return a new instance of the given ChoiceCalendarBreakdown (T).
	 * 
	 * @return
	 */
	ChoiceCalendarBreakdown newChoiceCalendarBreakdown(Survey survey, Question question, Choice choice, Date date, Long count);

	/**
	 * Add a date to the range of dates in the calendar. Called by generators to
	 * support earlyDate() and lateDate();
	 */
	void addDate(Date date);

	/**
	 * Get the list of dates for this report.
	 * 
	 * @return
	 */
	List<Date> getDateList();

	/**
	 * @return the earlyDate
	 */
	Date getEarlyDate();

	/**
	 * @return the lateDate
	 */
	Date getLateDate();

	/**
	 * Return the appropriate date format for this
	 * 
	 * @return
	 */
	DateFormat getDateFormat();
}

package com.oas.model.report.calendar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;

/**
 * A report broken down by some time factor, e.g., day or month.
 * 
 * The type T is
 * 
 * @author xhalliday
 * @since December 9, 2008 (refactor)
 * 
 * @param <T>
 */
abstract public class AbstractCalendarReport implements CalendarReport {

	/** The Survey this report pertains to. */
	private Survey survey;

	/** List of months for which this report has data. */
	private List<Date> dateList = new ArrayList<Date>();

	/**
	 * Breakdown by Question of TextAnswers.
	 */
	private Map<Question, Map<Date, Long>> textAnswerCounts = new HashMap<Question, Map<Date, Long>>();

	/**
	 * Breakdown by Question of ScaleAnswers.
	 */
	private Map<Question, Map<Date, Map<Long, Long>>> scaleAnswerCounts = new HashMap<Question, Map<Date, Map<Long, Long>>>();

	/**
	 * Breakdown data for a Choice. First key is a Choice, second key is a month
	 * (0-11).
	 */
	private Map<Choice, Map<Date, ChoiceCalendarBreakdown>> choiceAnswers = new HashMap<Choice, Map<Date, ChoiceCalendarBreakdown>>();

	/** Earliest date according to getMonthList(). */
	private Date earlyDate = null;

	/** Latest date according to getMonthList(). */
	private Date lateDate = null;

	// ======================================================================

	/**
	 * Only constructor. Requires a Survey.
	 */
	public AbstractCalendarReport(Survey survey) {
		this.survey = survey;
	}

	// ======================================================================

	@Override
	abstract public DateFormat getDateFormat();

	// ======================================================================

	/**
	 * Add a date to this report, which sets the list of dates with data and
	 * early and late dates (the date range).
	 * 
	 * @param date
	 *            java.util.Date expected to be set to midnight on the first day
	 *            of the month.
	 */
	public void addDate(Date date) {

		if (!dateList.contains(date)) {
			dateList.add(date);
		}

		// record earliest and latest dates added via this method.
		if (earlyDate == null || earlyDate.after(date)) {
			earlyDate = date;
		}
		if (lateDate == null || lateDate.before(date)) {
			lateDate = date;
		}

		Collections.sort(dateList);
	}

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the choiceAnswers
	 */
	public Map<Choice, Map<Date, ChoiceCalendarBreakdown>> getChoiceAnswers() {
		return choiceAnswers;
	}

	public Map<Question, Map<Date, Long>> getTextAnswerCounts() {
		return textAnswerCounts;
	}

	public Map<Question, Map<Date, Map<Long, Long>>> getScaleAnswerCounts() {
		return scaleAnswerCounts;
	}

	/**
	 * Returns a copy of the list of dates added to this object. Changing this
	 * method's return value will have no effect.
	 * 
	 * @return the monthList
	 */
	public List<Date> getDateList() {
		return new ArrayList<Date>(dateList);
	}

	/**
	 * @return the earlyDate
	 */
	public Date getEarlyDate() {
		return earlyDate;
	}

	/**
	 * @return the lateDate
	 */
	public Date getLateDate() {
		return lateDate;
	}
}

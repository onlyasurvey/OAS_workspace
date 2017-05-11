package com.oas.service;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.report.ResponsesPerDay;
import com.oas.model.report.ResponsesPerMonth;
import com.oas.model.report.calendar.DailyReport;
import com.oas.model.report.calendar.MonthlyReport;
import com.oas.model.report.calendar.value.TextCalendarValue;

/**
 * Service providing various data for reporting purposes.
 * 
 * @author xhalliday
 * @since September 27, 2008
 */
public interface ReportingService extends AbstractServiceInterface {

	/**
	 * Determine how many responses were entered for the survey between now and
	 * numDays ago.
	 * 
	 * @param survey
	 * @return Map<Date, Long> mapped by date, ordered ascending
	 */
	public Collection<ResponsesPerDay> getResponseRatePerDay(final Survey survey, final int numDays);

	/**
	 * Determine how many responses were entered for the survey between now and
	 * numMonths ago.
	 * 
	 * @param survey
	 * @return Map<Date, Long> mapped by date, ordered ascending
	 */
	public Collection<ResponsesPerMonth> getResponseRatePerMonth(final Survey survey, final int numMonths);

	/**
	 * Determine the percentage of respondents per language.
	 * 
	 * @param survey
	 * @return map of SupportedLanguage => Percentage
	 */
	public Map<SupportedLanguage, Integer> getResponsesPerLanguage(final Survey survey);

	/**
	 * Generate a Monthly Report.
	 * 
	 * @param survey
	 * @return MonthlyReport
	 */
	public MonthlyReport getMonthlyReport(Survey survey);

	/**
	 * Generate a Daily Report.
	 * 
	 * @param survey
	 * @return MonthlyReport
	 */
	public DailyReport getDailyReport(Survey survey);

	/**
	 * Gets all text responses for a survey.
	 * 
	 * @param survey
	 * @return
	 */
	public Map<Question, Collection<TextAnswer>> getTextAnswers(Survey survey);

	/**
	 * Gets all text responses for a year/month/day combination specified in the
	 * passed date.
	 * 
	 * @param date
	 *            Year/Month/Day are used to determine what to query.
	 * @param question
	 * @return
	 */
	public Collection<TextCalendarValue> getTextResponsesPerDay(Question question, Date date);

	/**
	 * Gets all text responses for a year/month combination specified in the
	 * passed date.
	 * 
	 * @param date
	 *            Year/Month are used to determine what to query.
	 * @param question
	 * @return
	 */
	public Collection<TextCalendarValue> getTextResponsesPerMonth(Question question, Date date);
}

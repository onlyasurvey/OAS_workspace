package com.oas.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.report.ResponsesPerDay;
import com.oas.model.report.ResponsesPerMonth;
import com.oas.model.report.abandonment.PartialResponseHighestQuestionSummary;
import com.oas.model.report.calendar.DailyReport;
import com.oas.model.report.calendar.MonthlyReport;
import com.oas.model.report.calendar.value.TextCalendarValue;
import com.oas.model.report.time.TimeTakenToComplete;

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
	 *            Subject
	 * @param numDays
	 *            Number of days to go back
	 * @return Collection<ResponsesPerDay>
	 */
	Collection<ResponsesPerDay> getResponseRatePerDay(final Survey survey, final int numDays);

	/**
	 * Determine how many responses were entered for the survey between now and
	 * numMonths ago.
	 * 
	 * @param survey
	 *            Subject
	 * @param numDays
	 *            Number of months to go back
	 * @return Collection<ResponsesPerMonth>
	 */
	Collection<ResponsesPerMonth> getResponseRatePerMonth(final Survey survey, final int numMonths);

	/**
	 * Determine the percentage of respondents per language.
	 * 
	 * @param survey
	 *            Subject
	 * @return map of SupportedLanguage => Percentage
	 */
	Map<SupportedLanguage, Integer> getResponsesPerLanguage(final Survey survey);

	/**
	 * Generate a Monthly Report.
	 * 
	 * @param survey
	 *            Subject
	 * @return MonthlyReport
	 */
	MonthlyReport getMonthlyReport(Survey survey);

	/**
	 * Generate a Daily Report.
	 * 
	 * @param survey
	 *            Subject
	 * @return MonthlyReport
	 */
	DailyReport getDailyReport(Survey survey);

	/**
	 * Gets all text responses for a survey.
	 * 
	 * @param survey
	 *            Subject
	 * @returnMap<Question, Collection<TextAnswer>>
	 */
	Map<Question, Collection<TextAnswer>> getTextAnswers(Survey survey);

	/**
	 * Gets all text responses for a year/month/day combination specified in the
	 * passed date.
	 * 
	 * @param date
	 *            Year/Month/Day are used to determine what to query.
	 * @param question
	 * @return
	 */
	Collection<TextCalendarValue> getTextResponsesPerDay(Question question, Date date);

	/**
	 * Gets all text responses for a year/month combination specified in the
	 * passed date.
	 * 
	 * @param date
	 *            Year/Month are used to determine what to query.
	 * @param question
	 * @return
	 */
	Collection<TextCalendarValue> getTextResponsesPerMonth(Question question, Date date);

	/**
	 * Gets a list of {@link PartialResponseHighestQuestionSummary} objects for
	 * the given Survey.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @return List<PartialResponseHighestQuestionSummary>
	 */
	List<PartialResponseHighestQuestionSummary> getAbandonmentHighestQuestionSummary(Survey survey);

	/**
	 * Get report data regarding how long people took to complete the survey.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @return List<TimeTakenToComplete>
	 */
	List<TimeTakenToComplete> getTimeTakenData(Survey survey);
}

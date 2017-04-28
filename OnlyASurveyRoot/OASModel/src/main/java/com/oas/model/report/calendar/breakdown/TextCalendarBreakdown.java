package com.oas.model.report.calendar.breakdown;

import com.oas.model.Survey;

/**
 * Parent interface for text breakdowns, by some calendar period.
 * 
 * @author xhalliday
 * @since December 9, 2008
 */
public interface TextCalendarBreakdown {

	TextCalendarBreakdownId getId();

	Survey getSurvey();

	Long getResponseCount();

}

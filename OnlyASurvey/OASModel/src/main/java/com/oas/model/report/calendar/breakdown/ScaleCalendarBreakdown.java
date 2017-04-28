package com.oas.model.report.calendar.breakdown;

import com.oas.model.Survey;

/**
 * Breaks down responses to Scale-type questions by date.
 * 
 * @author xhalliday
 * @since December 13, 2008
 */
public interface ScaleCalendarBreakdown {

	/**
	 * The composite key.
	 * 
	 * @return
	 */
	ScaleCalendarBreakdownId getId();

	/**
	 * Return the Survey.
	 * 
	 * @return
	 */
	Survey getSurvey();

	/**
	 * Response count for the range in the id.
	 * 
	 * @return
	 */
	Long getCount();

}

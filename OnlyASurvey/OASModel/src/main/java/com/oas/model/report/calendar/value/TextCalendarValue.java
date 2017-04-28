package com.oas.model.report.calendar.value;

import com.oas.model.Survey;

/**
 * Breaks down response data by day for a text question.
 * 
 * @author xhalliday
 * @since December 10, 2008
 */
public interface TextCalendarValue {

	// ======================================================================

	/**
	 * Return the composite key.
	 * 
	 * @return the id
	 */
	abstract public TextCalendarValueId getId();

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey();

	/**
	 * @return the value
	 */
	public String getValue();

}
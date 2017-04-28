package com.oas.model.report.calendar.breakdown;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Represents a count versus a question/survey pair.
 * 
 * @author xhalliday
 * @since December 9, 2008
 * 
 */
public interface ChoiceCalendarBreakdown {

	/**
	 * The composite key.
	 * 
	 * @return
	 */
	ChoiceCalendarBreakdownId getId();

	/**
	 * The question.
	 * 
	 * @return
	 */
	Question getQuestion();

	/**
	 * Response count for the range in the id.
	 * 
	 * @return
	 */
	Long getCount();

	// void setCount(Long count);

	Survey getSurvey();

}

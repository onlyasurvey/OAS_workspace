package com.oas.model.report.calendar.breakdown;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Question;

/**
 * Composite key for Scale calender breakdowns.
 * 
 * @author xhalliday
 * @since December 13, 2008
 */
@Embeddable
public class ScaleCalendarBreakdownId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1932852515925220127L;

	/** The relevant question. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "question_id", nullable = false, updatable = false)
	private Question question;

	/** Date range (month) to which it applies. */
	@Column(name = "target_date", updatable = false, insertable = false)
	private Date date;

	/** The actual Scale answer value. */
	@Column(name = "answer_value")
	private Long answerValue;

	// ======================================================================

	public ScaleCalendarBreakdownId() {
	}

	// public ScaleCalendarBreakdownId(Question question, Date date) {
	// this.question = question;
	// this.date = date;
	// // ======================================================================
	// }

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the answerValue
	 */
	public Long getAnswerValue() {
		return answerValue;
	}
}

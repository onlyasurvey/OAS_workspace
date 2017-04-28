package com.oas.model.report.calendar.breakdown;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Question;

/**
 * Composite key used in models that order data by response_id and language_id.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Embeddable
public class TextCalendarBreakdownId implements Serializable {

	/** The relevant question. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "question_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Question question;

	/** Date range (month) to which it applies. */
	@Column(name = "target_date", updatable = false, insertable = false)
	private Date date;

	// ======================================================================

	public TextCalendarBreakdownId() {
	}

	// ======================================================================

	/**
	 * @return the month
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

}

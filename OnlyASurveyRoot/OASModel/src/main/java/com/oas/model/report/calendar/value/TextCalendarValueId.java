package com.oas.model.report.calendar.value;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Question;
import com.oas.model.Response;

/**
 * Composite key used in models that order data by response_id and language_id.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Embeddable
public class TextCalendarValueId implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 1138946132565601668L;

	/** The response that the answer came from. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "response_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Response response;

	/** The relevant question. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "question_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Question question;

	/** Date range (month) to which it applies. */
	@Column(name = "target_date", updatable = false, insertable = false)
	private Date month;

	// ======================================================================

	public TextCalendarValueId() {
	}

	// ======================================================================

	/**
	 * @return the month
	 */
	public Date getMonth() {
		return month;
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

}

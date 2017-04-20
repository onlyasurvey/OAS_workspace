package com.oas.model.keys;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.oas.model.Question;
import com.oas.model.Response;

/**
 * Serves as a composite key joining Response and Question objects.
 * 
 * @author xhalliday
 * @since March 30, 2009
 */
@Embeddable
public class ResponseQuestionOrderKey implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 2099567170169420629L;

	@ManyToOne
	@JoinColumn(name = "response_id", nullable = false, updatable = false)
	private Response response;

	@ManyToOne
	@JoinColumn(name = "question_id", nullable = false, updatable = false)
	private Question question;

	@Column(name = "index_order", nullable = false, updatable = false)
	private int order;

	// ======================================================================

	public ResponseQuestionOrderKey() {
	}

	public ResponseQuestionOrderKey(Response response, Question question, int order) {
		this.response = response;
		this.question = question;
		this.order = order;
	}

	// ======================================================================

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ResponseQuestionOrderKey == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ResponseQuestionOrderKey rhs = (ResponseQuestionOrderKey) obj;
		return new EqualsBuilder().append(getResponse().getId(), getResponse().getId()).append(getQuestion().getId(),
				rhs.getQuestion().getId()).append(order, rhs.order).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(27, 29).append(getResponse().getId()).append(getQuestion().getId()).append(order).toHashCode();
	}

	// ======================================================================

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

}

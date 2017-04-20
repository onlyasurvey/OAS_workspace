package com.oas.model;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.keys.ResponseQuestionOrderKey;

/**
 * Stores the Response Question History - a record of which questions a
 * respondent was presented (regardless of whether they answered), and in what
 * order.
 * 
 * @author xhalliday
 * @since March 20, 2009
 */
@Entity
@Table(schema = "oas", name = "response_question_history")
public class ResponseQuestionHistory extends AbstractResourceModel implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -7392920178510634663L;

	/** Composite key for this entity. */
	// @Id
	@EmbeddedId
	private ResponseQuestionOrderKey id;

	// ======================================================================

	/* Default constructor. */
	public ResponseQuestionHistory() {
	}

	/** Basic constructor. */
	public ResponseQuestionHistory(Response response, Question question, int order) {
		this.id = new ResponseQuestionOrderKey(response, question, order);
	}

	// ======================================================================

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof ResponseQuestionHistory == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ResponseQuestionHistory rhs = (ResponseQuestionHistory) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(id, rhs.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(147, 39).appendSuper(super.hashCode()).append(id).toHashCode();
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public ResponseQuestionOrderKey getId() {
		return id;
	}

}

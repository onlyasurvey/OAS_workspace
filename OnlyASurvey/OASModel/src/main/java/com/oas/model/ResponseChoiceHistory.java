package com.oas.model;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.keys.ResponseChoiceOrderKey;

/**
 * Stores the Response Choice Order History - a record of which choices the
 * respondent was presented (regardless of whether they answered), and in what
 * order, keyed by Question. Used by ResponseQuestionHistory.
 * 
 * @author xhalliday
 * @since March 30, 2009
 */
@Entity
@Table(schema = "oas", name = "response_question_choice_history")
public class ResponseChoiceHistory extends AbstractResourceModel implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -2455097159855223011L;

	/** Composite key for this entity. */
	// @Id
	@EmbeddedId
	private ResponseChoiceOrderKey id;

	// ======================================================================

	/** Default constructor. */
	public ResponseChoiceHistory() {
	}

	/** Basic constructor. */
	public ResponseChoiceHistory(Response response, Choice choice, int order) {
		this.id = new ResponseChoiceOrderKey(response, choice, order);
	}

	// ======================================================================

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof ResponseChoiceHistory == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ResponseChoiceHistory rhs = (ResponseChoiceHistory) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(147, 39).append(id).toHashCode();
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public ResponseChoiceOrderKey getId() {
		return id;
	}

}

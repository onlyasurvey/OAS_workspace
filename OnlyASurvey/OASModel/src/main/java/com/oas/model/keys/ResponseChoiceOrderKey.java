package com.oas.model.keys;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.oas.model.Choice;
import com.oas.model.Response;

/**
 * Serves as a composite key joining Response and Choice objects.
 * 
 * @author xhalliday
 * @since March 31, 2009
 */
@Embeddable
public class ResponseChoiceOrderKey implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 2099567170169420629L;

	@ManyToOne
	@JoinColumn(name = "response_id", nullable = false, updatable = false)
	private Response response;

	@ManyToOne
	@JoinColumn(name = "choice_id", nullable = false, updatable = false)
	private Choice choice;

	@Column(name = "index_order", nullable = false, updatable = false)
	private int order;

	// ======================================================================

	public ResponseChoiceOrderKey() {
	}

	public ResponseChoiceOrderKey(Response response, Choice choice, int order) {
		this.response = response;
		this.choice = choice;
		this.order = order;
	}

	// ======================================================================

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ResponseChoiceOrderKey == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ResponseChoiceOrderKey rhs = (ResponseChoiceOrderKey) obj;
		return new EqualsBuilder().append(getResponse().getId(), rhs.getResponse().getId()).append(getChoice().getId(),
				rhs.getChoice().getId()).append(getOrder(), rhs.getOrder()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(27, 29).append(getResponse().getId()).append(getChoice().getId()).append(getOrder())
				.toHashCode();
	}

	// ======================================================================
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
	 * @return the choice
	 */
	public Choice getChoice() {
		return choice;
	}

}

package com.oas.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.answer.BooleanAnswer;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.ScaleAnswer;
import com.oas.model.answer.TextAnswer;

/**
 * Parent Answer type that defines all common answer attributes.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "answer")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Answer extends BaseObject {

	/**
	 * Serialize version ID.
	 */
	private static final long serialVersionUID = -3303997059139436853L;

	/** User response this answer is recorded on. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "response_id")
	private Response response;

	/** Question the answer pertains to. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id")
	private Question question;

	// ======================================================================
	public Answer() {
	}

	public Answer(Response response, Question question) {
		this.response = response;
		this.question = question;
	}

	// ======================================================================

	public boolean isBooleanAnswer() {
		return getClass().isAssignableFrom(BooleanAnswer.class);
	}

	public boolean isChoiceAnswer() {
		return getClass().isAssignableFrom(ChoiceAnswer.class);
	}

	public boolean isScaleAnswer() {
		return getClass().isAssignableFrom(ScaleAnswer.class);
	}

	public boolean isTextAnswer() {
		return getClass().isAssignableFrom(TextAnswer.class);
	}

	// ======================================================================

	public String getSimpleValue() {
		throw new RuntimeException("getSimpleValue cannot be called on parent Answer: only concrete answer types can have values");
	}

	// ======================================================================

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

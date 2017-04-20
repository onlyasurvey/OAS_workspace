package com.oas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.PageQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.util.QuestionTypeCode;
import com.oas.model.util.QuestionTypeConstants;

/**
 * Parent question type which defines all common question attributes.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Question extends BaseObject implements SurveyRelatedData, Comparable<Question> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3665529595789780512L;

	/** Containing Survey. */
	@OneToOne
	@JoinColumn(name = "survey_id")
	private Survey survey;

	/** Index of this Question in the Survey's list. */
	@Column(name = "display_order", nullable = false)
	private Long displayOrder;

	/** Provide "Other Text" input? */
	@Column(name = "allow_other_text")
	private boolean allowOtherText = false;

	/** Visual style. */
	private String style = "default";

	/** Whether a response is required to this Question. */
	private boolean required;

	// ======================================================================

	public Question() {
	}

	public Question(Survey survey) {
		this.survey = survey;
	}

	public Question(Survey survey, Long displayOrder) {
		this.survey = survey;
		this.displayOrder = displayOrder;
	}

	// ======================================================================

	public boolean isBooleanQuestion() {
		return getClass().isAssignableFrom(BooleanQuestion.class);
	}

	public boolean isChoiceQuestion() {
		return getClass().isAssignableFrom(ChoiceQuestion.class);
	}

	public boolean isSelectQuestion() {
		return isChoiceQuestion() && QuestionTypeCode.SELECT.equals(getQuestionTypeCode());
	}

	public boolean isTextQuestion() {
		return getClass().isAssignableFrom(TextQuestion.class);
	}

	public boolean isScaleQuestion() {
		return getClass().isAssignableFrom(ScaleQuestion.class);
	}

	public boolean isPageQuestion() {
		return getClass().isAssignableFrom(PageQuestion.class);
	}

	public String getQuestionTypeCode() {
		if (isBooleanQuestion()) {
			return QuestionTypeCode.BOOLEAN;
		} else if (isChoiceQuestion()) {

			//
			ChoiceQuestion thisChoiceQuestion = (ChoiceQuestion) this;

			//
			// multiple-choice questions for the basis for many types in the
			// business domain
			//
			if (thisChoiceQuestion.isSummingQuestion()) {
				//
				// constant sum
				//
				return QuestionTypeCode.CONSTANT_SUM;

			} else if (thisChoiceQuestion.isUnlimited()) {
				//
				// checkboxes
				//
				return QuestionTypeCode.CHECKBOX;

			} else {
				//
				if (QuestionTypeConstants.STYLE_SELECT.equals(getStyle())) {
					//
					// select list
					//
					return QuestionTypeCode.SELECT;

				} else {
					//
					// radio buttons
					//
					return QuestionTypeCode.RADIO;
				}
			}

		} else if (isTextQuestion()) {
			if (((TextQuestion) this).getNumRows() == 1) {
				// text
				return QuestionTypeCode.TEXT;
			} else {
				return QuestionTypeCode.ESSAY;
			}
		} else if (isScaleQuestion()) {
			return QuestionTypeCode.SCALE;
		} else if (isPageQuestion()) {
			return QuestionTypeCode.PAGE;
		}

		// BUG
		throw new RuntimeException("unknown question type");
	}

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the displayOrder
	 */
	public Long getDisplayOrder() {
		return displayOrder;
	}

	/**
	 * @param displayOrder
	 *            the displayOrder to set
	 */
	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

	/**
	 * @return the allowOtherText
	 */
	public boolean isAllowOtherText() {
		return allowOtherText;
	}

	/**
	 * @param allowOtherText
	 *            the allowOtherText to set
	 */
	public void setAllowOtherText(boolean allowOtherText) {
		this.allowOtherText = allowOtherText;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public int compareTo(Question o) {
		return getDisplayOrder().compareTo(o.getDisplayOrder());
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}
}

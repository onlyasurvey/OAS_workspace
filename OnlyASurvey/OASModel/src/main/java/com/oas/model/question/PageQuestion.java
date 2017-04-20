package com.oas.model.question;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Page question type.
 * 
 * @author Jason Halliday
 * @since May 25, 2009
 */
@Entity
@Table(schema = "oas", name = "page_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class PageQuestion extends Question {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2233840544638647619L;

	/** Should a "Back" button be shown to the respondent? */
	@Column(name = "show_back")
	private boolean showBack;

	/** Should a "Forward" button be shown to the respondent? */
	@Column(name = "show_forward")
	private boolean showForward;

	// ======================================================================

	/** Default constructor. */
	public PageQuestion() {
	}

	/** Simple constructor. */
	public PageQuestion(Survey survey) {
		super(survey);
	}

	/** Simple constructor. */
	public PageQuestion(Survey survey, Long displayOrder) {
		super(survey);
		setDisplayOrder(displayOrder);
	}

	/** Complex constructor. */
	public PageQuestion(Survey survey, boolean showBack, boolean showForward) {
		super(survey);
		setShowBack(showBack);
		setShowForward(showForward);
	}

	// ======================================================================

	/**
	 * @return the showBack
	 */
	public boolean isShowBack() {
		return showBack;
	}

	/**
	 * @param showBack
	 *            the showBack to set
	 */
	public void setShowBack(boolean showBack) {
		this.showBack = showBack;
	}

	/**
	 * @return the showForward
	 */
	public boolean isShowForward() {
		return showForward;
	}

	/**
	 * @param showForward
	 *            the showForward to set
	 */
	public void setShowForward(boolean showForward) {
		this.showForward = showForward;
	}
}

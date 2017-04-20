package com.oas.model.question;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.SupportedLanguage;

/**
 * Key for a label for a scale value, defined as a {@link SupportedLanguage} and
 * an integer scale value. Used in {@link ScaleQuestion}.
 * 
 * @author Jason Halliday
 * @since February 28, 2010
 */
@Embeddable
public class ScaleQuestionLabel {

	/**  */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", nullable = false)
	private SupportedLanguage language;

	@Column(name = "scale_value")
	private int scaleValue;

	public ScaleQuestionLabel() {
	}

	public ScaleQuestionLabel(SupportedLanguage language, int scaleValue) {
		this.language = language;
		this.scaleValue = scaleValue;
	}

	public SupportedLanguage getLanguage() {
		return language;
	}

	public int getScaleValue() {
		return scaleValue;
	}

}
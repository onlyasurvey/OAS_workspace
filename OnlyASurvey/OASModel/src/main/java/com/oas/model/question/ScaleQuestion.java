package com.oas.model.question;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKeyManyToMany;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Scale/Ratio question type - select value from range x to y.
 * 
 * TODO change to min/max to Integer
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "scale_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class ScaleQuestion extends Question {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5278163376472358393L;

	/** Minimum value for the scale. */
	@Column(nullable = false)
	private Long minimum = 1L;

	@Column(nullable = false)
	/* Maximum value for the scale. */
	private Long maximum = 10L;

	/**
	 * Should the question be rendered with labels only, and not include the
	 * scale value itself in the choices?
	 */
	@Column(name = "labels_only", nullable = false)
	private boolean labelsOnly;

	/** Labels for Scale/Ratio values. */
	@Basic(optional = true)
	@Column(name = "label_value")
	@CollectionOfElements
	@JoinTable(schema = "oas", name = "scale_question_label")
	@MapKeyManyToMany(joinColumns = { @JoinColumn(name = "question_id"), @JoinColumn(name = "language_id"),
			@JoinColumn(name = "scale_value") }, targetEntity = ScaleQuestionLabel.class)
	private final Map<ScaleQuestionLabel, String> labels = new HashMap<ScaleQuestionLabel, String>();

	// ======================================================================

	public ScaleQuestion() {
	}

	public ScaleQuestion(Survey survey) {
		super(survey);
	}

	public ScaleQuestion(Survey survey, Long displayOrder) {
		super(survey, displayOrder);
	}

	public ScaleQuestion(Survey survey, Long minimum, Long maximum) {
		super(survey);
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public ScaleQuestion(Survey survey, Long minimum, Long maximum, Long displayOrder) {
		super(survey);
		setDisplayOrder(displayOrder);

		this.minimum = minimum;
		this.maximum = maximum;
	}

	// ======================================================================

	public long[] getPossibleValues() {

		Assert.notNull(minimum);
		Assert.notNull(maximum);

		int max = maximum.intValue() - minimum.intValue() + 1;
		long[] retval = new long[max];

		int count = 0;
		for (long i = minimum; i <= maximum; i++) {
			retval[count++] = i;
		}

		return retval;
	}

	/**
	 * Get the label for a scale value (e.g., 1..11) for the user's locale (if
	 * possible), defaulting to the first label value found for a scale value
	 * where there is no language map to the current user's locale.
	 * 
	 * @param rating
	 *            int
	 * @return String
	 */
	public String getLabelForScale(int rating) {

		Locale locale = LocaleContextHolder.getLocale();
		Assert.notNull(locale, "no locale was found in LocaleContextHolder");
		String iso3lang = locale.getISO3Language();
		Assert.hasText(iso3lang, "no locale.ISO3Language was found in LocaleContextHolder");

		String defaultRetval = "";

		/*
		 * TODO this is inefficient, but SupportedLanguage isn't available here
		 * for querying against user's locale, e.g., in a ThreadLocale or some
		 * such.
		 */
		for (ScaleQuestionLabel key : getLabels().keySet()) {
			if (key.getScaleValue() == rating) {

				// try for an exact match
				if (key.getLanguage().getIso3Lang().equals(iso3lang)) {
					// this is our value: return immediately to avoid
					// defaultRetval
					return getLabels().get(key);
				}

				// set a default return value the first time a value is found
				if (defaultRetval == null) {
					defaultRetval = getLabels().get(key);
				}
			}
		}

		return defaultRetval;
	}

	// ======================================================================

	/**
	 * TODO change to Integer
	 * 
	 * @return the minimum
	 */
	public Long getMinimum() {
		return minimum;
	}

	/**
	 * TODO change to Integer
	 * 
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(Long minimum) {
		this.minimum = minimum;
	}

	/**
	 * @return the maximum
	 */
	public Long getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum
	 *            the maximum to set
	 */
	public void setMaximum(Long maximum) {
		this.maximum = maximum;
	}

	/**
	 * Accessor.
	 * 
	 * @return the labels
	 */
	public Map<ScaleQuestionLabel, String> getLabels() {
		return labels;
	}

	/**
	 * Accessor.
	 * 
	 * @param key
	 *            {@link ScaleQuestionLabel}
	 * @param value
	 *            String text value
	 */
	public void addLabel(ScaleQuestionLabel key, String value) {
		getLabels().put(key, value);
	}

	/**
	 * Accessor.
	 * 
	 * @return the labelsOnly
	 */
	public boolean isLabelsOnly() {
		return labelsOnly;
	}

	/**
	 * Accessor.
	 * 
	 * @param labelsOnly
	 *            the labelsOnly to set
	 */
	public void setLabelsOnly(boolean labelsOnly) {
		this.labelsOnly = labelsOnly;
	}

}

package com.oas.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import ca.inforealm.core.model.AbstractResourceModel;
import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * Name of an object, in as many languages are supported by the system.
 * 
 * @author xhalliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "object_name")
@SequenceGenerator(name = "objectNameSequence", sequenceName = "oas.object_name_id_seq")
public class ObjectName extends AbstractResourceModel implements Serializable, Comparable<ObjectName> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725051579823631887L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "objectNameSequence", strategy = GenerationType.SEQUENCE)
	@SuppressWarnings("unused")
	private Long id;

	/** The object which is named herein. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "object_id", insertable = true, updatable = false, nullable = false)
	private BaseObject object;

	/** Supported Language this text relates to. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id", nullable = false)
	private SupportedLanguage language;

	/** Language specific text value. */
	@Column(name = "text_value")
	private String value;

	// ======================================================================

	public ObjectName() {
	}

	public ObjectName(BaseObject object, SupportedLanguage language, String value) {
		super();

		setObject(object);
		setLanguage(language);
		setValue(value);
	}

	// ======================================================================

	@Override
	public int compareTo(ObjectName o2) {

		// the current locale's ISO3 code.
		String currentLanguage = LocaleContextHolder.getLocale().getISO3Language();

		String code1 = getLanguage().getIso3Lang();
		String code2 = o2.getLanguage().getIso3Lang();

		// as per the contract
		if (StringUtils.equals(code1, code2)) {
			return 0;
		}

		// if the language is the same as the current locale (e.g., user's
		// preferred language) then push it to the top
		if (currentLanguage.equals(code1)) {
			return -1;
		} else if (currentLanguage.equals(code2)) {
			return 1;
		} else {
			// compare based on displayTitle, so it's always alphabetical in the
			// user's language
			return code1.compareTo(code2);
		}
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	// public Long getId() {
	// return id;
	// }
	//
	// /**
	// * @param id
	// * the id to set
	// */
	// public void setId(Long id) {
	// this.id = id;
	// }
	/**
	 * @return the value
	 */
	@DisplayTitle
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the object
	 */
	public BaseObject getObject() {
		return object;
	}

	/**
	 * @param object
	 *            the object to set
	 */
	public void setObject(BaseObject object) {
		this.object = object;
	}

	/**
	 * @return the language
	 */
	public SupportedLanguage getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

}

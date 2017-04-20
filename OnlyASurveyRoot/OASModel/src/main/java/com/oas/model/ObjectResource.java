package com.oas.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.AbstractResourceModel;
import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * A text resource associated with some Base Object.
 * 
 * @author xhalliday
 * @since November 10, 2008
 */
@Entity
@Table(schema = "oas", name = "object_resource")
@SequenceGenerator(name = "objectResourceSequence", sequenceName = "oas.object_resource_id_seq")
public class ObjectResource extends AbstractResourceModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8604415353820425092L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "objectResourceSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** The object which is named herein. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "object_id")
	private BaseObject object;

	/** Supported Language this text relates to. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id")
	private SupportedLanguage supportedLanguage;

	/** Text-specific key. */
	@Column(name = "resource_key")
	private String key;

	/** Language-specific text value. */
	@Column(name = "text_value")
	private String value;

	// ======================================================================

	public ObjectResource() {
	}

	public ObjectResource(BaseObject object, SupportedLanguage supportedLanguage, String key, String value) {
		super();

		setObject(object);
		setKey(key);
		setSupportedLanguage(supportedLanguage);
		setValue(value);
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
	public SupportedLanguage getSupportedLanguage() {
		return supportedLanguage;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setSupportedLanguage(SupportedLanguage language) {
		this.supportedLanguage = language;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

}

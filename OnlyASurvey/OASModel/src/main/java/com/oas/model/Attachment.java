package com.oas.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKeyManyToMany;
import org.springframework.util.Assert;

import ca.inforealm.core.model.AbstractResourceModel;

/**
 * A user-supplied attachment to with some Base Object. Uses
 * <code>filename</code> property for a familiar metaphor to users. Payloads per
 * language are in the Map <code>payloads</code> keyed on SupportedLanguage.
 * <code>altText</code> on the individual payloads serve as proper titles.
 * 
 * @author xhalliday
 * @since November 10, 2008
 */
@Entity
@Table(schema = "oas", name = "object_attachment")
@SequenceGenerator(name = "objectAttachmentSequence", sequenceName = "oas.object_attachment_id_seq")
public class Attachment extends AbstractResourceModel implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 1429595343563283466L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "objectAttachmentSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** The object this attachment is for. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "object_id")
	private BaseObject subject;

	/** Content Type. */
	@Column(name = "content_type")
	private String contentType;

	/** General type indicator. */
	@Column(name = "type_code")
	@Enumerated(EnumType.STRING)
	private AttachmentType type;

	/** Payload data keyed on SupportedLanguage. */
	@MapKeyManyToMany(joinColumns = @JoinColumn(name = "language_id"), targetEntity = SupportedLanguage.class)
	@CollectionOfElements(fetch = FetchType.LAZY)
	@JoinTable(schema = "oas", name = "object_attachment_payload", joinColumns = { @JoinColumn(name = "id") })
	private Map<SupportedLanguage, AttachmentPayload> payloads = new HashMap<SupportedLanguage, AttachmentPayload>();

	// ======================================================================

	/**
	 * General type indicator - IMAGE, VIDEO, etc.
	 */
	public enum AttachmentType {
		/** Some other format. */
		UNKNOWN,

		/** An image. */
		IMAGE
	}

	// ======================================================================

	/** Default constructor. */
	public Attachment() {
	}

	/** Basic constructor. */
	public Attachment(BaseObject subject, AttachmentType type, String contentType) {
		setSubject(subject);
		setType(type);
		setContentType(contentType);
	}

	// ======================================================================

	/**
	 * Add a payload to this attachment. <code>payload.supportedLanguage</code>
	 * must be set.
	 */
	public void addPayload(SupportedLanguage language, AttachmentPayload payload) {
		Assert.notNull(this.payloads);
		Assert.notNull(payload);
		Assert.notNull(language);

		getPayloads().put(language, payload);
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the subject
	 */
	public BaseObject getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(BaseObject subject) {
		this.subject = subject;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the payloads
	 */
	public Map<SupportedLanguage, AttachmentPayload> getPayloads() {
		return payloads;
	}

	/**
	 * @return the type
	 */
	public AttachmentType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(AttachmentType type) {
		this.type = type;
	}

}

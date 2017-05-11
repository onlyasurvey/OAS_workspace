package ca.inforealm.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.annotation.DisplayTitle;

@Entity
@Table(name = "resource_string")
@SequenceGenerator(name = "idGenerator", sequenceName = "resource_string_id_seq")
public class ResourceString extends AbstractResourceModel {

	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToOne()
	@JoinColumn(name = "application_id")
	private Application application;

	@Column(name = "last_modified_date")
	private Date lastModifiedDate = new Date();

	/**
	 * Resource identifier (key). Called identifier instead of key because "key"
	 * is a reserved word in SQL RDBMSs.
	 */
	private String identifier;

	/** English value. */
	@Column(name = "value_en")
	private String valueEn;

	/** French value. */
	@Column(name = "value_fr")
	private String valueFr;

	// ======================================================================

	public ResourceString() {
	}

	public ResourceString(Application application, String identifier, String valueEn, String valueFr) {
		setApplication(application);
		setIdentifier(identifier);
		setValueEn(valueEn);
		setValueFr(valueFr);
	}

	// ======================================================================

	/**
	 * @return the application
	 */
	// TODO review this method; currently not referenced (only used in queries).
	// public Application getApplication() {
	// return application;
	// }
	/**
	 * @param application
	 *            the application to set
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the valueEn
	 */
	@DisplayTitle(language = "eng")
	public String getValueEn() {
		return valueEn;
	}

	/**
	 * @param valueEn
	 *            the valueEn to set
	 */
	public void setValueEn(String valueEn) {
		this.valueEn = valueEn;
	}

	/**
	 * @return the valueFr
	 */
	@DisplayTitle(language = "fra")
	public String getValueFr() {
		return valueFr;
	}

	/**
	 * @param valueFr
	 *            the valueFr to set
	 */
	public void setValueFr(String valueFr) {
		this.valueFr = valueFr;
	}

	/**
	 * @return the lastModifiedDate
	 */
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * @param lastModifiedDate
	 *            the lastModifiedDate to set
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}

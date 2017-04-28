package ca.inforealm.core.model;

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
@Table(name = "preference_definition")
@SequenceGenerator(name = "idGenerator", sequenceName = "preference_definition_id_seq")
public class PreferenceDefinition extends AbstractResourceModel {

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** The application to which this preference definition belongs. */
	@OneToOne
	@JoinColumn(name = "application_id")
	private Application application;

	/**
	 * Programmatic identifier, used to query/distinguish preference
	 * definitions.
	 */
	private String identifier;

	/** English display name. */
	@Column(name = "name_en")
	private String nameEn;

	/** French display name. */
	@Column(name = "name_fr")
	private String nameFr;

	// ----------------------------------------------------------------------

	/**
	 * @return the id
	 */
	// TODO review this method's usefulness; currently it is not referenced and
	// so removed from compilation
	// public Long getId() {
	// return id;
	// }
	/**
	 * @return the application
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * @param application
	 *            the application to set
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * @return the nameEn
	 */
	@DisplayTitle(language = "eng")
	public String getNameEn() {
		return nameEn;
	}

	/**
	 * @param nameEn
	 *            the nameEn to set
	 */
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	/**
	 * @return the nameFr
	 */
	@DisplayTitle(language = "fra")
	public String getNameFr() {
		return nameFr;
	}

	/**
	 * @param nameFr
	 *            the nameFr to set
	 */
	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
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

}

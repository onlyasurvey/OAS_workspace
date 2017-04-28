package ca.inforealm.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * Basic representation of an authorizable user.
 * 
 * @author Jason Halliday
 * @created April 4, 2008
 */
@Entity
@Table(name = "actor_group")
public class ActorGroup extends Actor {

	@OneToOne()
	@JoinColumn(name = "application_id")
	private Application application;

	@Column(name = "name_en")
	private String nameEn;

	@Column(name = "name_fr")
	private String nameFr;

	// ----------------------------------------------------------------------

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

}

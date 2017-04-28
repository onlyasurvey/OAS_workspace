package ca.inforealm.core.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * A SANE application.
 * 
 * @author Jason Mroz
 * @created April 4, 2008
 */
@Entity
@Table(name = "application")
@SequenceGenerator(name = "idGenerator", sequenceName = "application_id_seq", allocationSize = 1)
public class Application extends AbstractResourceModel {

	/**
	 * Primary key.
	 */
	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	/**
	 * Programmatic identifier for this application, such that it can be loaded
	 * by a configured textual value.
	 */
	@Column(nullable = false, unique = true)
	private String identifier;

	/**
	 * URL which can be accessed to start using this application.
	 */
	@Column(name = "start_url")
	private String startUrl;

	/**
	 * Definitions of roles for this application.
	 */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "application")
	@JoinColumn(name = "application_id")
	private Collection<RoleDefinition> roleDefinitions = new ArrayList<RoleDefinition>();

	/**
	 * Definitions of configuration items for this application.
	 */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "application")
	@JoinColumn(name = "application_id")
	private Collection<ConfigurationItem> configurationItems = new ArrayList<ConfigurationItem>();

	/**
	 * Definitions of preferences for this application.
	 */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "application")
	@JoinColumn(name = "application_id")
	private Collection<PreferenceDefinition> preferenceDefinitions = new ArrayList<PreferenceDefinition>();

	/**
	 * Definitions of configuration items for this application.
	 */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "application")
	@JoinColumn(name = "application_id")
	private Collection<ActorGroup> actorGroups = new ArrayList<ActorGroup>();

	@Column(name = "name_en")
	private String nameEn;

	@Column(name = "name_fr")
	private String nameFr;

	// ----------------------------------------------------------------------

	public Application() {
	}

	// ----------------------------------------------------------------------

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	public Collection<RoleDefinition> getRoleDefinitions() {
		return roleDefinitions;
	}

	/**
	 * @return the preferences
	 */
	public Collection<PreferenceDefinition> getPreferenceDefinitions() {
		return preferenceDefinitions;
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

	public Collection<ConfigurationItem> getConfigurationItems() {
		return configurationItems;
	}

	/**
	 * @return the actorGroups
	 */
	public Collection<ActorGroup> getActorGroups() {
		return actorGroups;
	}

}
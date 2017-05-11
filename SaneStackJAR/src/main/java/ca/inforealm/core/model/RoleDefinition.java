package ca.inforealm.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * Defines a role within an application.
 * 
 * @author Jason Halliday
 * @created April 4, 2008
 */
@Entity
@Table(name = "role_definition")
@SequenceGenerator(name = "idGenerator", sequenceName = "role_definition_id_seq")
public class RoleDefinition extends AbstractResourceModel {

	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	/**
	 * Identifier for the role, eg., ROLE_USER
	 */
	private String identifier;

	@OneToOne
	@JoinColumn(name = "application_id")
	private Application application;

	public RoleDefinition() {
	}

	public RoleDefinition(Application _application, String _identifier) {
		setApplication(_application);
		setIdentifier(_identifier);
	}

	// ----------------------------------------------------------------------

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	@DisplayTitle
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = (Application) application;
	}

}

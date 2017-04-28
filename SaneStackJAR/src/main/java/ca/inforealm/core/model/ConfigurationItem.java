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
@Table(name = "configuration_item")
@SequenceGenerator(name = "idGenerator", sequenceName = "configuration_item_id_seq")
public class ConfigurationItem extends AbstractResourceModel {

	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToOne
	@JoinColumn(name = "application_id")
	private Application application;

	/**
	 * Key used when storing/loading <code>ConfigurationItem</code>s from the
	 * back-end.
	 */
	private String identifier;

	/** "boolean", "number", "string" */
	@Column(name = "value_type")
	private String valueType;

	@Column(name = "item_value")
	private String value;

	// ======================================================================

	public ConfigurationItem() {
	}

	public ConfigurationItem(Application application, String identifier, String valueType, String value) {
		setApplication(application);
		setIdentifier(identifier);
		setValueType(valueType);
		setValue(value);
	}

	// ======================================================================

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@DisplayTitle
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}
}

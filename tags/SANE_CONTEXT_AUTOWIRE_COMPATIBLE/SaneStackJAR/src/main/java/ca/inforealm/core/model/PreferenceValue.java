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

@Entity
@Table(name = "preference_value")
@SequenceGenerator(name = "idGenerator", sequenceName = "preference_value_id_seq")
public class PreferenceValue extends AbstractResourceModel {

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** The definition to which this preference value belongs. */
	@OneToOne
	@JoinColumn(name = "definition_id")
	private PreferenceDefinition preferenceDefinition;

	/** The actor for which this preference is set. */
	@OneToOne
	@JoinColumn(name = "actor_id")
	private Actor actor;

	/** Preference value. */
	@Column(name = "pref_value")
	private String value;

	// ----------------------------------------------------------------------

	public PreferenceValue() {
	}

	public PreferenceValue(PreferenceDefinition preferenceDefinition, Actor actor, String value) {
		setPreferenceDefinition(preferenceDefinition);
		setActor(actor);
		setValue(value);
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the preferenceDefinition
	 */
	public PreferenceDefinition getPreferenceDefinition() {
		return preferenceDefinition;
	}

	/**
	 * @param preferenceDefinition
	 *            the preferenceDefinition to set
	 */
	public void setPreferenceDefinition(PreferenceDefinition preferenceDefinition) {
		this.preferenceDefinition = preferenceDefinition;
	}

	/**
	 * @return the actor
	 */
	// TODO review this method; currently not referenced (only used in queries).
	// public Actor getActor() {
	// return actor;
	// }
	/**
	 * @param actor
	 *            the actor to set
	 */
	public void setActor(Actor actor) {
		this.actor = actor;
	}

	/**
	 * @return the value
	 */
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

}
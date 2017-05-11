package ca.inforealm.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Links a user to a role in an application.
 * 
 * @author Jason Halliday
 * @created April 4, 2008
 */
@Entity
@Table(name = "actor_role")
@SequenceGenerator(name = "idGenerator", sequenceName = "actor_role_id_seq")
public class ActorRole extends AbstractResourceModel {

	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToOne(targetEntity = UserAccount.class)
	@JoinColumn(name = "actor_id")
	private Actor actor;

	@OneToOne(targetEntity = RoleDefinition.class)
	@JoinColumn(name = "role_id")
	private RoleDefinition role;

	// ----------------------------------------------------------------------
	public ActorRole() {
	}

	public ActorRole(Actor actor, RoleDefinition role) {
		setActor(actor);
		setRole(role);
	}

	// ----------------------------------------------------------------------

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the role
	 */
	public RoleDefinition getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(RoleDefinition role) {
		this.role = role;
	}

	/**
	 * @return the actor
	 */
	public Actor getActor() {
		return actor;
	}

	/**
	 * @param actor
	 *            the actor to set
	 */
	public void setActor(Actor actor) {
		this.actor = actor;
	}

}

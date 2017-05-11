package ca.inforealm.core.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Basic representation of an authorizable user.
 * 
 * NOTE that equals() and hashCode() only consider the ID.
 * 
 * @author Jason Halliday
 * @created April 4, 2008
 */
@Entity
@Table(name = "actor")
@SequenceGenerator(name = "idGenerator", sequenceName = "actor_id_seq")
@Inheritance(strategy = InheritanceType.JOINED)
public class Actor extends AbstractResourceModel {

	@Id
	@GeneratedValue(generator = "idGenerator", strategy = GenerationType.SEQUENCE)
	private Long id;

	// ======================================================================
	public Actor() {
	}

	public Actor(Long id) {
		this.id = id;
	}

	// ======================================================================

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Actor == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}

		Actor rhs = (Actor) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(id, rhs.id);
		return builder.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		return builder.toHashCode();
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

}

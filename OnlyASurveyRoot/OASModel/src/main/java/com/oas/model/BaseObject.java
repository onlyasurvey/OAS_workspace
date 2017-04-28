package com.oas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.model.AbstractResourceModel;

/**
 * Global parent type that defines all objects in this system.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "base_object")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq", allocationSize = 1)
public class BaseObject extends AbstractResourceModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3410263461613284038L;

	@Id
	@GeneratedValue(generator = "baseObjectSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "object")
	@JoinColumn(name = "object_id")
	// TODO remove and optimize getDisplayTitle
	// @LazyCollection(value = LazyCollectionOption.FALSE)
	@LazyCollection(value = LazyCollectionOption.TRUE)
	@Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	@Sort(type = SortType.NATURAL)
	private SortedSet<ObjectName> objectNames = new TreeSet<ObjectName>();

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "object", fetch = FetchType.LAZY)
	@Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	@JoinColumn(name = "object_id")
	@LazyCollection(LazyCollectionOption.TRUE)
	private Collection<ObjectResource> objectResources = new ArrayList<ObjectResource>();

	@Column(name = "date_created")
	private Date created = new Date();

	@Column(name = "date_deleted")
	private Date dateDeleted;

	@Column(name = "is_deleted")
	private boolean deleted;

	// ======================================================================

	/**
	 * Default constructor.
	 */
	public BaseObject() {
		super();
	}

	/**
	 * Constructor for subclasses that need to set an explicit ID.
	 * 
	 * @param id
	 *            Primary key
	 */
	protected BaseObject(Long id) {
		this.id = id;
	}

	// ======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		return builder.append(getId()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseObject == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		BaseObject rhs = (BaseObject) obj;
		return new EqualsBuilder().append(getId(), rhs.getId()).isEquals();
	}

	// ======================================================================

	public boolean isSurveyType() {
		return Survey.class.isAssignableFrom(this.getClass());
	}

	public boolean isQuestionType() {
		return Question.class.isAssignableFrom(this.getClass());
	}

	public boolean isChoiceType() {
		return Choice.class.isAssignableFrom(this.getClass());
	}

	// ======================================================================

	@Override
	public String getDisplayTitle() {

		Locale locale = LocaleContextHolder.getLocale();
		String matchLanguage = locale.getISO3Language();

		Assert.notNull(locale, "cannot call getDisplayTitle with no locale context");
		ObjectName defaultName = null;

		// TODO this is inefficient
		for (ObjectName name : getObjectNames()) {
			if (defaultName == null) {
				defaultName = name;
			} else {

				boolean currentIsEnglish = matchLanguage.equals(name.getLanguage().getIso3Lang());
				boolean defaultIsEnglish = matchLanguage.equals(defaultName.getLanguage().getIso3Lang());

				if (currentIsEnglish && !defaultIsEnglish) {
					// the current item is english, and current default (ie.,
					// first object) is NOT english: always make English default
					// if no exact match
					defaultName = name;
				}
			}

			if (matchLanguage.equals(name.getLanguage().getIso3Lang())) {
				// exact match on language
				return name.getValue();
			}
		}

		if (defaultName == null) {
			// no records at all
			return "";
		} else {
			return defaultName.getValue();
		}
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @return the dateDeleted
	 */
	public Date getDateDeleted() {
		return dateDeleted;
	}

	/**
	 * @param dateDeleted
	 *            the dateDeleted to set
	 */
	public void setDateDeleted(Date dateDeleted) {
		this.dateDeleted = dateDeleted;
	}

	public Collection<ObjectName> getObjectNames() {
		return objectNames;
	}

	public void addObjectName(SupportedLanguage language, String value) {
		setObjectName(language, value);
	}

	public void setObjectName(SupportedLanguage language, String value) {
		ObjectName target = null;
		for (ObjectName name : getObjectNames()) {
			if (name.getLanguage().getId().equals(language.getId())) {
				target = name;
				break;
			}
		}

		if (target == null) {
			// no existing name
			if (StringUtils.hasText(value)) {
				// only add non-null values
				objectNames.add(new ObjectName(this, language, value));
			}
		} else {
			if (StringUtils.hasText(value)) {
				// update existing name
				target.setValue(value);
			} else {
				// delete existing name
				getObjectNames().remove(target);
				// target.setObject(null);
			}
		}
	}

	public Collection<ObjectResource> getObjectResources() {
		return objectResources;
	}

	public void addObjectResource(SupportedLanguage language, String key, String value) {
		setObjectResource(language, key, value);
	}

	public void setObjectResource(SupportedLanguage language, String key, String value) {

		Assert.notNull(language);
		Assert.notNull(key);
		// value can be null

		ObjectResource target = null;
		for (ObjectResource resource : getObjectResources()) {
			if (key.equals(resource.getKey()) && resource.getSupportedLanguage().getId().equals(language.getId())) {
				target = resource;
				break;
			}
		}

		if (target == null) {
			// no existing resource
			if (value != null) {
				// only add non-null values
				objectResources.add(new ObjectResource(this, language, key, value));
			}
		} else {
			if (value != null) {
				// update existing resource
				target.setValue(value);
			} else {
				// delete existing resource
				getObjectResources().remove(target);
			}
		}
	}

	public void removeObjectResource(String key) {
		for (Iterator<ObjectResource> iterator = getObjectResources().iterator(); iterator.hasNext();) {
			ObjectResource resource = iterator.next();
			if (resource.getKey().equals(key)) {
				iterator.remove();
			}
		}
	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted
	 *            the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}

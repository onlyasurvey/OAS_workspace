package com.oas.model;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

/**
 * A language supported by the site.
 * 
 * @author xhalliday
 * @since September 15, 2008
 */
@Entity
@Table(schema = "oas", name = "supported_language")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SupportedLanguage extends BaseObject implements Comparable<SupportedLanguage> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2929068486990130552L;

	/** Language code in ISO-3 format. */
	@Column(name = "iso3lang", insertable = false, updatable = false, unique = true, nullable = false)
	private String iso3Lang;

	// ======================================================================

	public SupportedLanguage() {
	}

	public SupportedLanguage(String iso3Lang) {
		this.iso3Lang = iso3Lang;
	}

	// ======================================================================
	@Override
	public String toString() {
		return getDisplayTitle();
	}

	@Override
	public int compareTo(SupportedLanguage o2) {
		Assert.notNull(o2, "comparator doesn't handle nulls [2]");

		Assert.hasText(getIso3Lang(), "comparator doesn't handle empty text [1]");
		Assert.hasText(o2.getIso3Lang(), "comparator doesn't handle empty text [2]");

		// the current locale's ISO3 code.
		String currentLanguage = LocaleContextHolder.getLocale().getISO3Language();

		// as per contract
		if (StringUtils.equals(getIso3Lang(), o2.getIso3Lang())) {
			return 0;
		}

		// if the language is the same as the current locale (e.g., user's
		// preferred language) then push it to the top
		if (currentLanguage.equals(getIso3Lang())) {
			return -1;
		} else if (currentLanguage.equals(o2.getIso3Lang())) {
			return 1;
		} else {
			// compare based on displayTitle, so it's always alphabetical in the
			// user's language
			return getIso3Lang().compareTo(o2.getIso3Lang());
		}
	}

	// ======================================================================

	public Locale getLocale() {
		return LocaleUtils.toLocale(getIso3Lang().substring(0, 2));
	}

	// ======================================================================

	/**
	 * @return the iso3Lang
	 */
	public String getIso3Lang() {
		return iso3Lang;
	}

}

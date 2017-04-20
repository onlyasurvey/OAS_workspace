package com.oas.command.model;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import com.oas.model.SupportedLanguage;

/**
 * Command for setting object text - ie, ObjectResource values
 * 
 * @author xhalliday
 */
public class ObjectTextCommand extends NameObjectCommand {

	/** Key used for this text. */
	private String key;

	/** Default constructor. */
	public ObjectTextCommand() {
	}

	/** Constructor taking a key. */
	public ObjectTextCommand(String key) {
		this.key = key;
	}

	/** Constructor taking a key and list of supported languages. */
	public ObjectTextCommand(String key, Collection<SupportedLanguage> supportedLanguages) {
		super(supportedLanguages);
		this.key = key;
	}

	/** Constructor taking a key and map of values. */
	public ObjectTextCommand(String key, Map<String, String> map) {
		super(map);
		this.key = key;
	}

	// ======================================================================

	/**
	 * Return the value of this object in the user's locale.
	 * 
	 * @return String in the user's locale, if possible, first value otherwise
	 */
	public String getDisplayTitle() {
		if (getMap() == null || getMap().isEmpty()) {
			return "";
		}

		// a value specific to this language, if available
		String language = LocaleContextHolder.getLocale().getISO3Language();
		String bestMatch = getMap().get(language);

		if (StringUtils.hasText(bestMatch)) {
			return bestMatch;
		}

		// at this point there is no bestMatch, but there is at least one
		// value in the map
		String firstKey = getMap().keySet().iterator().next();
		return getMap().get(firstKey);
	}

	// ======================================================================

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

}

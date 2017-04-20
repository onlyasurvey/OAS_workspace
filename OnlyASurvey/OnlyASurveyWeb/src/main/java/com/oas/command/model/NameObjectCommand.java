package com.oas.command.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.oas.model.SupportedLanguage;

/**
 * Multi-lingual text command consisting of a map keyed on ISO 3-character
 * language code.
 * 
 * @author xhalliday
 */
public class NameObjectCommand {

	/** Internal map, keyed on language (ISO 3-character). */
	private Map<String, String> map = new HashMap<String, String>();

	/**
	 * Default constructor.
	 */
	public NameObjectCommand() {
	}

	/**
	 * Constructor that initializes the internal map such that there is an empty
	 * string for each language specified here.
	 * 
	 * @param supportedLanguages
	 */
	public NameObjectCommand(Collection<SupportedLanguage> supportedLanguages) {
		map = new HashMap<String, String>(supportedLanguages.size());
		for (SupportedLanguage language : supportedLanguages) {
			map.put(language.getIso3Lang(), "");
		}
	}

	/**
	 * Constructor that initializes the internal map using the one passed here.
	 * 
	 * @param map
	 */
	public NameObjectCommand(Map<String, String> map) {

		Map<String, String> newMap = new HashMap<String, String>(map);
		setMap(newMap);
	}

	/**
	 * Return true if the map contains at least one object and there is a value
	 * for any language in the map as per StringUtils.hasText().
	 * 
	 * @return
	 */
	public boolean hasText() {
		// no data at all
		if (CollectionUtils.isEmpty(map)) {
			return false;
		}

		// for each language in the map...
		for (String language : map.keySet()) {

			// if there's a value then return immediately
			if (StringUtils.hasText(map.get(language))) {
				return true;
			}
		}

		// no values for any language
		return false;
	}

	/**
	 * @return the map
	 */
	public Map<String, String> getMap() {
		return map;
	}

	public Map<String, String> getM() {
		return getMap();
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	/**
	 * ?m[k]=v
	 * 
	 * @param map
	 */
	public void setM(Map<String, String> map) {
		setMap(map);
	}

	public void addName(String languageCode, String value) {
		map.put(languageCode, value);
	}

}

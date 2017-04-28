package com.oas.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.IdListCommand;
import com.oas.model.SupportedLanguage;

/**
 * Service that handles SupportedLanguage related calls.
 * 
 * TODO periodic reload
 * 
 * @author xhalliday
 * @Since September 15, 2008
 */
public interface SupportedLanguageService extends AbstractServiceInterface {

	/**
	 * Get all supported languages.
	 * 
	 * @return Collection<SupportedLanguage>
	 */
	List<SupportedLanguage> getSupportedLanguages();

	/**
	 * Get the IDs of all supported languages.
	 * 
	 * @return
	 */
	List<Long> getSupportedLanguageIds();

	/**
	 * Return all supported languages keyed by language.
	 * 
	 * @return
	 */
	Map<String, SupportedLanguage> getSupportedLanguageMap();

	/**
	 * Find a supported language by it's backend ID.
	 * 
	 * @param id
	 * @return
	 */
	SupportedLanguage findById(Long id);

	/**
	 * Find a supported language by it's ISO-3 code.
	 * 
	 * @param isoCode
	 * @return
	 */
	SupportedLanguage findByCode(String isoCode);

	/**
	 * Given the IdListCommand return all matching supported languages.
	 * 
	 * @param idList
	 * @return
	 */
	Collection<SupportedLanguage> findByIdListCommand(IdListCommand idList);

	/**
	 * Given the list of IDs return all matching supported languages.
	 * 
	 * @param idList
	 * @return
	 */
	Collection<SupportedLanguage> findByIdList(Collection<Long> idList);

	/**
	 * Compare the specified IdListCommand (long values) to the list of
	 * supported languages, throwing an IllegalArgumentException if any ID
	 * specified isn't in the collection.
	 * 
	 * @param ids
	 * @return boolean
	 */
	boolean isValidIdList(IdListCommand command);
}

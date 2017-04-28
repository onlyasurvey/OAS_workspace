package com.oas.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.command.model.IdListCommand;
import com.oas.model.SupportedLanguage;
import com.oas.service.SupportedLanguageService;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SupportedLanguageServiceImpl extends AbstractServiceImpl implements SupportedLanguageService {

	private List<SupportedLanguage> supportedLanguages;
	private Map<Long, SupportedLanguage> idMap;
	private List<Long> idList;

	private boolean internalDataInitialized = false;

	/**
	 * Get all supported languages.
	 * 
	 * @return Collection<SupportedLanguage>
	 */
	@Override
	@Unsecured
	public List<SupportedLanguage> getSupportedLanguages() {
		initializeInternalData();
		return supportedLanguages;
	}

	@Override
	@Unsecured
	public List<Long> getSupportedLanguageIds() {

		initializeInternalData();
		Assert.notNull(idList);
		return idList;
	}

	@Override
	@Unsecured
	public Map<String, SupportedLanguage> getSupportedLanguageMap() {
		initializeInternalData();
		Collection<SupportedLanguage> languages = getSupportedLanguages();
		Assert.notNull(languages);
		Map<String, SupportedLanguage> map = new HashMap<String, SupportedLanguage>(languages.size());

		for (SupportedLanguage language : languages) {
			map.put(language.getIso3Lang(), language);
		}

		return map;
	}

	@Override
	@Unsecured
	public SupportedLanguage findById(Long id) {
		initializeInternalData();
		return idMap.get(id);
	}

	@Override
	@Unsecured
	public SupportedLanguage findByCode(String isoCode) {

		Assert.notNull(isoCode);

		initializeInternalData();

		for (SupportedLanguage language : getSupportedLanguages()) {
			if (language.getIso3Lang().equals(isoCode)) {
				return language;
			}
		}

		return null;
	}

	@Override
	@Unsecured
	public Collection<SupportedLanguage> findByIdList(Collection<Long> idList) {

		Assert.notNull(idList);
		Assert.notNull(supportedLanguages);

		initializeInternalData();

		Collection<SupportedLanguage> retval = new ArrayList<SupportedLanguage>(idList.size());
		for (SupportedLanguage language : supportedLanguages) {
			// match on ID
			if (idList.contains(language.getId())) {
				retval.add(language);
			}
		}
		return retval;
	}

	@Override
	@Unsecured
	public Collection<SupportedLanguage> findByIdListCommand(IdListCommand idList) {

		Assert.notNull(idList);

		initializeInternalData();

		return findByIdList(idList.getIds());
	}

	/**
	 * Compare the specified IdListCommand (long values) to the list of
	 * supported languages, returning false if any ID specified isn't in the
	 * collection.
	 * 
	 * @param command
	 */
	@Override
	@Unsecured
	public boolean isValidIdList(IdListCommand command) {

		initializeInternalData();

		// individual matches below
		boolean retval = true;

		Collection<Long> supportedIdList = getSupportedLanguageIds();
		// expects system data to already exist, otherwise entire system will
		// fail
		Assert.notNull(supportedIdList);
		Assert.notEmpty(supportedIdList);

		if (command == null || command.getIds() == null) {
			return false;
		}

		for (Long id : command.getIds()) {
			if (!supportedIdList.contains(id)) {
				retval = false;
				break;
			}
		}

		return retval;
	}

	private void initializeInternalData() {

		if (!internalDataInitialized) {

			// initialize list of supported languages and load
			supportedLanguages = find("from SupportedLanguage");

			int size = supportedLanguages.size();

			// initialize an ID <> language map
			idMap = new HashMap<Long, SupportedLanguage>(size);

			// initialize ID list
			idList = new ArrayList<Long>(size);

			// we need to walk the tree since these objects will be detached
			// also useful for debugging
			for (SupportedLanguage language : supportedLanguages) {

				Long languageId = language.getId();

				// walk it
				language.getIso3Lang();

				// add to map
				idMap.put(languageId, language);

				// add to list
				idList.add(languageId);

				//
				log.info("adding support for language: " + language.getDisplayTitle());
			}

			internalDataInitialized = true;
		}
	}

}

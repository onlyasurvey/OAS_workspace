package com.oas;

import org.springframework.beans.factory.annotation.Autowired;

import com.oas.command.model.IdListCommand;
import com.oas.command.model.PreferencesCommand;
import com.oas.service.SupportedLanguageService;

abstract public class AbstractPreferencesRelatedTest extends AbstractOASBaseTest {

	@Autowired
	protected SupportedLanguageService supportedLanguageService;

	protected PreferencesCommand newValidCommand() {
		Long englishId = supportedLanguageService.findByCode("eng").getId();
		Long frenchId = supportedLanguageService.findByCode("fra").getId();

		PreferencesCommand retval = new PreferencesCommand();
		retval.setLanguageId(englishId);
		retval.setSurveyLanguageIdList(new IdListCommand(new Long[] { englishId, frenchId }));
		return retval;
	}

	protected PreferencesCommand newInvalidCommand() {
		Long englishId = supportedLanguageService.findByCode("eng").getId();

		PreferencesCommand retval = new PreferencesCommand();
		retval.setLanguageId(englishId);
		retval.setSurveyLanguageIdList(new IdListCommand(new Long[] { englishId, 9213233L }));
		return retval;
	}
}

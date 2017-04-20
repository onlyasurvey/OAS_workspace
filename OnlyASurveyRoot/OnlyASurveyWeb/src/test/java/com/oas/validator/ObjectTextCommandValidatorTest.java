package com.oas.validator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.SupportedLanguage;
import com.oas.service.SupportedLanguageService;

/**
 * Unit tests for the ObjectTextCommandValidator.
 * 
 * @author xhalliday
 * @since February 4, 2009
 */
public class ObjectTextCommandValidatorTest extends AbstractOASBaseTest {

	/** A test key. */
	private final static String TEST_KEY = "someKey";

	/** Validator under test. */
	@Autowired
	private ObjectTextCommandValidator validator;

	/** For language-related stuff. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void supportsCorrectCommand() {
		assertNotNull(validator);

		// the base class
		assertTrue("does not support correct class", validator.supports(ObjectTextCommand.class));

		// some other command not in the same hierarchy
		assertFalse("supports incorrect class", validator.supports(IdListCommand.class));
	}

	@Test
	public void success() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		ObjectTextCommand noc = new ObjectTextCommand(TEST_KEY, languageList);
		for (SupportedLanguage language : languageList) {
			noc.addName(language.getIso3Lang(), "testValue");
		}

		// validator should return 0 errors - values for all languages are
		// present
		validateAndExpect(validator, noc, 0);
	}

	@Test
	public void failsWithMissingMapValue() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		ObjectTextCommand noc = new ObjectTextCommand(TEST_KEY, languageList);
		boolean skippedOne = false;
		for (SupportedLanguage language : languageList) {
			if (!skippedOne) {
				// do nothing here, so one language is skipped
				skippedOne = true;
				continue;
			}

			noc.addName(language.getIso3Lang(), "testValue");
		}

		// validator should return 1 error - one missing language, based on what
		// was passed in
		validateAndExpect(validator, noc, 1, new String[] { "error.objectText.missingInLanguage" });
	}

	@Test
	public void failsWithHugeText() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		ObjectTextCommand noc = new ObjectTextCommand(TEST_KEY, languageList);

		final String format = "%" + 128 * 1024 + "s";

		for (SupportedLanguage language : languageList) {
			noc.addName(language.getIso3Lang(), String.format(format, "testValue"));
		}

		validateAndExpect(validator, noc, 2, new String[] { "error.objectText.tooLong", "error.objectText.tooLong" });
	}

	@Test
	public void illegalArgumentWithNullMapKeys() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		ObjectTextCommand noc = new ObjectTextCommand(TEST_KEY, languageList);

		// null out
		for (SupportedLanguage language : languageList) {
			noc.getMap().remove(language.getIso3Lang());
		}

		validateAndExpect(validator, noc, 1, new String[] { "illegalArgument" });
	}

	@Test
	public void illegalArgumentWithNullMapEntries() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		ObjectTextCommand noc = new ObjectTextCommand(TEST_KEY, languageList);

		// null out
		for (SupportedLanguage language : languageList) {
			noc.getMap().put(language.getIso3Lang(), null);
		}

		validateAndExpect(validator, noc, 2, new String[] { "illegalArgument", "illegalArgument" });
	}
}

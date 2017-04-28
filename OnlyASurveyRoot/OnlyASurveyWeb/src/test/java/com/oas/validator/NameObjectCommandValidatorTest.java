package com.oas.validator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.model.SupportedLanguage;
import com.oas.service.SupportedLanguageService;

/**
 * Unit tests for the NameObjectCommandValidator
 * 
 * @author xhalliday
 * @since December 18,2008
 */
public class NameObjectCommandValidatorTest extends AbstractOASBaseTest {

	/** Validator under test. */
	@Autowired
	private NameObjectCommandValidator validator;

	/** For language-related stuff. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void testSupportsCorrectCommand() {
		assertNotNull(validator);

		// the base class
		assertTrue("does not support correct class", validator.supports(NameObjectCommand.class));

		// a derivative
		assertTrue("supports incorrect class", validator.supports(CreateQuestionCommand.class));

		// some other command not in the same hierarchy
		assertFalse("supports incorrect class", validator.supports(IdListCommand.class));
	}

	@Test
	public void testSuccess() {
		// support every language
		NameObjectCommand noc = new NameObjectCommand(supportedLanguageService.getSupportedLanguages());
		for (SupportedLanguage language : supportedLanguageService.getSupportedLanguages()) {
			noc.addName(language.getIso3Lang(), "testValue");
		}

		// validator should return 0 errors - values for all languages are
		// present
		validateAndExpect(validator, noc, 0);
	}

	@Test
	public void testFailsWithMissingMapValue() {
		// support every language
		NameObjectCommand noc = new NameObjectCommand(supportedLanguageService.getSupportedLanguages());
		boolean skippedOne = false;
		for (SupportedLanguage language : supportedLanguageService.getSupportedLanguages()) {
			if (!skippedOne) {
				// do nothing here, so one language is skipped
				skippedOne = true;
				continue;
			}

			noc.addName(language.getIso3Lang(), "testValue");
		}

		// validator should return 1 error - one missing language, based on what
		// was passed in
		// Errors errors = new BindException(noc, "command");
		// validator.validate(noc, errors);
		validateAndExpect(validator, noc, 1, new String[] { "error.nameObject.missingInLanguage" });
	}

	@Test
	public void testFailsWithHugeText() {
		// support every language
		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		NameObjectCommand noc = new NameObjectCommand(languageList);

		final String format = "%" + 128 * 1024 + "s";

		for (SupportedLanguage language : languageList) {
			noc.addName(language.getIso3Lang(), String.format(format, "testValue"));
		}

		validateAndExpect(validator, noc, 2, new String[] { "error.nameObject.tooLong", "error.nameObject.tooLong" });
	}
}

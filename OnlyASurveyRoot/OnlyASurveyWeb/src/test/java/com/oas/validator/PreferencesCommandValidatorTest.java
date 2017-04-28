package com.oas.validator;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractPreferencesRelatedTest;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.PreferencesCommand;

public class PreferencesCommandValidatorTest extends AbstractPreferencesRelatedTest {

	@Autowired
	private PreferencesCommandValidator validator;

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(validator.supports(PreferencesCommand.class));
	}

	// ======================================================================

	@Test
	public void testSuccess() {
		// returns a valid command
		PreferencesCommand command = newValidCommand();
		validateAndExpect(validator, command, 0);
	}

	// ======================================================================

	@Test
	public void testFail_InvalidLanguageId_Null() {
		PreferencesCommand command = newValidCommand();
		command.setLanguageId(null);
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testFail_InvalidLanguageId_NoSuchLanguage() {
		PreferencesCommand command = newValidCommand();
		command.setLanguageId(-409823L);
		validateAndExpect(validator, command, 1);
	}

	// ======================================================================

	@Test
	public void testFail_InvalidSurveyLanguages_Null() {
		PreferencesCommand command = newValidCommand();
		command.setSurveyLanguageIdList(null);
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testFail_InvalidSurveyLanguages_NoSuchLanguage() {
		PreferencesCommand command = newValidCommand();
		command.setSurveyLanguageIdList(new IdListCommand(new Long[] { -2329923L }));
		validateAndExpect(validator, command, 1);
	}

}
package com.oas.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.service.SupportedLanguageService;

public class CreateSurveyValidatorTest extends AbstractOASBaseTest {

	@Autowired
	private CreateSurveyValidator createSurveyValidator;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(createSurveyValidator.supports(IdListCommand.class));
	}

	@Test
	public void testValidData_Success() {
		// valid data
		IdListCommand command = new IdListCommand();
		command.setIds(supportedLanguageService.getSupportedLanguageIds());

		Errors errors = new BindException(command, "cmd");
		createSurveyValidator.validate(command, errors);
		assertFalse("should have zero errors", errors.hasErrors());
	}

	@Test
	public void testInvalidDataFails_Null() {
		// valid data
		IdListCommand command = new IdListCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setIds(null);

		Errors errors = new BindException(command, "cmd");
		createSurveyValidator.validate(command, errors);
		assertEquals("should have 1 error", 1, errors.getAllErrors().size());
	}

	@Test
	public void testInvalidDataFails_EmptyList() {
		// valid data
		IdListCommand command = new IdListCommand();
		command.setIds(Collections.EMPTY_LIST);

		Errors errors = new BindException(command, "cmd");
		createSurveyValidator.validate(command, errors);
		assertEquals("should have 1 error", 1, errors.getAllErrors().size());
	}

	@Test
	public void testInvalidDataFails_IllegalId() {
		// valid data
		IdListCommand command = new IdListCommand();
		command.setIds(new ArrayList<Long>());
		command.addId(-2912372L);

		Errors errors = new BindException(command, "cmd");
		createSurveyValidator.validate(command, errors);
		assertEquals("should have 1 error", 1, errors.getAllErrors().size());
	}
}

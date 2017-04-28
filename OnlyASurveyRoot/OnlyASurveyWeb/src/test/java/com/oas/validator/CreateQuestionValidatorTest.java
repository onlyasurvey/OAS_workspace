package com.oas.validator;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.model.SupportedLanguage;
import com.oas.model.util.QuestionTypeCode;

public class CreateQuestionValidatorTest extends AbstractOASBaseTest {

	@Autowired
	private CreateQuestionValidator createQuestionValidator;

	// ======================================================================

	private void validateAndExpect(CreateQuestionCommand command, int expectedErrors) {
		validateAndExpect(createQuestionValidator, command, expectedErrors, null);
	}

	private void validateAndExpect(CreateQuestionCommand command, int expectedErrors, String[] errorCodes) {
		validateAndExpect(createQuestionValidator, command, expectedErrors, errorCodes);
	}

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(createQuestionValidator.supports(CreateQuestionCommand.class));
	}

	@Test
	public void testInvalidTypeCodeFails_Null() {
		// valid data
		CreateQuestionCommand command = new CreateQuestionCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setTypeCode(null);

		validateAndExpect(command, 1);
	}

	@Test
	public void testInvalidTypeCodeFails_IllegalId() {
		// valid data
		CreateQuestionCommand command = new CreateQuestionCommand();
		command.setTypeCode("fancyAjax234");

		validateAndExpect(command, 1);
	}

	// ======================================================================

	@Test
	public void testName_FailsWithHugeText() {
		// support every language

		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(35);

		List<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();

		final String format = "%" + 128 * 1024 + "s";

		for (SupportedLanguage language : languageList) {
			command.addName(language.getIso3Lang(), String.format(format, "testValue"));
		}

		validateAndExpect(command, 2, new String[] { "error.nameObject.tooLong", "error.nameObject.tooLong" });
	}

	// ======================================================================

	// ======================================================================
	// TEXT type validation
	// ======================================================================

	@Test
	public void testText_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(35);

		validateAndExpect(command, 0);
	}

	@Test
	public void testText_Fail_NumRows_Min() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(0);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(35);

		validateAndExpect(command, 1);
	}

	@Test
	public void testText_Fail_NumRows_Max() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(2); // should ALWAYS be 1
		command.setMaximumLength(50);
		command.setFieldDisplayLength(35);

		validateAndExpect(command, 1);
	}

	@Test
	public void testText_Fail_MaxLength_Min() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(0);
		command.setFieldDisplayLength(35);

		validateAndExpect(command, 1);
	}

	@Test
	public void testText_Fail_MaxLength_Max() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(1048576 * 1024); // a gig should always fail
		command.setFieldDisplayLength(35);

		validateAndExpect(command, 1);
	}

	@Test
	public void testText_Fail_FieldDisplayLength_Min() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 1);
	}

	@Test
	public void testText_Fail_FieldDisplayLength_Max() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.TEXT);
		command.setNumRows(1);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(1024);

		validateAndExpect(command, 1);
	}

	// ======================================================================
	// ESSAY type validation
	// ======================================================================

	@Test
	public void testEssay_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(2);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 0);
	}

	@Test
	public void testEssay_Fail_NumRows_Min() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(1);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 1);
	}

	@Test
	public void testEssay_Fail_NumRows_Max() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(2000);
		command.setMaximumLength(50);
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 1);
	}

	@Test
	public void testEssay_Fail_MaxLength_Min() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(2);
		command.setMaximumLength(0);
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 1);
	}

	@Test
	public void testEssay_Fail_MaxLength_Max() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(2);
		command.setMaximumLength(1048576 * 1024); // a gig should always fail
		command.setFieldDisplayLength(0);

		validateAndExpect(command, 1);
	}

	@Test
	public void testEssay_Fail_FieldDisplayLength() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.ESSAY);
		command.setNumRows(2);
		command.setMaximumLength(35);
		command.setFieldDisplayLength(59); // must be zero

		validateAndExpect(command, 1);
	}

	// ======================================================================
	// RADIO type validation
	// ======================================================================

	@Test
	public void testRadio_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.RADIO);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>(10);
		for (int i = 0; i < 10; i++) {
			ChoiceCommand cmd = new ChoiceCommand();
			cmd.setMap(createNameMap());
			choiceList.add(cmd);
		}
		command.setChoiceList(choiceList);

		validateAndExpect(command, 0);
	}

	@Test
	public void testCheckbox_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>(10);
		for (int i = 0; i < 10; i++) {
			ChoiceCommand cmd = new ChoiceCommand();
			cmd.setMap(createNameMap());
			choiceList.add(cmd);
		}
		command.setChoiceList(choiceList);

		validateAndExpect(command, 0);
	}

	@Test
	public void testSelectList_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.SELECT);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>(10);
		for (int i = 0; i < 10; i++) {
			ChoiceCommand cmd = new ChoiceCommand();
			cmd.setMap(createNameMap());
			choiceList.add(cmd);
		}
		command.setChoiceList(choiceList);

		validateAndExpect(command, 0);
	}

	@Test
	public void testConstantSum_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CONSTANT_SUM);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>(10);
		for (int i = 0; i < 10; i++) {
			ChoiceCommand cmd = new ChoiceCommand();
			cmd.setMap(createNameMap());
			choiceList.add(cmd);
		}
		command.setChoiceList(choiceList);

		validateAndExpect(command, 0);
	}

	// ======================================================================
	// MULTIPLE-CHOICE COMMON
	// ======================================================================

	@Test
	public void testMultipleChoiceCommon_Fail_NullChoiceData() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		command.setChoiceList(null);

		try {
			validateAndExpect(command, 1);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// validator doesn't like null data
		}
	}

	@Test
	public void testMultipleChoiceCommon_Fail_EmptyCollectionOfOptions() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>();
		command.setChoiceList(choiceList);

		try {
			validateAndExpect(command, 1);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// validator doesn't like null data
		}
	}

	@Test
	public void testMultipleChoiceCommon_Fail_AllOptionEmpty() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>();
		Map<String, String> emptyMap = new HashMap<String, String>();
		emptyMap.put("eng", "");
		emptyMap.put("fra", "");
		choiceList.add(new ChoiceCommand(emptyMap));
		command.setChoiceList(choiceList);

		validateAndExpect(command, 1, new String[] { "createQuestion.error.atLeastOneChoiceRequired" });
	}

	@Test
	public void testMultipleChoiceCommon_Fail_OneOptionHalfEmpty() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);

		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>();

		// has value in one language, not both
		ChoiceCommand choiceCommand = new ChoiceCommand();
		choiceCommand.addName("eng", NAME_EN);
		choiceCommand.addName("fra", "");

		choiceList.add(choiceCommand);
		command.setChoiceList(choiceList);

		validateAndExpect(command, 1, new String[] { "createQuestion.error.titlesRequired" });
	}

	@Test
	public void testMultipleChoiceCommon_Fail_MissingOneName() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.CHECKBOX);
		// has value in one language, not both
		command.getMap().put("eng", "");

		List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>();

		ChoiceCommand choiceCommand = new ChoiceCommand();
		choiceCommand.addName("eng", NAME_EN);
		choiceCommand.addName("fra", NAME_FR);

		choiceList.add(choiceCommand);
		command.setChoiceList(choiceList);

		validateAndExpect(command, 1, new String[] { "createQuestion.error.titlesRequired" });
	}

	// ======================================================================
	// BOOLEAN type validation
	// ======================================================================
	@Test
	public void testBoolean_Success() {
		// valid data
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.BOOLEAN);

		validateAndExpect(command, 0);
	}

	// ======================================================================
	// SCALE type validation
	// ======================================================================

	@Test
	public void testScale_Success() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.setMinimum(1L);
		command.setMaximum(11L);

		validateAndExpect(command, 0);
	}

	@Test
	public void testScale_Fail_HugeMaximum() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.setMinimum(1L);
		command.setMaximum(1000L);

		validateAndExpect(command, 1);
	}

	@Test
	public void testScale_Fail_MaxMustBeGreaterThanMin() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.SCALE);

		// maximum must always be > minimum
		command.setMinimum(1L);
		command.setMaximum(1L);
		validateAndExpect(command, 1, new String[] { "scaleMaximumMustBeGreaterThanMinimum" });

		command.setMinimum(null);
		command.setMaximum(1L);
		validateAndExpect(command, 1);

		command.setMinimum(1L);
		command.setMaximum(null);
		validateAndExpect(command, 1);
	}

	@Test
	public void testScale_Fail_Nulls() {
		validateAndExpect(new CreateQuestionCommand(), 1);
	}

	/**
	 * Current design means that anything other than "1" for the minimum is
	 * invalid.
	 */
	@Test
	public void testScale_Fail_NonOneMinimum() {
		CreateQuestionCommand command = new CreateQuestionCommand(createNameMap());
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.setMinimum(3L);
		command.setMaximum(10L);

		validateAndExpect(command, 1);
	}
}

package com.oas.validator;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.model.util.QuestionTypeCode;
import com.oas.service.SupportedLanguageService;

/**
 * Validate a command into the CreateSurvey controller.
 * 
 * @author xhalliday
 * @since September 10, 2008
 */
@Component("createQuestionValidator")
public class CreateQuestionValidator implements Validator {

	/**
	 * 64K max size
	 * 
	 * TODO review
	 */
	private static final int MAX_DATA_LENGTH = 65536;

	/** Maximum size for HTML page payloads. */
	private static final int MAX_PAGE_TYPE_LENGTH = 16384;

	/** Highest Maximum Value for a Scale question. */
	private static final int HIGHEST_SCALE_MAXIMUM = 11;

	/** i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	/** Post-construct initializer. */
	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		Assert.notNull(supportedLanguageService, "supportedLanguageService required");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		// the CreateSurvey controller only accepts a list of IDs as input
		return clazz.isAssignableFrom(CreateQuestionCommand.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Assert.isAssignable(CreateQuestionCommand.class, target.getClass());
		CreateQuestionCommand command = (CreateQuestionCommand) target;

		if (!StringUtils.hasText(command.getTypeCode())) {
			// no type code: fail
			errors.reject("illegalArgument");
			return;
		}

		// all types require names set
		validateNames(command, errors);

		// do type-specific validation
		if (QuestionTypeCode.BOOLEAN.equals(command.getTypeCode())) {
			validateBoolean(command, errors);
		} else if (QuestionTypeCode.TEXT.equals(command.getTypeCode())) {
			validateText(command, errors);
		} else if (QuestionTypeCode.ESSAY.equals(command.getTypeCode())) {
			validateEssay(command, errors);
		} else if (QuestionTypeCode.RADIO.equals(command.getTypeCode())) {
			validateRadio(command, errors);
		} else if (QuestionTypeCode.CHECKBOX.equals(command.getTypeCode())) {
			validateCheckbox(command, errors);
		} else if (QuestionTypeCode.SELECT.equals(command.getTypeCode())) {
			validateSelect(command, errors);
		} else if (QuestionTypeCode.CONSTANT_SUM.equals(command.getTypeCode())) {
			validateConstantSum(command, errors);
		} else if (QuestionTypeCode.SCALE.equals(command.getTypeCode())) {
			validateScale(command, errors);
		} else if (QuestionTypeCode.PAGE.equals(command.getTypeCode())) {
			validatePage(command, errors);
		} else {
			errors.rejectValue("typeCode", "createQuestion.error.selectTypeRequired");
		}
	}

	protected void validatePage(CreateQuestionCommand command, Errors errors) {

		// content for all languages title must not be empty
		Map<String, String> map = command.getPageContent().getMap();
		for (String key : map.keySet()) {
			if (!StringUtils.hasText(map.get(key))) {
				errors.rejectValue("m[" + key + "]", "createQuestion.error.pageContentRequired");

				// only show this error once
				break;
			} else {
				// has text
				if (map.get(key).length() > NameObjectCommandValidator.MAXIMUM_NAME_VALUE_LENGTH) {
					errors.rejectValue("map[" + key + "]", "error.nameObject.tooLong", new Object[] {
							supportedLanguageService.findByCode(key).getDisplayTitle(), MAX_PAGE_TYPE_LENGTH }, null);
				}
			}
		}
	}

	/**
	 * Validate the Question's names, one per supported language is required
	 * (based on the map's keyset).
	 * 
	 * @param command
	 * @param errors
	 */
	protected void validateNames(CreateQuestionCommand command, Errors errors) {

		// each title must not be empty
		Map<String, String> map = command.getMap();
		for (String key : map.keySet()) {
			if (!StringUtils.hasText(map.get(key))) {
				errors.rejectValue("m[" + key + "]", "createQuestion.error.titlesRequired");

				// only show this error once
				break;
			} else {
				// has text
				if (map.get(key).length() > NameObjectCommandValidator.MAXIMUM_NAME_VALUE_LENGTH) {
					errors.rejectValue("map[" + key + "]", "error.nameObject.tooLong", new Object[] {
							supportedLanguageService.findByCode(key).getDisplayTitle(),
							NameObjectCommandValidator.MAXIMUM_NAME_VALUE_LENGTH }, null);
				}
			}
		}
	}

	protected void validateBoolean(CreateQuestionCommand command, Errors errors) {
		Assert.notNull(command);
		Assert.notNull(errors);
	}

	protected void validateText(CreateQuestionCommand command, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "fieldDisplayLength", "fieldDisplayLengthRequired");
		ValidationUtils.rejectIfEmpty(errors, "maximumLength", "maximumLengthRequired");

		if (command.getFieldDisplayLength() < 1) {
			errors.rejectValue("fieldDisplayLength", "createQuestion.error.mustBeAtLeast1");
		}
		// TODO review: 100?
		if (command.getFieldDisplayLength() > 100) {
			errors.rejectValue("fieldDisplayLength", "createQuestion.error.numberTooHigh");
		}
		if (command.getMaximumLength() < 1) {
			errors.rejectValue("maximumLength", "createQuestion.error.mustBeAtLeast1");
		}
		if (command.getMaximumLength() > MAX_DATA_LENGTH) {
			// TODO better value, parameterized etc
			errors.rejectValue("maximumLength", "createQuestion.error.numberTooHigh");
		}
		if (command.getNumRows() != 1) {
			// invalid data
			errors.reject("illegalArgumentException");
		}
	}

	protected void validateEssay(CreateQuestionCommand command, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "numRows", "numRowsRequired");
		ValidationUtils.rejectIfEmpty(errors, "maximumLength", "maximumLengthRequired");

		if (command.getNumRows() < 2) {
			errors.rejectValue("numRows", "createQuestion.error.mustBeAtLeast2");
		}
		// TODO review: 100?
		if (command.getNumRows() > 100) {
			errors.rejectValue("numRows", "createQuestion.error.numberTooHigh");
		}
		if (command.getMaximumLength() < 1) {
			errors.rejectValue("maximumLength", "createQuestion.error.mustBeAtLeast1");
		}
		if (command.getMaximumLength() > MAX_DATA_LENGTH) {
			// TODO better value, parameterized etc
			errors.rejectValue("maximumLength", "createQuestion.error.numberTooHigh");
		}
		if (command.getFieldDisplayLength() != 0) {
			// invalid data
			errors.reject("illegalArgumentException");
		}

	}

	/**
	 * Common validations for the choiceList part of a CreateQuestionCommand.
	 * 
	 * @param command
	 * @param errors
	 */
	public void validateMultipleChoiceCommon(CreateQuestionCommand command, Errors errors) {

		// must have at least one question
		// caller cannot send us null data
		Assert.notNull(command);
		Assert.notNull(command.getChoiceList());
		Assert.notEmpty(command.getChoiceList());

		// if true, an error is shown once
		boolean anyChoicesMissingLanguageText = false;

		List<ChoiceCommand> list = command.getChoiceList();

		int i = 0;
		int choiceCount = 0;
		for (ChoiceCommand choiceCommand : list) {
			// String field = "choiceList[" + i + "]";
			if (choiceCommand.getMap() == null || choiceCommand.getMap().size() == 0) {
				// should never be possible
				// TODO disable and see if CheckboxCreateQuestionControllerTest
				// fails
				continue;
			}

			// ignore choice commands that have entirely empty data
			boolean allEmpty = true;
			for (String language : choiceCommand.getMap().keySet()) {
				String value = choiceCommand.getMap().get(language);
				if (StringUtils.hasText(value)) {
					allEmpty = false;
				}
			}

			// when one is specified, they all must be
			if (!allEmpty) {

				// increment for later validation
				choiceCount++;

				// validate text
				for (String language : choiceCommand.getMap().keySet()) {
					// String value = choiceCommand.getMap().get(language);
					if (!StringUtils.hasText(choiceCommand.getMap().get(language))) {
						// errors.rejectValue(field + ".map[" + language + "]",
						// "createQuestion.error.titlesRequired");
						anyChoicesMissingLanguageText = true;
						// only show once
						break;
					}

				}
			}

			i++;
		}

		if (anyChoicesMissingLanguageText) {
			errors.reject("createQuestion.error.titlesRequired");
		}

		if (choiceCount == 0) {
			errors.rejectValue("choiceList[0]", "createQuestion.error.atLeastOneChoiceRequired");
		}
	}

	protected void validateRadio(CreateQuestionCommand command, Errors errors) {
		validateMultipleChoiceCommon(command, errors);
	}

	protected void validateCheckbox(CreateQuestionCommand command, Errors errors) {
		validateMultipleChoiceCommon(command, errors);
	}

	protected void validateSelect(CreateQuestionCommand command, Errors errors) {
		validateMultipleChoiceCommon(command, errors);
	}

	protected void validateConstantSum(CreateQuestionCommand command, Errors errors) {
		validateMultipleChoiceCommon(command, errors);
	}

	/**
	 * Validate Scale questions.
	 */
	protected void validateScale(CreateQuestionCommand command, Errors errors) {

		Assert.notNull(errors);
		Assert.notNull(command);

		// enforce the minimum-is-always-1 rule
		if (command.getMinimum() == null || command.getMinimum() != 1) {
			// UI does not currently allow setting this value, so it's an
			// illegal argument

			errors.rejectValue("minimum", "illegalArgument");
			return;
		}

		if (command.getMaximum() == null) {
			//
			errors.rejectValue("maximum", "createQuestion.error.mustBeAtLeast1", "Maximum is required");
		} else if (command.getMaximum() > HIGHEST_SCALE_MAXIMUM) {
			errors.rejectValue("maximum", "scaleOverHighestValue", new String[] { Integer.toString(HIGHEST_SCALE_MAXIMUM) },
					"Scale must be < " + HIGHEST_SCALE_MAXIMUM);
		} else {
			// some numerically valid maximum
			if (command.getMaximum() <= command.getMinimum()) {
				errors
						.rejectValue("maximum", "scaleMaximumMustBeGreaterThanMinimum",
								"Maximum must be greater than the minimum.");
			}
		}
	}
}

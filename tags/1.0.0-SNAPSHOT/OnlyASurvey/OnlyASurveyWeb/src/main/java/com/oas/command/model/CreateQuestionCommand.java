package com.oas.command.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.oas.model.SupportedLanguage;
import com.oas.util.QuestionTypeCode;

/**
 * TODO get rid of numRows' default value
 * 
 * @author xhalliday
 * 
 */
public class CreateQuestionCommand extends NameObjectCommand {

	private String typeCode;
	private int fieldDisplayLength;
	private int maximumLength;
	private boolean required;
	private boolean allowOtherText;

	private int numRows = 1; // default value is important here
	private Long minimum;
	private Long maximum;

	private List<ChoiceCommand> choiceList = new ArrayList<ChoiceCommand>(10);

	// ======================================================================

	public CreateQuestionCommand() {
		super();
	}

	public CreateQuestionCommand(Collection<SupportedLanguage> supportedLanguages) {
		super(supportedLanguages);
	}

	public CreateQuestionCommand(Map<String, String> map) {
		super(map);
	}

	// ======================================================================

	public boolean isBooleanType() {
		return QuestionTypeCode.BOOLEAN.equals(getTypeCode());
	}

	public boolean isTextType() {
		return QuestionTypeCode.TEXT.equals(getTypeCode());
	}

	public boolean isEssayType() {
		return QuestionTypeCode.ESSAY.equals(getTypeCode());
	}

	public boolean isRadioType() {
		return QuestionTypeCode.RADIO.equals(getTypeCode());
	}

	public boolean isSelectType() {
		return QuestionTypeCode.SELECT.equals(getTypeCode());
	}

	public boolean isCheckboxType() {
		return QuestionTypeCode.CHECKBOX.equals(getTypeCode());
	}

	public boolean isScaleType() {
		return QuestionTypeCode.SCALE.equals(getTypeCode());
	}

	public boolean isConstantSumType() {
		return QuestionTypeCode.CONSTANT_SUM.equals(getTypeCode());
	}

	// public boolean hasValidTypeCode() {
	// return isBoolean() || isChoice() || isText();
	// }

	// ======================================================================

	/**
	 * @return the fieldDisplayLength
	 */
	public int getFieldDisplayLength() {
		return fieldDisplayLength;
	}

	/**
	 * @param fieldDisplayLength
	 *            the fieldDisplayLength to set
	 */
	public void setFieldDisplayLength(int fieldDisplayLength) {
		this.fieldDisplayLength = fieldDisplayLength;
	}

	/**
	 * @return the maximumLength
	 */
	public int getMaximumLength() {
		return maximumLength;
	}

	/**
	 * @param maximumLength
	 *            the maximumLength to set
	 */
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return the numRows
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * @param numRows
	 *            the numRows to set
	 */
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	/**
	 * @return the choiceList
	 */
	public final List<ChoiceCommand> getChoiceList() {
		return choiceList;
	}

	/**
	 * @param choiceList
	 *            the choiceList to set
	 */
	public final void setChoiceList(List<ChoiceCommand> choiceList) {
		this.choiceList = choiceList;
	}

	/**
	 * @return the typeCode
	 */
	public final String getTypeCode() {
		return typeCode;
	}

	/**
	 * @param typeCode
	 *            the typeCode to set
	 */
	public final void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	/**
	 * @return the allowOtherText
	 */
	public boolean isAllowOtherText() {
		return allowOtherText;
	}

	/**
	 * @param allowOtherText
	 *            the allowOtherText to set
	 */
	public void setAllowOtherText(boolean allowOtherText) {
		this.allowOtherText = allowOtherText;
	}

	/**
	 * @return the minimum
	 */
	public Long getMinimum() {
		return minimum;
	}

	/**
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(Long minimum) {
		this.minimum = minimum;
	}

	/**
	 * @return the maximum
	 */
	public Long getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum
	 *            the maximum to set
	 */
	public void setMaximum(Long maximum) {
		this.maximum = maximum;
	}

}

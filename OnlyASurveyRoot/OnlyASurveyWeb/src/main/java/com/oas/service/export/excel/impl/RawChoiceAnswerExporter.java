package com.oas.service.export.excel.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.model.Choice;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.report.raw.RawChoiceRow;
import com.oas.model.report.raw.RawChoiceRowId;
import com.oas.security.SecurityAssertions;

/**
 * Exports {@link ChoiceAnswer}'s into a Raw Excel Export.
 * 
 * @author xhalliday
 * @since September 26, 2009
 */
@Service
public class RawChoiceAnswerExporter extends AbstractAnswerExporter {

	@Override
	@ValidUser
	@Transactional
	public void exportRawData(Survey survey, Date startDate, Date endDate, HSSFWorkbook workbook,
			Map<Long, Integer> responseToCellMap, Map<Long, Integer> questionToRowMap) {

		//
		Assert.notNull(survey, "null survey");
		Assert.notNull(workbook, "null workbook");
		SecurityAssertions.assertOwnership(survey);

		HSSFSheet sheet = workbook.getSheetAt(0);

		//
		List<RawChoiceRow> list = getRawAnswerRows(RawChoiceRow.class, survey, startDate, endDate);

		for (RawChoiceRow item : list) {
			//
			RawChoiceRowId id = item.getId();
			Long questionId = id.getQuestionId();

			Integer rowIndex = getOrCreateRowIndex(sheet, responseToCellMap, item);
			Assert.notNull(rowIndex, "no row index for item");

			Integer cellIndex = questionToRowMap.get(questionId);
			Assert.notNull(cellIndex, "no cell index for question #" + questionId);

			HSSFRow row = sheet.getRow(rowIndex);
			Assert.notNull(row, "no row found at row index");

			// get or create a new cell for this value
			HSSFCell cell = getOrCreateValueCell(row, cellIndex, HSSFCell.CELL_TYPE_STRING);

			//
			cell.setCellValue(getValueCellText(item, cell));
		}
	}

	/**
	 * Get the contents of the cell that shows the value for this item.
	 * 
	 * @param item
	 * @param cell
	 * @return
	 */
	private HSSFRichTextString getValueCellText(RawChoiceRow item, HSSFCell cell) {

		// load the associated choice
		String choiceTitle = "Error";
		Choice choice = item.getId().getChoice();
		// Assert.notNull(choice, "specified choice does not exist");
		choiceTitle = choice.getDisplayTitle();

		String content = (choice.getDisplayOrder() + 1) + ". " + choiceTitle;
		if (item.getSumValue() != null) {
			content += " (" + item.getSumValue() + ")";
		}

		HSSFRichTextString retval = null;
		HSSFRichTextString oldValue = cell.getRichStringCellValue();

		if (oldValue == null) {
			retval = new HSSFRichTextString(content);
		} else {
			// existing content
			String oldContent = oldValue.getString();
			retval = new HSSFRichTextString(oldContent + "\n" + content);
		}

		// 
		return retval;
	}
}

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
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.report.raw.RawGenericRowId;
import com.oas.model.report.raw.RawScaleRow;
import com.oas.security.SecurityAssertions;

/**
 * Exports {@link TextAnswer}'s into a Raw Excel Export.
 * 
 * @author xhalliday
 * @since September 25, 2009
 */
@Service
public class RawScaleAnswerExporter extends AbstractAnswerExporter {

	@Override
	@ValidUser
	public void exportRawData(Survey survey, Date startDate, Date endDate, HSSFWorkbook workbook,
			Map<Long, Integer> responseToCellMap, Map<Long, Integer> questionToRowMap) {

		//
		Assert.notNull(survey, "null survey");
		Assert.notNull(workbook, "null workbook");
		SecurityAssertions.assertOwnership(survey);

		HSSFSheet sheet = workbook.getSheetAt(0);
		// HSSFRow responseIdRow = getResponseIdRow(sheet);

		List<RawScaleRow> list = getRawAnswerRows(RawScaleRow.class, survey, startDate, endDate);

		for (RawScaleRow item : list) {
			//
			RawGenericRowId id = item.getId();
			Long questionId = id.getQuestionId();

			Integer rowIndex = getOrCreateRowIndex(sheet, responseToCellMap, item);
			Assert.notNull(rowIndex, "no row index for item");

			Integer cellIndex = questionToRowMap.get(questionId);
			Assert.notNull(cellIndex, "no cell index for question #" + questionId);

			HSSFRow row = sheet.getRow(rowIndex);
			Assert.notNull(row, "no row found at row index");

			// get or create a new cell for this value
			HSSFCell cell = getOrCreateValueCell(row, cellIndex, HSSFCell.CELL_TYPE_NUMERIC);

			//
			cell.setCellValue(new HSSFRichTextString(item.getAnswerValue().toString()));
		}
	}
}

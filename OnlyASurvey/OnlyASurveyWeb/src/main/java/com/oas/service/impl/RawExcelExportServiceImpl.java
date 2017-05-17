package com.oas.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.RawExcelExportService;
import com.oas.service.ReportingService;
import com.oas.service.export.excel.RawExcelExporter;

/**
 * {@link ReportingService}.
 * 
 * @author Jason Halliday
 * @since September 6, 2008
 * @see ReportingService
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RawExcelExportServiceImpl extends AbstractServiceImpl implements RawExcelExportService {

	/** Components that participate in exporting answer data. */
	@Autowired
	private List<RawExcelExporter> rawAnswerExporterList;

	/** i18n. */
	@Autowired
	private MessageSource messageSource;

	/** {@inheritDoc} */
	@Override
	@ValidUser
	public HSSFWorkbook rawExport(Survey survey, Date startDate, Date endDate) {

		SecurityAssertions.assertOwnership(survey);

		HSSFWorkbook retval = new HSSFWorkbook();

		// maps Response IDs to their cell index; the first time a response is
		// added to the workbook it's id is added to the end
		Map<Long, Integer> responseToCellMap = new HashMap<Long, Integer>();

		// maps Question IDs to their row index
		Map<Long, Integer> questionToRowMap = new HashMap<Long, Integer>();

		// add header row
		initializeRawExportWorksheet(retval);

		// add in the questions
		addRawQuestionCells(survey, retval, questionToRowMap);

		for (RawExcelExporter exporter : rawAnswerExporterList) {

			// delegate
			exporter.exportRawData(survey, startDate, endDate, retval, responseToCellMap, questionToRowMap);
		}

		//
		return retval;
	}

	private void initializeRawExportWorksheet(HSSFWorkbook workbook) {

		Locale locale = getCurrentLocale();

		// excel works in 1/256th of a character width
		int cell0width = 256 * 25;

		HSSFSheet sheet = workbook.createSheet("1");
		sheet.setColumnWidth(0, cell0width);

		// response ID
		sheet.createRow(0).createCell(0).setCellValue(
				new HSSFRichTextString(messageSource.getMessage("report.rawExcel.rowHeader.responseId", null, locale)));

		// date created
		sheet.createRow(1).createCell(0).setCellValue(
				new HSSFRichTextString(messageSource.getMessage("report.rawExcel.rowHeader.created", null, locale)));

		// date closed
		sheet.createRow(2).createCell(0).setCellValue(
				new HSSFRichTextString(messageSource.getMessage("report.rawExcel.rowHeader.completed", null, locale)));
	}

	/**
	 * Add {@link Question}s from the Survey to the workbook, one per row in the
	 * first cell.
	 * 
	 * @param survey
	 * @param workbook
	 * @param questionToRowMap
	 */
	private void addRawQuestionCells(Survey survey, HSSFWorkbook workbook, Map<Long, Integer> questionToRowMap) {
		Assert.notNull(survey);
		Assert.notNull(workbook);
		Assert.notNull(questionToRowMap);

		// start at 4th row, one for the response ID, two for start/close dates
		int rowNum = 3;
		HSSFSheet sheet = workbook.getSheetAt(0);
		Assert.notNull(sheet, "no first sheet");

		for (Question question : survey.getQuestions()) {
			HSSFRow row = sheet.getRow(rowNum);
			if (row == null) {
				// lazily create: this should typically be the case
				row = sheet.createRow(rowNum);
			}

			String textContent = question.getDisplayTitle();
			if (!StringUtils.hasText(textContent)) {
				textContent = "#" + rowNum;
			}
			HSSFRichTextString text = new HSSFRichTextString(textContent);
			text.applyFont(HSSFFont.BOLDWEIGHT_BOLD);

			HSSFCell cell = row.createCell(0);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(text);

			questionToRowMap.put(question.getId(), rowNum);

			rowNum++;
		}

	}

}

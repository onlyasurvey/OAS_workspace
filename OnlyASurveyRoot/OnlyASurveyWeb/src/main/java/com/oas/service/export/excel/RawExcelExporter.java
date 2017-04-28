package com.oas.service.export.excel;

import java.util.Date;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.oas.model.Survey;

/**
 * A component that is capable of exporting some subset of a {@link Survey}'s
 * response data into the Raw Excel Export format.
 * 
 * @author xhalliday
 * @since September 25, 2009
 */
public interface RawExcelExporter {

	/**
	 * Export raw data into an existing workbook.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @param startDate
	 *            (Optional) If not null then results are restricted by start
	 *            date
	 * @param endDate
	 *            (Optional) If not null then results are restricted by end date
	 * @param workbook
	 *            {@link HSSFWorkbook} to export data into
	 * @param responseToCellMap
	 *            Map of responseId's to cell index
	 * @param questionToRowMap
	 *            Map of questionId's to row index
	 */
	void exportRawData(Survey survey, Date startDate, Date endDate, HSSFWorkbook workbook, Map<Long, Integer> responseToCellMap,
			Map<Long, Integer> questionToRowMap);
}

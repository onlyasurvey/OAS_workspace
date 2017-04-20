package com.oas.service;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Survey;

/**
 * Service providing various data for reporting purposes.
 * 
 * @author xhalliday
 * @since September 27, 2008
 */
public interface RawExcelExportService extends AbstractServiceInterface {

	/**
	 * Exports the raw data of a {@link Survey}'s results into a Excel
	 * spreadsheet ({@link HSSFWorkbook}).
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @param startDate
	 *            If null, no start date is used.
	 * @param endDate
	 *            If null, no end date is used
	 * @return workbook Workbook with results export into; this avoids buffering
	 *         in memory.
	 * 
	 */
	public HSSFWorkbook rawExport(Survey survey, Date startDate, Date endDate);
}

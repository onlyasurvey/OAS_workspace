package com.oas.service.export.excel.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.util.Assert;

import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.report.raw.AbstractRawRow;
import com.oas.service.export.excel.RawExcelExporter;

/**
 * Exports {@link TextAnswer}'s into a Raw Excel Export.
 * 
 * @author xhalliday
 * @since September 25, 2009
 */
public abstract class AbstractAnswerExporter extends AbstractServiceImpl implements RawExcelExporter {

	/**
	 * Returns the {@link HSSFRow} that represents the line where response IDs
	 * show up.
	 * 
	 * @param sheet
	 *            {@link HSSFSheet}
	 * @return {@link HSSFRow}
	 */
	protected HSSFRow getResponseIdRow(HSSFSheet sheet) {
		return sheet.getRow(0);
	}

	/**
	 * Returns the {@link HSSFRow} that represents the line where start dates
	 * show up.
	 * 
	 * @param sheet
	 *            {@link HSSFSheet}
	 * @return {@link HSSFRow}
	 */
	protected HSSFRow getStartDateRow(HSSFSheet sheet) {
		return sheet.getRow(1);
	}

	/**
	 * Returns the {@link HSSFRow} that represents the line where close dates
	 * show up.
	 * 
	 * @param sheet
	 *            {@link HSSFSheet}
	 * @return {@link HSSFRow}
	 */
	protected HSSFRow getEndDateRow(HSSFSheet sheet) {
		return sheet.getRow(2);
	}

	/**
	 * Set date criterion, if applicable.
	 * 
	 * @param crit
	 *            {@link Criteria}
	 * @param startDate
	 *            Start {@link Date}, or null
	 * @param endDate
	 *            Start {@link Date}, or null
	 */
	protected void setDateCriteron(Criteria crit, Date startDate, Date endDate) {
		if (startDate != null) {
			crit.add(Restrictions.ge("dateClosed", startDate));
		}
		if (endDate != null) {
			crit.add(Restrictions.ge("dateClosed", endDate));
		}
	}

	/**
	 * Return an {@link HSSFCell} to put Choice content into, either a new one
	 * or an existing one in the case where a value is already at the calculated
	 * position (ie., when multiple ChoiceAnswers are present).
	 * 
	 * @param row
	 * @param cellIndex
	 * @param cellType
	 *            {@link HSSFCell}.CELL_TYPE_STRING or similar
	 * @return
	 */
	protected HSSFCell getOrCreateValueCell(HSSFRow row, Integer cellIndex, int cellType) {

		HSSFCell retval = row.getCell(cellIndex);
		if (retval == null) {
			// initialize the cell
			retval = row.createCell(cellIndex);
			retval.setCellType(cellType);
		} // else: appending to an existing cell

		return retval;
	}

	/**
	 * Get the cell index for a responseId, either returning an existing index
	 * or creating a cell and return it's new index.
	 * 
	 * @param idRow
	 * @param responseToCellMap
	 * @param responseId
	 * @return
	 */
	protected Integer getOrCreateCellIndex(HSSFRow idRow, HSSFRow startDateRow, HSSFRow endDateRow,
			Map<Long, Integer> responseToCellMap, AbstractRawRow data) {

		Assert.notNull(idRow);
		Assert.notNull(responseToCellMap);
		Long responseId = data.getId().getResponseId();
		Assert.notNull(responseId);

		// determine or add cell for the responseId
		Integer cellIndex = responseToCellMap.get(responseId);
		if (cellIndex == null) {

			// add a new cell for this response
			int lastIndex = idRow.getLastCellNum();
			int newIndex;
			if (lastIndex == -1) {
				// skip the 0th cell
				newIndex = 1;
			} else {
				newIndex = lastIndex;
			}

			cellIndex = Integer.valueOf(newIndex);
			responseToCellMap.put(responseId, cellIndex);

			//
			addResponseIdHeaderCell(idRow, responseId, cellIndex);
			//
			addDateCells(startDateRow, endDateRow, cellIndex, data);
		}

		return cellIndex;
	}

	/**
	 * Add a header row for the responseId.
	 * 
	 * @param row
	 * @param responseId
	 * @param cellIndex
	 */
	private void addResponseIdHeaderCell(HSSFRow row, Long responseId, int cellIndex) {
		//
		HSSFRichTextString text = new HSSFRichTextString(responseId.toString());
		text.applyFont(HSSFFont.BOLDWEIGHT_BOLD);

		HSSFCell cell = row.createCell(cellIndex);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(text);
	}

	/**
	 * The first time a responseId is encountered Start and Closed date header
	 * cells are created.
	 * 
	 * @param startDateRow
	 *            {@link HSSFRow}
	 * @param endDateRow
	 *            {@link HSSFRow}
	 * @param cellIndex
	 *            Cell index in the header columns to add the dates
	 * @param data
	 *            {@link AbstractRawRow} data for the cell
	 */
	private void addDateCells(HSSFRow startDateRow, HSSFRow endDateRow, int cellIndex, AbstractRawRow data) {
		HSSFCell startCell = startDateRow.createCell(cellIndex);
		HSSFCell endCell = endDateRow.createCell(cellIndex);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm", getCurrentLocale());

		startCell.setCellValue(new HSSFRichTextString(dateFormat.format(data.getDateCreated())));
		endCell.setCellValue(new HSSFRichTextString(dateFormat.format(data.getDateClosed())));
	}

	/**
	 * Get a list of {@link AbstractRawRow}-derived objects related to the
	 * specified {@link Survey} and date range.
	 * 
	 * @param clazz
	 *            Class of the return type
	 * @param survey
	 *            Target {@link Survey}
	 * @param startDate
	 *            Start {@link Date}, or null
	 * @param endDate
	 *            Start {@link Date}, or null
	 * @return
	 */
	protected <C> List<C> getRawAnswerRows(Class<C> clazz, Survey survey, Date startDate, Date endDate) {

		//
		Criteria crit = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(clazz);
		crit.add(Restrictions.eq("id.survey", survey));

		// restrict by dates
		setDateCriteron(crit, startDate, endDate);

		// execute query
		@SuppressWarnings("unchecked")
		List<C> retval = crit.list();

		return retval;
	}

}

package com.oas.service;

import static junit.framework.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;

public class RawExcelExportServiceTest extends AbstractOASBaseTest {

	@Autowired
	private RawExcelExportService service;

	private Survey testData;

	private Date firstDate;
	private Date lastDate;

	private int numResponses;

	@Before
	public void setupData() {
		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal1.add(Calendar.MONTH, 0);

		// 1 month ago
		Calendar cal2 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal2.add(Calendar.MONTH, -1);

		// 2 months ago
		Calendar cal3 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal3.add(Calendar.MONTH, -2);

		// 3 months ago
		Calendar cal4 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal4.add(Calendar.MONTH, -3);

		//
		testData = scenarioDataUtil.createMonthlyReportTestSurvey(createAndSetSecureUserWithRoleUser(), //
				cal1, cal2, cal3, cal4);
		flushAndClear();

		numResponses = ((Long) unique(find("select count(r) from Response r where r.survey = ?", testData))).intValue();

		//
		firstDate = cal1.getTime();
		lastDate = cal4.getTime();
	}

	@Test
	public void rawExport_NoDates_FullExport_Success() {

		doExportAndTest(testData, null, null);
	}

	@Test
	public void rawExport_StartAndEndDates_Success() {

		doExportAndTest(testData, firstDate, lastDate);
	}

	@Test
	public void rawExport_StartDateSuccess() {

		doExportAndTest(testData, firstDate, null);
	}

	@Test
	public void rawExport_EndDate_Success() {

		doExportAndTest(testData, null, lastDate);
	}

	private void doExportAndTest(Survey testData, Date startDate, Date endDate) {
		//
		// method under test
		//
		HSSFWorkbook workbook = service.rawExport(testData, startDate, endDate);

		// if (false) {
		// try {
		// //
		// FileOutputStream os = new FileOutputStream(new File("\\raw.xls"));
		// workbook.write(os);
		// os.close();
		//
		// // now inspect the workbook
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }
		// }

		doRawExcelAsserts(testData, workbook);
	}

	private void doRawExcelAsserts(Survey survey, HSSFWorkbook workbook) {

		Assert.notNull(survey);
		Assert.notNull(workbook);

		// raw exports use 1 sheet
		HSSFSheet sheet = workbook.getSheetAt(0);

		int expectedRows = numResponses + 1; // to account for header orw

		assertEquals("unexpected # rows", expectedRows, sheet.getPhysicalNumberOfRows());

		// questions should be enumerated in the first row
		// first 3 cells are id, start, end
		int cellNum = 3;
		for (Question question : survey.getQuestions()) {

			HSSFRow row = sheet.getRow(0);

			HSSFCell cell = row.getCell(cellNum);
			assertEquals("unexpected cell content", question.getDisplayTitle(), cell.getRichStringCellValue().getString());

			cellNum++;
		}
	}

	// ======================================================================

}

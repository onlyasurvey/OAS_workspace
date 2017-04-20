package com.oas.controller.dashboard.report.export;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.oas.AbstractOASBaseTest;
import com.oas.controller.dashboard.export.excel.BreakdownExcelController;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;

/**
 * Tests for the Export to Excel feature.
 * 
 * TODO these are not extensive: the controller needs more and deeper tests
 * 
 * @author xhalliday
 * @since December 4, 2008
 */
public class ExcelControllerTest extends AbstractOASBaseTest {

	@Autowired
	private BreakdownExcelController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	// ======================================================================

	// private class SomeInvalidQuestionType extends Question {
	// }

	// ======================================================================

	/**
	 * Basic success path test. Validates the two header titles to ensure valid
	 * XLS data is being exported but does not do deep validation of the
	 * results.
	 * 
	 * TODO deep inspection
	 */
	@Test
	public void testDailyReport_Success() throws IOException {

		// test data can be used for any time period
		Survey survey = scenarioDataUtil.createMonthlyReportTestSurvey(createAndSetSecureUserWithRoleUser());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".xls");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		// invoke for daily
		controller.dailyBreakdownReportHttpServletRequest(request, response, output);

		// load it using POI
		ByteArrayInputStream excelInputStream = new ByteArrayInputStream(output.toByteArray());
		HSSFWorkbook wb = new HSSFWorkbook(excelInputStream);
		assertNotNull("unable to load workbook", wb);

		// should have 3 sheets - one per question
		HSSFSheet sheet1 = wb.getSheetAt(0);
		HSSFSheet sheet2 = wb.getSheetAt(1);
		HSSFSheet sheet3 = wb.getSheetAt(2);

		assertNotNull("sheet missing", sheet1);
		assertNotNull("sheet missing", sheet2);
		assertNotNull("sheet missing", sheet3);

		assertEquals("unexpected sheet header #1", survey.getQuestions().get(0).getDisplayTitle(), sheet1.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
		assertEquals("unexpected sheet header #2", survey.getQuestions().get(1).getDisplayTitle(), sheet2.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
		assertEquals("unexpected sheet header #2", survey.getQuestions().get(2).getDisplayTitle(), sheet3.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
	}

	/**
	 * Basic success path test. Validates the two header titles to ensure valid
	 * XLS data is being exported but does not do deep validation of the
	 * results.
	 * 
	 * TODO deep inspection
	 */
	@Test
	public void testMonthlyReport_Success() throws IOException {

		// test data can be used for any time period
		Survey survey = scenarioDataUtil.createMonthlyReportTestSurvey(createAndSetSecureUserWithRoleUser());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".xls");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		// invoke for monthly
		controller.monthlyBreakdownReportHttpServletRequest(request, response, output);

		// load it using POI
		ByteArrayInputStream excelInputStream = new ByteArrayInputStream(output.toByteArray());
		HSSFWorkbook wb = new HSSFWorkbook(excelInputStream);
		assertNotNull("unable to load workbook", wb);

		// should have 3 sheets - one per question
		HSSFSheet sheet1 = wb.getSheetAt(0);
		HSSFSheet sheet2 = wb.getSheetAt(1);
		HSSFSheet sheet3 = wb.getSheetAt(2);

		assertNotNull("sheet missing", sheet1);
		assertNotNull("sheet missing", sheet2);
		assertNotNull("sheet missing", sheet3);

		assertEquals("unexpected sheet header #1", survey.getQuestions().get(0).getDisplayTitle(), sheet1.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
		assertEquals("unexpected sheet header #2", survey.getQuestions().get(1).getDisplayTitle(), sheet2.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
		assertEquals("unexpected sheet header #2", survey.getQuestions().get(2).getDisplayTitle(), sheet3.getRow(0).getCell(0)
				.getRichStringCellValue().getString());
	}

	// ======================================================================

	/**
	 * No parameters are optional: should always fail.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDailyReport_Fail_NullInputs() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OutputStream output = new ByteArrayOutputStream();

		doTestDailyReport_FailOnNullInput(null, response, output);
		doTestDailyReport_FailOnNullInput(request, null, output);
		doTestDailyReport_FailOnNullInput(request, response, null);
	}

	/**
	 * No parameters are optional: should always fail.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMonthlyReport_Fail_NullInputs() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		OutputStream output = new ByteArrayOutputStream();

		doTestDailyReport_FailOnNullInput(null, response, output);
		doTestDailyReport_FailOnNullInput(request, null, output);
		doTestDailyReport_FailOnNullInput(request, response, null);
	}

	private void doTestDailyReport_FailOnNullInput(HttpServletRequest request, HttpServletResponse response, OutputStream output)
			throws IOException {
		try {
			controller.dailyBreakdownReportHttpServletRequest(request, response, output);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================
}

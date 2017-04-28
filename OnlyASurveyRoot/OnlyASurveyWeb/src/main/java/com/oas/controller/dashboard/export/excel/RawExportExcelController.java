package com.oas.controller.dashboard.export.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import com.oas.model.Survey;
import com.oas.service.RawExcelExportService;

/**
 * Extract survey data to Excel format.
 * 
 * TODO move exporting functionality to a service.
 * 
 * @author xhalliday
 * @since November 24, 2008
 */
@Controller
public class RawExportExcelController extends AbstractExcelController {

	/**
	 * Service for getting report data.
	 */
	@Autowired
	private RawExcelExportService rawExcelExportService;

	/**
	 * i18n.
	 */
	// @Autowired
	// private MessageSource messageSource;

	/**
	 * Export a Daily Breakdown Report to Excel (XLS).
	 * 
	 * @param request
	 * @param response
	 * @param output
	 * @throws IOException
	 */
	@RequestMapping(value = "/db/ex/xls/rw/*.xls")
	public void rawExport(HttpServletRequest request, HttpServletResponse response, OutputStream output) throws IOException {

		Assert.noNullElements(new Object[] { request, response, output });

		// load survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey, "no survey data");

		// delegate
		exportRawDataToExcel(survey, request, response, output, "oas-rw-" + survey.getId() + ".xls");
	}

	/**
	 * Export to Excel (XLS) format.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param response
	 *            {@link HttpServletResponse}
	 * @param output
	 *            OutputStream for binary data
	 * @param filename
	 *            For content-disposition
	 * 
	 * @throws IOException
	 */
	private void exportRawDataToExcel(Survey survey, HttpServletRequest request, HttpServletResponse response,
			OutputStream output, String filename) throws IOException {

		// null means no range
		Date startDate = null;
		Date endDate = null;

		// delegate
		HSSFWorkbook workbook = rawExcelExportService.rawExport(survey, startDate, endDate);

		// all good
		response.setHeader("Content-Disposition", "file; name=" + filename);
		response.setContentType("application/vnd.ms-excel");
		workbook.write(output);
	}
}

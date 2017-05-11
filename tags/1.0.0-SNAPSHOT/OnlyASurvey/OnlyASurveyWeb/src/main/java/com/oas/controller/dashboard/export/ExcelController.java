package com.oas.controller.dashboard.export;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.oas.controller.AbstractOASController;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.report.calendar.CalendarReport;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;
import com.oas.service.ReportingService;

/**
 * Extract survey data to Excel format.
 * 
 * TODO move exporting functionality to a service.
 * 
 * @author xhalliday
 * @since November 24, 2008
 */
@Controller
public class ExcelController extends AbstractOASController {

	/**
	 * Service for getting report data.
	 */
	@Autowired
	private ReportingService reportingService;

	/**
	 * i18n.
	 */
	@Autowired
	private MessageSource messageSource;

	/**
	 * Export a Daily Breakdown Report to Excel (XLS).
	 * 
	 * @param request
	 * @param response
	 * @param output
	 * @throws IOException
	 */
	@RequestMapping(value = "/db/ex/xls/dbd/*.xls")
	public void dailyBreakdownReportHttpServletRequest(HttpServletRequest request, HttpServletResponse response,
			OutputStream output) throws IOException {

		Assert.noNullElements(new Object[] { request, response, output });

		// load survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey, "no survey data");

		CalendarReport report = reportingService.getDailyReport(survey);
		Assert.notNull(report, "unable to load monthly data");

		// delegate rest
		exportToExcel(request, response, output, "oas-d-" + survey.getId() + ".xls", report);
	}

	/**
	 * Export a Monthly Breakdown Report to Excel (XLS).
	 * 
	 * @param request
	 * @param response
	 * @param output
	 * @throws IOException
	 */
	@RequestMapping(value = "/db/ex/xls/mbd/*.xls")
	public void monthlyBreakdownReportHttpServletRequest(HttpServletRequest request, HttpServletResponse response,
			OutputStream output) throws IOException {

		Assert.noNullElements(new Object[] { request, response, output });

		// load survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey, "no survey data");

		CalendarReport report = reportingService.getMonthlyReport(survey);
		Assert.notNull(report, "unable to load monthly data");

		// delegate rest
		exportToExcel(request, response, output, "oas-m-" + survey.getId() + ".xls", report);
	}

	/**
	 * Export to Excel (XLS) format.
	 * 
	 * @param request
	 * @param response
	 * @param output
	 * @param report
	 * 
	 * 
	 * @throws IOException
	 */
	protected void exportToExcel(HttpServletRequest request, HttpServletResponse response, OutputStream output, String filename,
			CalendarReport report) throws IOException {

		Survey survey = report.getSurvey();
		Assert.notNull(survey);

		Map<Question, Collection<TextAnswer>> textData = reportingService.getTextAnswers(survey);
		Assert.notNull(textData);

		response.setHeader("Content-Disposition", "file; name=" + filename);
		response.setContentType("application/vnd.ms-excel");

		HSSFWorkbook wb = new HSSFWorkbook();

		for (Question question : survey.getQuestions()) {

			// question result data
			if (question.isTextQuestion()) {

				addTextQuestion(wb, question, textData, false);

				// done with this iteration
				continue;
			}

			if (question.isChoiceQuestion()) {

				addChoiceQuestion(wb, question, report);

				// add new sheet if there is "other" data
				if (question.isAllowOtherText()) {
					Collection<TextAnswer> otherAnswers = textData.get(question);
					if (!CollectionUtils.isEmpty(otherAnswers)) {
						addTextQuestion(wb, question, textData, true);
					}
				}

				// done with this iteration
				continue;
			}

			if (question.isScaleQuestion()) {

				addScaleQuestion(wb, question, report);

				// add new sheet if there is "other" data
				if (question.isAllowOtherText()) {
					Collection<TextAnswer> otherAnswers = textData.get(question);
					if (!CollectionUtils.isEmpty(otherAnswers)) {
						addTextQuestion(wb, question, textData, true);
					}
				}

				// done with this iteration
				continue;
			}

			// nothing handled the type
			// log.error("unknown question type for id#" + question.getId());
			// throw new IllegalArgumentException("unknown question type");
			log.warn("unknown question type encountered while generating Excel data: " + question.getClass().getSimpleName());
		}

		wb.write(output);
	}

	/**
	 * Add a default question header to the export.
	 * 
	 * @param sheet
	 * @param question
	 * @param title
	 */
	private void addDefaultQuestionHeader(HSSFSheet sheet, Question question, String title) {
		// sheet header
		HSSFRow header = sheet.createRow(0);
		HSSFCell headerCell = header.createCell(0);

		HSSFRichTextString headerTitle = new HSSFRichTextString(title);
		headerTitle.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
		headerCell.setCellValue(headerTitle);
	}

	/**
	 * Add results of a text question to the workbook.
	 * 
	 * @param wb
	 * @param question
	 * @param textData
	 * @param otherAnswerMode
	 */
	private void addTextQuestion(HSSFWorkbook wb, Question question, Map<Question, Collection<TextAnswer>> textData,
			boolean otherAnswerMode) {

		Assert.notNull(wb);
		Assert.notNull(question);
		Assert.notNull(textData);

		// configure sheet for this question
		String worksheetTitle = getDefaultSheetName(question);
		String worksheetHeader = question.getDisplayTitle();
		if (otherAnswerMode) {
			String prefix = messageSource.getMessage("report.xsl.otherTextQuestionNamePrefix", null, "", LocaleContextHolder
					.getLocale());
			String otherSuffix = messageSource.getMessage("report.xsl.otherTextSheetSuffix", null, "", LocaleContextHolder
					.getLocale());

			worksheetTitle += " " + otherSuffix;
			worksheetHeader = prefix + " " + worksheetHeader;
		}

		HSSFSheet sheet = wb.createSheet(worksheetTitle);
		sheet.setColumnWidth(0, 12 * 256);

		addDefaultQuestionHeader(sheet, question, worksheetHeader);

		int rowNum = 2;

		if (textData.get(question) != null) {
			for (TextAnswer answer : textData.get(question)) {
				HSSFRow row = sheet.createRow(rowNum++);

				HSSFCell date = row.createCell(0);
				HSSFCell text = row.createCell(1);

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				date.setCellValue(new HSSFRichTextString(df.format(answer.getResponse().getCreated())));
				text.setCellValue(new HSSFRichTextString(answer.getValue()));
			}
		}
	}

	/**
	 * Add results of a multiple-choice question to the workbook.
	 * 
	 * @param wb
	 * @param question
	 * @param report
	 */
	private void addChoiceQuestion(HSSFWorkbook wb, Question question, CalendarReport report) {

		Assert.notNull(wb);
		Assert.notNull(question);
		Assert.notNull(report);

		// create the header for this question
		HSSFSheet sheet = createSheetWithMonthlyHeader(wb, getDefaultSheetName(question), question, report);

		int longestChoiceName = 10;
		int rowNum = 3;
		ChoiceQuestion cq = (ChoiceQuestion) question;
		for (Choice choice : cq.getChoices()) {

			int colNum = 1;
			HSSFRow row = sheet.createRow(rowNum++);

			// header - Choice Title
			{
				String choiceTitle = choice.getDisplayTitle();
				if (choiceTitle.length() > longestChoiceName) {
					longestChoiceName = choiceTitle.length();
				}
				HSSFCell header = row.createCell(0);
				HSSFRichTextString headerValue = new HSSFRichTextString(choiceTitle);
				headerValue.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
				header.setCellValue(headerValue);
			}

			// monthly breakdown
			Map<Date, ChoiceCalendarBreakdown> map = report.getChoiceAnswers().get(choice);
			Assert.notNull(map, "invalid report data: no map: " + choice.getDisplayTitle());

			for (Date date : report.getDateList()) {
				ChoiceCalendarBreakdown breakdown = map.get(date);
				Assert.notNull(breakdown, "invalid report data: no breakdown: id#" + choice.getId() + ", q#" + question.getId()
						+ " as: " + choice.getDisplayTitle() + ", date: " + date.toString());
				Long count = breakdown.getCount();

				HSSFCell cell = row.createCell(colNum++);
				cell.setCellValue(count);
			}
		}

		// set first column width to support longest choice name
		sheet.setColumnWidth(0, (longestChoiceName + 2) * 256);

		// if there is no date list then there is no response data: ignore
		// results
		if (!CollectionUtils.isEmpty(report.getDateList())) {
			// account for "Other" text answers
			if (question.isAllowOtherText()) {
				Map<Date, Long> otherAnswers = report.getTextAnswerCounts().get(question);
				Assert.isTrue(!CollectionUtils.isEmpty(otherAnswers), "expecting Other text answer data");

				int colNum = 1;

				String otherLabel = messageSource.getMessage("question.other", null, "", LocaleContextHolder.getLocale());
				HSSFRow row = sheet.createRow(rowNum++);
				HSSFCell header = row.createCell(0);
				HSSFRichTextString headerValue = new HSSFRichTextString(otherLabel);
				headerValue.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
				header.setCellValue(headerValue);

				for (Date date : report.getDateList()) {
					Long count = otherAnswers.get(date);
					Assert.notNull(count, "null count for Other text answers");

					HSSFCell cell = row.createCell(colNum++);
					cell.setCellValue(count);
				}
			}
		}
	}

	/**
	 * Add results of a scale question to the workbook.
	 * 
	 * @param wb
	 * @param question
	 * @param report
	 */
	private void addScaleQuestion(HSSFWorkbook wb, Question question, CalendarReport report) {
		Assert.notNull(wb);
		Assert.notNull(question);
		Assert.notNull(report);

		// configure sheet for this question
		HSSFSheet sheet = createSheetWithMonthlyHeader(wb, getDefaultSheetName(question), question, report);

		int rowNum = 3;
		ScaleQuestion cq = (ScaleQuestion) question;
		for (Long answerValue : cq.getPossibleValues()) {

			int colNum = 1;
			HSSFRow row = sheet.createRow(rowNum++);

			// header - Choice Title
			{
				HSSFCell header = row.createCell(0);
				HSSFRichTextString headerValue = new HSSFRichTextString(answerValue.toString());
				headerValue.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
				header.setCellValue(headerValue);
			}

			// monthly breakdown
			Map<Date, Map<Long, Long>> map = report.getScaleAnswerCounts().get(question);
			Assert.notNull(map, "invalid report data: no map: " + map);

			for (Date date : report.getDateList()) {
				//
				Map<Long, Long> breakdown = map.get(date);

				Long count = breakdown.get(answerValue);
				Assert.notNull(breakdown, "invalid report data: no breakdown: q#" + question.getId() + " as: " + answerValue
						+ ", date: " + date.toString());

				HSSFCell cell = row.createCell(colNum++);
				cell.setCellValue(count);
			}
		}

		// set first column width to support longest choice name
		sheet.setColumnWidth(0, 6 * 256);

		// account for "Other" text answers
		if (question.isAllowOtherText()) {
			Map<Date, Long> otherAnswers = report.getTextAnswerCounts().get(question);
			Assert.isTrue(!CollectionUtils.isEmpty(otherAnswers), "expecting Other text answer data");

			int colNum = 1;

			String otherLabel = messageSource.getMessage("question.other", null, "", LocaleContextHolder.getLocale());
			HSSFRow row = sheet.createRow(rowNum++);
			HSSFCell header = row.createCell(0);
			HSSFRichTextString headerValue = new HSSFRichTextString(otherLabel);
			headerValue.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
			header.setCellValue(headerValue);

			for (Date date : report.getDateList()) {
				Long count = otherAnswers.get(date);
				Assert.notNull(count, "null count for Other text answers");

				HSSFCell cell = row.createCell(colNum++);
				cell.setCellValue(count);
			}
		}
	}

	/**
	 * Generate a default name for a sheet.
	 * 
	 * @return
	 */
	private String getDefaultSheetName(Question question) {
		return "Q" + (question.getDisplayOrder());
	}

	/**
	 * Create a basic sheet with the report's date range used to create a header
	 * row.
	 * 
	 * @param wb
	 * @param sheetName
	 * @param question
	 * @return
	 */
	private HSSFSheet createSheetWithMonthlyHeader(HSSFWorkbook wb, String sheetName, Question question, CalendarReport report) {
		// configure sheet for this question
		HSSFSheet sheet = wb.createSheet(getDefaultSheetName(question));

		addDefaultQuestionHeader(sheet, question, question.getDisplayTitle());

		{
			// the header for the row (choice name)
			HSSFRow headerRow = sheet.createRow(2);

			//
			DateFormat df = report.getDateFormat();

			// each Date is the first day of a month
			int headerColNum = 1;

			for (Date date : report.getDateList()) {
				HSSFCell header = headerRow.createCell(headerColNum);
				String dateString = df.format(date);
				HSSFRichTextString headerValue = new HSSFRichTextString(dateString);
				sheet.setColumnWidth(headerColNum, (dateString.length() + 2) * 256);

				// HSSFCellStyle cellStyle = header.getCellStyle();
				// cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				// header.setCellStyle( );
				headerValue.applyFont(HSSFFont.BOLDWEIGHT_BOLD);
				header.setCellValue(headerValue);

				headerColNum++;
			}
		}

		return sheet;
	}

}

package com.oas.controller.dashboard.report;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.model.report.ResponsesPerDay;
import com.oas.service.ReportingService;

/**
 * Prompts the user to select a survey to report on and then presents a list of
 * available reports.
 * 
 * @author xhalliday
 * @since September 26, 2008
 */
@Controller
public class ResponsesPerDayController extends AbstractOASController {

	/** Number of milliseconds in a day. */
	private static final long MILLISECS_PER_DAY = 86400 * 1000;

	/** Height of the Response Per Day graph image. */
	public static final int RESPONSE_PER_DAY_GRAPH_HEIGHT = 150;

	/** Width of the Response Per Day graph image. */
	public static final int RESPONSE_PER_DAY_GRAPH_WIDTH = 750;

	@Autowired
	private ReportingService reportingService;

	@Autowired
	private MessageSource messageSource;

	public ResponsesPerDayController() {
	}

	/**
	 * Main report generating method.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
	@RequestMapping("/db/rpt/rpd/*.png")
	@ValidUser
	public ModelAndView doReport(HttpServletRequest request, HttpServletResponse response) throws IOException {

		requireSecureContext();

		// handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		PlotOrientation orientation = PlotOrientation.VERTICAL;

		Collection<ResponsesPerDay> data = reportingService.getResponseRatePerDay(survey, 10);
		Assert.notNull(data, "no data");

		DateFormat df = RelativeDateFormat.getDateInstance(RelativeDateFormat.SHORT, LocaleContextHolder.getLocale());

		for (ResponsesPerDay rpd : data) {
			Long count = rpd.getCount();
			Date date = rpd.getId().getDate();

			String reportData = df.format(date);

			String rowKey = "hasResponses";

			dataset.setValue(count, rowKey, reportData);
		}

		String chartTitle = messageSource.getMessage("report.responsesPerDay.title", null, "Responses Per Day",
				LocaleContextHolder.getLocale());

		// "Responses per Day"
		// JFreeChart chart = ChartFactory.createWaterfallChart(
		JFreeChart chart = ChartFactory.createAreaChart(//
				// JFreeChart chart = ChartFactory.createLineChart(
				// JFreeChart chart = ChartFactory.createLineChart3D(
				// JFreeChart chart = ChartFactory.createBarChart3D(//
				// JFreeChart chart = ChartFactory.createStackedBarChart3D(
				//
				chartTitle, null, null, dataset, orientation, false, true, false);
		chart.setBackgroundPaint(ChartColor.WHITE);
		response.reset();
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, RESPONSE_PER_DAY_GRAPH_WIDTH,
				RESPONSE_PER_DAY_GRAPH_HEIGHT);

		// return new ModelAndView("/reports/responsesPerDay/report", model);
		return null;
	}

	/**
	 * Shows the same info as the chart, but in a table.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
	@RequestMapping("/db/rpt/rpd/*.html")
	@ValidUser
	public ModelAndView doReportDetails(HttpServletRequest request) throws IOException {

		requireSecureContext();

		// handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		Collection<ResponsesPerDay> data = reportingService.getResponseRatePerDay(survey, calculateSurveyAgeInDays(survey));
		Assert.notNull(data, "no data");

		ModelMap model = new ModelMap();
		model.addAttribute("survey", survey);
		model.addAttribute("data", data);

		return new ModelAndView("/reports/responsesPerDay/responsesPerDay", model);
	}

	/**
	 * Get the number of days that a Survey has existed.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @return int Number of days
	 */
	private int calculateSurveyAgeInDays(Survey survey) {
		Date created = survey.getCreated();
		Assert.notNull(created);

		int deltaDays = (int) ((new Date().getTime() - created.getTime()) / MILLISECS_PER_DAY);
		if (deltaDays < 1) {
			deltaDays = 1;
		}
		return deltaDays;
	}
}

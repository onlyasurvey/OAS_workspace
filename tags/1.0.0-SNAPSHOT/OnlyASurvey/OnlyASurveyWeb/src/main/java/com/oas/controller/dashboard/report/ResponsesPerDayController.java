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
	 * @return
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

		Collection<ResponsesPerDay> data = reportingService.getResponseRatePerDay(survey, 7);
		Assert.notNull(data, "no data");

		// dataset.setValue("B", 1L)

		DateFormat df = RelativeDateFormat.getDateInstance(RelativeDateFormat.SHORT, LocaleContextHolder.getLocale());

		for (ResponsesPerDay rpd : data) {
			Long count = rpd.getCount();
			Date date = rpd.getId().getDate();

			String reportData = df.format(date);

			String rowKey = "hasResponses";

			dataset.setValue(count, rowKey, reportData);
		}
		// "Responses per Day"
		JFreeChart chart = ChartFactory.createBarChart3D(messageSource.getMessage("report.responsesPerDay.title", null,
				"Responses Per Day", LocaleContextHolder.getLocale()), null, null, dataset, orientation, false, true, false);
		chart.setBackgroundPaint(ChartColor.WHITE);
		response.reset();
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 500, 150);

		// return new ModelAndView("/reports/responsesPerDay/report", model);
		return null;
	}
}

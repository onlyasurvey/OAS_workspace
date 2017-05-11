package com.oas.controller.dashboard.report;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.service.ReportingService;

/**
 * Prompts the user to select a survey to report on and then presents a list of
 * available reports.
 * 
 * @author xhalliday
 * @since November 16, 2008
 */
@Controller
public class ResponsesPerLanguageController extends AbstractOASController {

	@Autowired
	private ReportingService reportingService;

	@Autowired
	private MessageSource messageSource;

	public ResponsesPerLanguageController() {
	}

	/**
	 * Main report generating method.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/db/rpt/rpl/*.png")
	@ValidUser
	public ModelAndView doReport(HttpServletRequest request, HttpServletResponse response) throws IOException {

		requireSecureContext();

		// handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		DefaultPieDataset dataset = new DefaultPieDataset();
		// PlotOrientation orientation = PlotOrientation.VERTICAL;

		Map<SupportedLanguage, Integer> data = reportingService.getResponsesPerLanguage(survey);
		Assert.notNull(data, "no data");

		for (SupportedLanguage language : data.keySet()) {

			// percentage
			Integer percent = data.get(language);
			dataset.setValue(language.getDisplayTitle(), percent);
		}

		// "Responses per Day"
		Locale locale = LocaleContextHolder.getLocale();
		JFreeChart chart = ChartFactory.createPieChart3D(messageSource.getMessage("report.responsesPerLanguage.title", null, "Per Language", locale), dataset,
				false, false, locale);

		chart.setBackgroundPaint(ChartColor.WHITE);
		response.reset();
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 200, 150);

		// return new ModelAndView("/reports/responsesPerDay/report", model);
		return null;
	}
}

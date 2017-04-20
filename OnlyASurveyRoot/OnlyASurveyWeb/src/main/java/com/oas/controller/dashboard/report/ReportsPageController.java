package com.oas.controller.dashboard.report;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.service.DashboardService;
import com.oas.service.ReportingService;

/**
 * Prompts the user to select a survey to report on and then presents a list of
 * available reports.
 * 
 * @author xhalliday
 * @since September 26, 2008
 */
@Controller
public class ReportsPageController extends AbstractOASController {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private ReportingService reportingService;

	public ReportsPageController() {
	}

	@RequestMapping("/db/rpt.html")
	@ValidUser
	public ModelAndView doShowSurveyList() {

		requireSecureContext();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("list", dashboardService.findSurveys());
		Assert.notNull(model);

		return new ModelAndView("/reports/reports", model);
	}

	@RequestMapping("/db/rpt/*.html")
	@ValidUser
	public ModelAndView doShowReportList(HttpServletRequest request) {

		requireSecureContext();
		applyWideLayout(request);

		Map<String, Object> model = new HashMap<String, Object>();

		// get the survey ID from the URL; handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		// model
		model.put("survey", survey);
		model.put("byMonth", reportingService.getResponseRatePerMonth(survey, 2));

		Assert.notNull(model);

		return new ModelAndView("/reports/reportIndex", model);
	}
}

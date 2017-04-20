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
import com.oas.service.ReportingService;

/**
 * Shows the "Abandonment Report" detailing completion rate, where people
 * abandon responses, etc.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Controller
public class AbandonmentReportController extends AbstractOASController {

	/** Backing service. */
	@Autowired
	private ReportingService reportingService;

	/**
	 * Show a summary report by question and count of abandoned responses at
	 * that position.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
	@RequestMapping("/db/rpt/abnd/*.html")
	@ValidUser
	public ModelAndView questionSummaryReport(HttpServletRequest request) {

		// get the survey ID from the URL; handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		// model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("data", reportingService.getAbandonmentHighestQuestionSummary(survey));

		return new ModelAndView("/reports/abandonment/questionSummary", model);
	}

}

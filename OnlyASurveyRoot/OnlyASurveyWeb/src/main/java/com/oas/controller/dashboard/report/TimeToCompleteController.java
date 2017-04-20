package com.oas.controller.dashboard.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.service.ReportingService;

/**
 * Report with a table that shows how long it takes people to complete the
 * survey.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Controller
public class TimeToCompleteController extends AbstractOASController {

	/** Backing service. */
	@Autowired
	private ReportingService reportingService;

	/** i18n. */
	@Autowired
	private MessageSource messageSource;

	// ======================================================================

	public TimeToCompleteController() {
	}

	// ======================================================================

	/**
	 * Generate the table.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param response
	 *            {@link HttpServletResponse}
	 * @throws IOException
	 *             On writing
	 */
	@RequestMapping("/db/rpt/ttc/*.html")
	@ValidUser
	public ModelAndView timeToCompleteSurvey(HttpServletRequest request) throws IOException {

		// get the survey ID from the URL; handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		// data comes in sorted
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("data", reportingService.getTimeTakenData(survey));
		return new ModelAndView("/reports/time/timeTakenToComplete", model);
	}
}

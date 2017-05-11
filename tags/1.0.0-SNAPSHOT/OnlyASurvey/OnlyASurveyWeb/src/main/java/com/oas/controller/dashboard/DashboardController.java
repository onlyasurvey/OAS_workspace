package com.oas.controller.dashboard;

import java.util.Collection;
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
import com.oas.model.report.SurveySummary;
import com.oas.service.DashboardService;

@Controller
// @RequestMapping("/db/*.html")
public class DashboardController extends AbstractOASController {

	@Autowired
	private DashboardService dashboardService;

	public DashboardController() {
	}

	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String, Object>();

		Collection<SurveySummary> summaryList = dashboardService.findSurveySummaries();
		model.put("summaryList", summaryList);
		model.put("publishedCount", publishedCount(summaryList));

		return model;
	}

	protected Long publishedCount(Collection<SurveySummary> summaryList) {
		long retval = 0;

		for (SurveySummary summary : summaryList) {
			if (summary.getSurvey().isPublished()) {
				retval++;
			}
		}

		return retval;
	}

	@RequestMapping("/db/db.html")
	@ValidUser
	public ModelAndView doMain(HttpServletRequest request) {

		requireSecureContext();

		Map<String, Object> model = referenceData(request);
		Assert.notNull(model);

		applyWideLayout(request);
		return new ModelAndView("/dashboard/dashboard", model);
	}

}

package com.oas.controller.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.AccountOwner;
import com.oas.model.report.SurveySummary;
import com.oas.service.DashboardService;

/**
 * Dashboard Controller, the main view in the application.
 * 
 * @author xhalliday
 */
@Controller
public class DashboardController extends AbstractOASController {

	/** Supporting service. */
	@Autowired
	private DashboardService dashboardService;

	/** Generic data access service. */
	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	/** Default constructor. */
	public DashboardController() {
	}

	/**
	 * Get reference data for the view.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * 
	 * @return Map<String,Object>
	 */
	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String, Object>();

		// the in-session user record will not always be fresh; always load from
		// the DB to ensure the timeliest data, esp. considering admin actions
		// that cannot affect `whoami`'s session
		AccountOwner accountOwner = dataAccessObject.get(AccountOwner.class, getCurrentUser().getId());
		if (accountOwner == null) {
			String message = "could not find the current user in the database";
			log.error(message);
			Assert.notNull(accountOwner, message);
		}

		Collection<SurveySummary> summaryList = dashboardService.findSurveySummaries();
		model.put("summaryList", summaryList);
		model.put("publishedCount", publishedCount(summaryList));
		model.put("accountOwner", accountOwner);

		return model;
	}

	/**
	 * Determine how many of the Surveys in the summary list have been
	 * published.
	 * 
	 * @param summaryList
	 *            Input
	 * @return Count of Published Surveys
	 */
	protected Long publishedCount(Collection<SurveySummary> summaryList) {
		long retval = 0;

		for (SurveySummary summary : summaryList) {
			if (summary.getSurvey().isPublished()) {
				retval++;
			}
		}

		return retval;
	}

	/**
	 * Main View.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
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

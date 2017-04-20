package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.persistence.DataAccessObject;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;

/**
 * Web Site Integration - Visitor Opt-In - editor controller, allows the user to
 * set the likelihood that a visitor will be shown the opt-in lightbox.
 * 
 * @author xhalliday
 * @since February 22, 2010
 */
@Controller
public class WebSiteIntegrationEditorController extends AbstractOASController {

	/** Generic DAO. */
	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	/**
	 * Default view.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pb/wsi/optin/*.html", method = { RequestMethod.GET })
	public ModelAndView defaultView(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);

		// show view
		// applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/publishTab/siteIntegration/siteIntegration", model);
	}

	/**
	 * SAVE submit handler.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
	@RequestMapping(value = "/db/mgt/pb/wsi/optin/*.html", params = { "_save" }, method = { RequestMethod.POST })
	public ModelAndView submit(HttpServletRequest request, @RequestParam("percent") String percentParam) {

		// paranoia: browsers don't always behave as expected
		if (!isSave(request)) {
			return cancel(request);
		}

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		long percent = 100;
		try {
			percent = Long.valueOf(percentParam);
		} catch (Exception e) {
			log.warn("User entered invalid number for percent of visitors to see opt-in lightbox: " + percentParam);
			// TODO add error message
			return defaultView(request);
		}

		// fix for sloppy users
		if (percent < 0) {
			percent = 0;
		} else if (percent > 100) {
			percent = 100;
		}

		// save change
		survey.setOptinPercentage(percent);
		dataAccessObject.persist(survey);

		//
		return createRedirect(request, "/html/db/mgt/pb/" + survey.getId() + ".html");
	}

	/**
	 * CANCEL submit handler.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView}
	 */
	@RequestMapping(value = "/db/mgt/pb/wsi/optin/*.html", params = { "_cancel" }, method = { RequestMethod.POST })
	public ModelAndView cancel(HttpServletRequest request) {
		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		return createRedirect("/html/db/mgt/pb/" + survey.getId() + ".html");
	}
}

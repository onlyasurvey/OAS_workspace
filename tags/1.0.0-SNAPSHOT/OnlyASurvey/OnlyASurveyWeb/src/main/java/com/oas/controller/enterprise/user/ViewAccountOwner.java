package com.oas.controller.enterprise.user;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.AccountOwner;
import com.oas.security.SecurityAssertions;
import com.oas.service.enterprise.EnterpriseDashboardService;

/**
 * Primary user viewing controller.
 * 
 * @author xhalliday
 * @since February 16, 2009
 */
@Controller
public class ViewAccountOwner extends AbstractOASController {

	/** Service for general Enterprise operations. */
	@Autowired
	private EnterpriseDashboardService enterpriseDashboardService;

	/** Default constructor. */
	public ViewAccountOwner() {
	}

	/**
	 * Default Enterprise Dashboard view.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ent/ao/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView viewAccountOwner(HttpServletRequest request) throws Exception {

		// security
		SecurityAssertions.assertEnterpriseAdmin();

		// load the subject
		AccountOwner subject = getEntityFromRestfulUrl(AccountOwner.class, request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("subject", subject);
		model.put("surveyList", enterpriseDashboardService.findSurveysFor(subject, true));

		// view
		return new ModelAndView("/enterprise/accountOwner/viewAccountOwner", model);
	}
}

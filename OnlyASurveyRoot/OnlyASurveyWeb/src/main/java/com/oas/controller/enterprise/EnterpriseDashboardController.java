package com.oas.controller.enterprise;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.security.SecurityAssertions;
import com.oas.service.enterprise.EnterpriseDashboardService;

/**
 * Main home page controller for the Enterprise package, which shows summary
 * data and provides navigation to more in-depth views.
 * 
 * @author xhalliday
 * @since October 19, 2008
 */
@Controller
public class EnterpriseDashboardController extends AbstractOASController {

	/** Dashboard service. */
	@Autowired
	private EnterpriseDashboardService enterpriseDashboardService;

	/** Default constructor. */
	public EnterpriseDashboardController() {
	}

	/**
	 * Load data required for the dashboard view.
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();

		// quick-status
		model.put("enterpriseQuickStats", enterpriseDashboardService.getQuickStats());

		// Contact Us messages
		model.put("contactMessages", enterpriseDashboardService.findContactUsMessages(getDisplayTagPageNumber(request, "cMsg"),
				10));
		model.put("contactMessagesSize", enterpriseDashboardService.countContactUsMessages());

		// Account Owners
		model.put("accountOwners", enterpriseDashboardService.findAccountOwners(getDisplayTagPageNumber(request, "acOw"), 25));
		model.put("accountOwnersSize", enterpriseDashboardService.countAccountOwners());

		return model;
	}

	/**
	 * Default Enterprise Dashboard view.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/ent/db/db.html")
	@ValidUser
	public ModelAndView dashboardView(HttpServletRequest request) throws Exception {

		//
		SecurityAssertions.assertEnterpriseAdmin();

		//
		applyWideLayout(request);
		return new ModelAndView("/enterprise/dashboard/dashboard", referenceData(request));
	}
}

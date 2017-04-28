package com.oas.controller.enterprise.user.billing;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.AccountBillType;
import com.oas.model.AccountOwner;
import com.oas.security.SecurityAssertions;
import com.oas.service.enterprise.EnterpriseDashboardService;

/**
 * Change Account.BillType for a user.
 * 
 * @author xhalliday
 * @since July 3, 2009
 */
@Controller
public class ChangeBillTypeController extends AbstractOASController {

	/** Service for general Enterprise operations. */
	@Autowired
	private EnterpriseDashboardService enterpriseDashboardService;

	/** Default constructor. */
	public ChangeBillTypeController() {
	}

	/**
	 * Success view used when the Bill Type is changed for an
	 * {@link AccountOwner}.
	 * 
	 * @param request
	 * @return
	 */
	// TODO this is a good idea but no hard req, todo later
	// @RequestMapping(value = "/ent/ao/chgbt/*.html", method =
	// RequestMethod.GET)
	// @ValidUser
	// public ModelAndView changeBillTypeSuccess(HttpServletRequest request)
	// {
	// }
	/**
	 * Form submission for changing an {@link AccountOwner}'s BillType property.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link ModelAndView} Redirect
	 */
	@RequestMapping(value = "/ent/ao/chgbt/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView changeBillTypeSubmit(HttpServletRequest request, @RequestParam("billType") String billTypeParam) {

		// security
		SecurityAssertions.assertEnterpriseAdmin();

		// load the subject
		AccountOwner subject = getEntityFromRestfulUrl(AccountOwner.class, request);
		Assert.notNull(subject);

		AccountBillType billType = AccountBillType.valueOf(billTypeParam);
		subject.setBillType(billType);

		// view
		return createRedirect(request, "/html/ent/ao/" + subject.getId() + ".html");
	}
}

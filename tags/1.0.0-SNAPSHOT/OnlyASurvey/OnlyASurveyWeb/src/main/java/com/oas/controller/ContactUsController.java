package com.oas.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.SecurityUtil;

import com.oas.command.model.ContactUsCommand;
import com.oas.service.SiteService;

/**
 * Controller behind the "Contact Us" functionality.
 * 
 * @author xhalliday
 * @since November 15, 2008
 */
@Controller
public class ContactUsController extends AbstractOASController {

	/** Site Service. */
	@Autowired
	private SiteService siteService;

	/**
	 * Show the initial form.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/con/frm.html", method = RequestMethod.GET)
	public ModelAndView doForm(HttpServletRequest request) {

		ContactUsCommand command = new ContactUsCommand();
		setDefaultEmailValue(command);
		applyWideLayout(request);
		return new ModelAndView("/contactUs/contactUsForm", "command", command);
	}

	@RequestMapping(value = "/con/frm.html", method = RequestMethod.POST)
	public ModelAndView doSubmit(HttpServletRequest request, ContactUsCommand command) {

		BindException errors = new BindException(command, "command");

		// super-cheapo validation since this is such a trivial command
		if (!StringUtils.hasText(command.getMessage())) {
			errors.rejectValue("message", "contactUs.error.messageRequired");
		}

		applyWideLayout(request);

		// show form errors or save and redirect out
		if (errors.hasErrors()) {
			// validation errors

			// set default email for authenticated users if not already set by
			// the user
			setDefaultEmailValue(command);

			//
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("command", command);
			model.put("errors", errors);
			return new ModelAndView("/contactUs/contactUsForm", model);
		} else {
			// save to the backend
			siteService.addContactMessage(command);

			// redirect to Thanks message
			return createRedirect("/html/con/tx.html");
		}
	}

	@RequestMapping(value = "/con/tx.html", method = RequestMethod.GET)
	public ModelAndView doThanks(HttpServletRequest request) {
		applyWideLayout(request);
		return new ModelAndView("/contactUs/thanks");
	}

	// ======================================================================

	/**
	 * If a user is logged in, and no email address has been set in the form,
	 * then set it from their profile.
	 */
	private void setDefaultEmailValue(ContactUsCommand command) {
		if (!StringUtils.hasText(command.getEmail())) {
			if (SecurityUtil.isSecureContext()) {
				command.setEmail(SecurityUtil.getCurrentUser().getEmail());
			}
		}
	}
}

package com.oas.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.util.Constants;

/**
 * TODO REVIEW: usefulness of controller
 */
@Controller
public class LoginController extends AbstractOASController {

	/**
	 * If the user is authenticated then redirect to default home, otherwise
	 * present the Login Form.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/lgn.html")
	protected ModelAndView loginForm(HttpServletRequest request) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() != null) {
			// already authenticated
			return new ModelAndView(new RedirectView(Constants.DEFAULT_HOME, true));
		}

		// show login form
		applyWideLayout(request);
		return new ModelAndView("/loginForm");
	}

	/**
	 * Show the Home Page.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/oas.html")
	protected ModelAndView homePage(HttpServletRequest request) {

		// show login form
		applyWideLayout(request);
		return new ModelAndView("/homePage");
	}
}

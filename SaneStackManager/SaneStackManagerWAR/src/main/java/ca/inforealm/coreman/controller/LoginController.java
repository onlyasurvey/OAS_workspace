package ca.inforealm.coreman.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.controller.AbstractAnnotatedSaneController;

/**
 * TODO REVIEW: usefulness of controller
 */
@Controller
@RequestMapping("/app/login.html")
public class LoginController extends AbstractAnnotatedSaneController {

	@RequestMapping("/login.html")
	protected ModelAndView doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() != null) {
			// already authenticated
			return new ModelAndView(new RedirectView("/app/main.html"));
		}

		// show login form
		return new ModelAndView("/loginForm");
	}
}

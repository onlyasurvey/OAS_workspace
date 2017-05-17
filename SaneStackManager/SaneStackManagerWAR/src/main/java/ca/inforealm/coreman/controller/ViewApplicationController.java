package ca.inforealm.coreman.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.security.GlobalRoles;

@Controller
@RequestMapping("/app/viewApplication.html")
public class ViewApplicationController extends AbstractAnnotatedSadmanController {

	// ======================================================================

	@RequestMapping("/viewApplication.html")
	protected ModelAndView doViewApplication(HttpServletRequest request, HttpServletResponse response) {

		// require valid user and security context
		requireSecureContext();

		// ?id= must always be set
		requireIdParameter(request);

		// get the value
		Long id = getId(request);

		// ensure user is an app admin for the specified application
		assertHasApplicationRole(id, GlobalRoles.ROLE_APPLICATION_ADMIN);

		// load subject data into model and go
		Application subject = applicationManagementService.load(id);

		return new ModelAndView("viewApplication", "subject", subject);
	}

	// ======================================================================

}

package ca.inforealm.coreman.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Application;

/**
 * Shows a Main Page view.
 * 
 * @author Jason Mroz
 * 
 */
@Controller
@RequestMapping("/app/main.html")
public class MainPageController extends AbstractAnnotatedSadmanController {

	// ======================================================================

	@RequestMapping("/main.html")
	protected ModelAndView doMain(HttpServletRequest request, HttpServletResponse response) {

		// must be logged in, etc.
		requireSecureContext();

		// service dependency
		Assert.notNull(applicationManagementService, "applicationManagementService is not available");

		// get a list of applications that the user has APP_ADMIN access to
		List<Application> applications = applicationManagementService.getApplicationsForAdmin();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("applications", applications);

		//
		return new ModelAndView("mainPage", model);
	}

	// ======================================================================

}

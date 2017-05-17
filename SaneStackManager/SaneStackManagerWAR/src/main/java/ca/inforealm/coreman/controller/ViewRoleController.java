package ca.inforealm.coreman.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.GlobalRoles;

@Controller
@RequestMapping("/app/viewRole.html")
public class ViewRoleController extends AbstractAnnotatedSadmanController {

	// ======================================================================

	@RequestMapping("/viewRole.html")
	protected ModelAndView doViewRole(HttpServletRequest request, HttpServletResponse response) {

		// require valid user and security context
		requireSecureContext();

		// ?id= must always be set
		requireIdParameter(request);

		// get the value
		Long id = getId(request);

		// load the role
		RoleDefinition role = roleManagementService.load(id);
		Assert.notNull(role);

		// get the associated application
		Application application = role.getApplication();
		Assert.notNull(application);

		// SECURE: ensure the user has APP_ADMIN on the application that this
		// role belongs to
		assertHasApplicationRole(application.getId(), GlobalRoles.ROLE_APPLICATION_ADMIN);

		// load all related users
		Collection<Actor> list = roleManagementService.getRoleMembers(application.getId(), role.getIdentifier());

		// data for front end
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("subject", role);
		model.put("list", list);
		model.put("application", application);

		//
		return new ModelAndView("/viewRole", model);
	}

	// ======================================================================

}

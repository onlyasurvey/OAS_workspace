package ca.inforealm.coreman.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.web.Keys;

/**
 * Assigns a user to a role in an application, providing both search and commit
 * functionality.
 * 
 * @author Jason Mroz
 * 
 */
@Controller
@RequestMapping("/app/revokeRole.html")
public class RevokeRoleController extends AbstractAnnotatedSadmanController {

	// ======================================================================

	@RequestMapping("/revokeRole.html")
	protected ModelAndView doRevokeRole(HttpServletRequest request, HttpServletResponse response) {

		// must be logged in, etc.
		requireSecureContext();

		// get the role in question
		requireIdParameter(request, Keys.ROLE_ID);
		Long roleId = getId(request, Keys.ROLE_ID);

		// the actor
		requireIdParameter(request, Keys.ACTOR_ID);
		Long actorId = getId(request, Keys.ACTOR_ID);

		// load the role
		RoleDefinition role = roleManagementService.load(roleId);
		Assert.notNull(role);

		// get the associated application
		Application application = role.getApplication();
		Assert.notNull(application);

		// SECURE: ensure the user has APP_ADMIN on the application that this
		// role belongs to
		assertHasApplicationRole(application.getId(), GlobalRoles.ROLE_APPLICATION_ADMIN);

		// do the deed
		roleManagementService.revokeRole(new Actor(actorId), role);
		return new ModelAndView(new RedirectView("viewRole.html?id=" + role.getId()));
	}

	// ======================================================================

}

package ca.inforealm.coreman.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.service.ActorQueryService;

/**
 * Assigns a user to a role in an application, providing both search and commit
 * functionality.
 * 
 * @author Jason Mroz
 * 
 */
@Controller
@RequestMapping("/app/assignRole.html")
public class AssignRoleController extends AbstractAnnotatedSadmanController {

	protected ActorQueryService actorQueryService;

	// ======================================================================

	@RequestMapping("/assignRole.html")
	protected ModelAndView doAssignRole(HttpServletRequest request, HttpServletResponse response) {

		// sanity
		Assert.notNull(actorQueryService, "missing service dependency");

		// must be logged in, etc.
		requireSecureContext();

		// get the role in question
		requireIdParameter(request, "roleId");
		Long id = getId(request, "roleId");

		// load the role
		RoleDefinition role = roleManagementService.load(id);
		Assert.notNull(role);

		// get the associated application
		Application application = role.getApplication();
		Assert.notNull(application);

		// SECURE: ensure the user has APP_ADMIN on the application that this
		// role belongs to
		assertHasApplicationRole(application.getId(), GlobalRoles.ROLE_APPLICATION_ADMIN);

		// get the actorId, if specified
		Long actorId = getId(request, "actorId");
		if (actorId != null) {
			// do the deed

			roleManagementService.assignRole(new Actor(actorId), role);
			return new ModelAndView(new RedirectView("viewRole.html?id=" + role.getId()));
		} else {
			// show the form
			Map<String, Object> model = new HashMap<String, Object>();
			String query = request.getParameter("q");
			if (query != null && query.trim().length() > 0) {
				model.put("list", actorQueryService.findUserByAny(query));
			} else {
				model.put("list", new ArrayList());
			}

			model.put("subject", role);

			// show a view
			return new ModelAndView("/assignRole", model);
		}
	}

	// ======================================================================

	/**
	 * @param actorQueryService
	 *            the actorQueryService to set
	 */
	@Autowired
	public void setActorQueryService(ActorQueryService actorQueryService) {
		this.actorQueryService = actorQueryService;
	}

}

package ca.inforealm.coreman.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.support.WebContentGenerator;

import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.MainPageController;

public class MainPageControllerTest extends AbstractSaneManBaseTest {

	public void testMainPage_Pass_ZeroApps() throws Exception {

		// initialize security context
		createAndSetSecureUserWithRoleUser();

		// the good stuff
		MainPageController controller = (MainPageController) getApplicationContext().getBean("mainPageController");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod(WebContentGenerator.METHOD_GET);
		org.springframework.web.servlet.ModelAndView mav = controller.doMain(request, new MockHttpServletResponse());

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull(mav.getModel().get("applications"));

		List<Application> list = (List<Application>) mav.getModel().get("applications");
		assertNotNull(list);
		assertEquals("should have zero applications", 0, list.size());
	}

	public void testMainPage_Pass_NonZeroAppCount() throws Exception {

		// initialize security context and add some roles so the current test
		// app is in the returned list
		Collection<RoleDefinition> roles = new ArrayList<RoleDefinition>();
		roles.add(createRoleDefinition("ROLE_SOMETHING"));
		roles.add(getSaneContext().getRoleDefinition(GlobalRoles.ROLE_USER));
		roles.add(getSaneContext().getRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN));
		createAndSetSecureUser(roles, null);

		// create a second app with roles for the user, so it also shows up int
		// he list
		Application app2 = new Application();
		app2.setIdentifier("someApp" + getMBUN());
		RoleDefinition app2role = new RoleDefinition(app2, GlobalRoles.ROLE_APPLICATION_ADMIN);
		app2.getRoleDefinitions().add(app2role);
		getHibernateTemplate().persist(app2);
		getHibernateTemplate().persist(new ActorRole(getCurrentUser(), app2role));

		// the good stuff
		MainPageController controller = (MainPageController) getApplicationContext().getBean("mainPageController");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod(WebContentGenerator.METHOD_GET);
		org.springframework.web.servlet.ModelAndView mav = controller.doMain(request, new MockHttpServletResponse());

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull(mav.getModel().get("applications"));

		List<Application> list = (List<Application>) mav.getModel().get("applications");
		assertNotNull(list);
		assertEquals("should have zero applications", 2, list.size());
	}

}

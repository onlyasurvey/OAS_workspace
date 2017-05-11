package ca.inforealm.coreman.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;

@Controller
@RequestMapping("/app/manageConfiguration.html")
public class ManageConfigurationController extends AbstractAnnotatedSadmanController implements InitializingBean {
	// ======================================================================

	@RequestMapping("/manageConfiguration.html")
	public ModelAndView doManageConfiguration(HttpServletRequest request, HttpServletResponse response) {

		// require valid user and security context
		requireSecureContext();

		// load subject data into model and go
		Application application = getRequestedApplication(request);
		Collection<ConfigurationItem> subject = configurationManagementService.getConfigurationItems(application.getId());

		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("subject", subject);
		model.put("application", application);

		return new ModelAndView("manageConfiguration", model);
	}

	// ======================================================================

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(configurationManagementService, "configurationManagementService needs to be wired in");
	}

	// ======================================================================
}

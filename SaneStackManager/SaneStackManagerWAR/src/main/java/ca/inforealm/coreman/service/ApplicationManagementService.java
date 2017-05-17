package ca.inforealm.coreman.service;

import java.util.List;

import ca.inforealm.core.model.Application;

public interface ApplicationManagementService {

	/**
	 * List all applications in the back-end that the user has any role in.
	 * 
	 * @return
	 */
	public abstract List<Application> getApplicationsForAdmin();

	/**
	 * Load an application by it's ID.
	 * 
	 * @param id
	 * @return
	 */
	public abstract Application load(Long id);

}
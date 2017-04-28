package ca.inforealm.core.service.dao;

import java.util.Collection;
import java.util.Date;

import ca.inforealm.core.model.ResourceString;

public interface ResourceStringServiceDAO {

	/**
	 * Retrieve all resources defined for this application.
	 * 
	 * @return
	 */
	Collection<ResourceString> loadAllResources();

	/**
	 * Load all resources that have changed since the baselineDate.
	 * 
	 * @param baselineDate
	 * @return
	 */
	Collection<ResourceString> loadNewerResources(Date baselineDate);

}
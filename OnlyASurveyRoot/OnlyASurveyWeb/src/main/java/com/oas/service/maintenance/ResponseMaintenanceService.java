package com.oas.service.maintenance;

/**
 * Service for maintaining the Response repository.
 * 
 * @author xhalliday
 * @since 2009-10-16
 */
public interface ResponseMaintenanceService {

	/**
	 * Clean-up partial responses after a reasonable grace period.
	 */
	void cleanUpPartialResponses();

}

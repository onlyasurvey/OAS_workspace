package ca.inforealm.core.support.autoinstall;

import ca.inforealm.core.model.Application;

/**
 * Implementations of this interface can register their application, i.e., as
 * opposed to a create script.
 * 
 * @author xhalliday
 * @since May 2, 2009
 */
public interface ApplicationAutoInstaller {

	/**
	 * Perform steps required to install this application into the core
	 * Inforealm database - register it's Application model, roles, config
	 * items, etc.
	 * 
	 * Implementations are responsible for all persistence; the caller will
	 * assume that this method stores all required entities.
	 * 
	 * @return Application The newly persisted application model.
	 */
	Application inforealmRegistration();

	/**
	 * Perform any custom installation steps required by the application.
	 */
	void customInstallation();

}

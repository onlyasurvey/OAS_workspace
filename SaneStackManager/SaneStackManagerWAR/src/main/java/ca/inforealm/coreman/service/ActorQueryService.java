package ca.inforealm.coreman.service;

import java.util.Collection;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.service.AbstractServiceInterface;

/**
 * Provides query functionality for front-end controllers.
 * 
 * @author Jason Mroz
 * 
 */
public interface ActorQueryService extends AbstractServiceInterface {

	/**
	 * Returns all UserAccount's that match a given query string.
	 * 
	 * @param query
	 * @return
	 */
	public Collection<UserAccount> findUserByAny(String query);

}
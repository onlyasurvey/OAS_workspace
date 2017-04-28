package com.oas.service.enterprise;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractOASBaseTest;
import com.oas.util.EnterpriseRoles;

/**
 * Parent tests for all Enterprise service tests.
 * 
 * @author xhalliday
 * @since February 7, 2009
 */
abstract public class AbstractEnterpriseServiceTest extends AbstractOASBaseTest {

	protected UserAccount createEnterpriseAdmin() {
		// requires security context with the appropriate role
		return createAndSetSecureUser(new String[] { EnterpriseRoles.ROLE_ENTERPRISE_ADMIN }, null);
	}

}

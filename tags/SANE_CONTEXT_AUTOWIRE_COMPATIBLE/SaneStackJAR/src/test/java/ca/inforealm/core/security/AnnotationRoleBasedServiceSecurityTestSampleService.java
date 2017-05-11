package ca.inforealm.core.security;

import org.springframework.security.annotation.Secured;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

/**
 * Used by AnnotationRoleBasedServiceSecurityTest
 * 
 * @author Jason Mroz
 */

public class AnnotationRoleBasedServiceSecurityTestSampleService extends AbstractServiceImpl implements
		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface {

	@Secured( { GlobalRoles.ROLE_USER })
	public void somethingRequiringUserRole() {
	}

	@Secured( { GlobalRoles.ROLE_APPLICATION_ADMIN })
	public void somethingRequiringApplicationAdminRole() {
	}

	@Override
	public void somethingWithNoSecurityAnnotations() {
	}

	@Override
	@Unsecured
	public void somethingWithUnsecuredAnnotation() {
	}

	@Override
	@ValidUser
	public void somethingWithValidUserAnnotation() {
	}
}

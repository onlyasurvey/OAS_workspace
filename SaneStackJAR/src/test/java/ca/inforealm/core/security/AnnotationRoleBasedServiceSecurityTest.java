package ca.inforealm.core.security;

import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.AccessDeniedException;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;

/**
 * Tests role-based security annotations on service objects.
 * 
 * @author Jason Mroz
 */
public class AnnotationRoleBasedServiceSecurityTest extends AbstractTestDataCreatingBaseTest {

	@Autowired
	private AnnotationRoleBasedServiceSecurityTestSampleServiceInterface annotationRoleBasedServiceSecurityTestSampleService;

	protected AnnotationRoleBasedServiceSecurityTestSampleServiceInterface getSampleServiceImpl() {
		return annotationRoleBasedServiceSecurityTestSampleService;
	}

	// ======================================================================

	@Test
	public void testSecuredAnnotation_AssignedRoleWorks() {

		// USER
		{
			// create user with role required by method being invoked
			createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER }, null);

			// invoke method requiring above role
			getSampleServiceImpl().somethingRequiringUserRole();
		}

		// test a second role to ensure the stack is not blindly
		// accepting/denying calls

		// APPLICATION ADMIN
		{
			// create user with role required by method being invoked
			createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

			// invoke method requiring above role
			getSampleServiceImpl().somethingRequiringApplicationAdminRole();
		}
	}

	@Test
	public void testSecuredAnnotation_UnassignedRoleFails() {

		// USER
		{
			// create user WITHOUT role required by method being invoked
			createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

			try {
				// invoke method requiring above role
				getSampleServiceImpl().somethingRequiringUserRole();
				fail("should have thrown AccessDeniedException");
			} catch (AccessDeniedException e) {
				// expected
			}
		}

		// test a second role to ensure the stack is not blindly
		// accepting/denying calls

		// APPLICATION ADMIN
		{
			// create user WITHOUT role required by method being invoked
			createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER }, null);

			try {
				// invoke method requiring above role
				getSampleServiceImpl().somethingRequiringApplicationAdminRole();
				fail("should have thrown AccessDeniedException");
			} catch (AccessDeniedException e) {
				// expected
			}
		}
	}

}

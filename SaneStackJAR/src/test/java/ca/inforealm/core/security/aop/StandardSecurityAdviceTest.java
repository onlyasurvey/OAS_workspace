package ca.inforealm.core.security.aop;

import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.AccessDeniedException;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.security.AnnotationRoleBasedServiceSecurityTestSampleServiceInterface;

public class StandardSecurityAdviceTest extends AbstractTestDataCreatingBaseTest {

	@Autowired
	private AnnotationRoleBasedServiceSecurityTestSampleServiceInterface annotationRoleBasedServiceSecurityTestSampleService;

	protected AnnotationRoleBasedServiceSecurityTestSampleServiceInterface getSampleServiceImpl() {
		return annotationRoleBasedServiceSecurityTestSampleService;
	}

	@Test
	public void testUnsecuredAnnotation_WhenPresent() {
		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface service = getSampleServiceImpl();
		try {
			service.somethingWithUnsecuredAnnotation();
			// should have worked
		} catch (AccessDeniedException e) {
			fail("unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void testHasAnySecurityAnnotations_FailsWhenNonePresent() {
		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface service = getSampleServiceImpl();
		try {
			service.somethingWithNoSecurityAnnotations();
			fail("should have thrown");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testValidUserAnnotation_PassWithUserWithRole() {

		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface service = getSampleServiceImpl();
		createAndSetSecureUserWithRoleUser();

		try {
			service.somethingWithValidUserAnnotation();
		} catch (AccessDeniedException e) {
			fail("unexpected exception: " + e.getClass().getName() + " : " + e.getMessage());
		}
	}

	@Test
	public void testValidUserAnnotation_FailsWithUserWithNoRoles() {

		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface service = getSampleServiceImpl();
		createAndSetSecureUser(); // no roles

		try {
			service.somethingWithValidUserAnnotation();
			fail("should have thrown");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testValidUserAnnotation_FailsWithoutContext() {

		AnnotationRoleBasedServiceSecurityTestSampleServiceInterface service = getSampleServiceImpl();
		clearSecurityContext();

		try {
			service.somethingWithValidUserAnnotation();
			fail("should have thrown");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

}

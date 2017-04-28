package ca.inforealm.core.security.aop;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.security.AnnotationRoleBasedServiceSecurityTestSampleServiceInterface;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

public class AbstractSecurityAdviceTest extends AbstractBaseTest {

	@Autowired
	private AnnotationRoleBasedServiceSecurityTestSampleServiceInterface annotationRoleBasedServiceSecurityTestSampleService;

	private class Util_AbstractSecurityAdviceImpl extends AbstractSecurityAdvice {

		@Override
		public void before(Method method, Object[] args, Object target) throws Throwable {
			// TODO Auto-generated method stub

		}
	}

	private class TestClass_NoAnnotations extends AbstractServiceImpl {
		public void doSomeThing() {
		}
	}

	private class TestClass_SomeAnnotations extends AbstractServiceImpl {
		@Transactional
		@Secured( { "FOO" })
		public void doSomeThing() {
		}
	}

	protected AnnotationRoleBasedServiceSecurityTestSampleServiceInterface getSampleServiceImpl() {
		return annotationRoleBasedServiceSecurityTestSampleService;
	}

	@Test
	public void testHasAnySecurityAnnotations_WithNoAnnotations() throws Exception {
		Method method = TestClass_NoAnnotations.class.getMethod("doSomeThing", null);
		Util_AbstractSecurityAdviceImpl advice = new Util_AbstractSecurityAdviceImpl();
		assertFalse("should report no security annotations", advice.hasAnySecurityAnnotations(method));
	}

	@Test
	public void testHasAnySecurityAnnotations_WithSomeAnnotations() throws Exception {
		Method method = TestClass_SomeAnnotations.class.getMethod("doSomeThing", null);
		Util_AbstractSecurityAdviceImpl advice = new Util_AbstractSecurityAdviceImpl();
		assertTrue("should report SOME security annotations", advice.hasAnySecurityAnnotations(method));
	}

}

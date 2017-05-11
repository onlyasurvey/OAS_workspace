package ca.inforealm.core.security.aop;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.AccessDeniedException;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;

/**
 * Applies SAD-defined standard security advice to a method invocation.
 * 
 * Executed before the target method is invoked, optionally throwing an
 * AccessDeniedException. See
 * {@link AbstractSecurityAdvice#validateSecurityAnnotationPresence} for details
 * on how annotations are resolved.
 * 
 * This class requires that one of these be true:
 * 
 * a) Unsecured is the only security-related annotation present
 * 
 * b) Secured is present, and zero or more SANE security annotations are present
 * 
 * c) One or more SANE security annotations are present and both Secured and
 * Unsecured are not present
 * 
 * The Unsecured annotation will cause the security check to immediately
 * terminate. If any other annotations are present (other than Secured) then
 * they will be checked in sequence, potentially throwing an
 * AccessDeniedException.
 * 
 * <ul>
 * <li>Secured(String[])</li>
 * <li>Unsecured</li>
 * </ul>
 * 
 * @author Jason Mroz
 * @created July 12, 2008
 */
public class StandardSecurityAdvice extends AbstractSecurityAdvice {

	/** Logger */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * Advice body.
	 */
	@Override
	public void before(Method inboundMethod, Object[] args, Object target) throws Throwable {

		Class targetClass = AopUtils.getTargetClass(target);

		// get the real target method; invocation.getMethod will return a
		// proxy-generated method based on an interface, therefore no
		// annotations are present
		Method method = AopUtils.getMostSpecificMethod(inboundMethod, targetClass);

		// Annotation[] list = method.getAnnotations();
		// Object target = invocation.getThis();
		final String classAndMethodName = targetClass.getName() + "." + method.getName();

		// at least one of the required security annotations (or Unsecured) MUST
		// be present
		if (!validateSecurityAnnotationPresence(method)) {
			throwAccessDeniedException(classAndMethodName);
		}

		// ------------------------------------------------------------------
		// the @Unsecured annotation was explicitly added - that means that
		// no checks are done
		// ------------------------------------------------------------------
		if (method.isAnnotationPresent(Unsecured.class)) {
			//
			return;
		}

		// the @Secured annotation from Spring Security will allow this check to
		// pass even if no other annotations are present, but it will not
		// prevent other checks from being done if their corresponding
		// annotations are present

		// ------------------------------------------------------------------
		// check all other security annotations and possibly throw an exception
		// ------------------------------------------------------------------

		// (optional) @ValidUser or AccessDeniedException
		checkValidUserAnnotation(method);
	}

	// ======================================================================

	/**
	 * Requires only basic security context - authenticated user with at least
	 * one role
	 * 
	 * @throws AccessDeniedException
	 */
	/* package */void checkValidUserAnnotation(Method method) {
		if (method.isAnnotationPresent(ValidUser.class)) {
			SecurityUtil.requireSecureContext();
		}
	}

	// ======================================================================

	private void throwAccessDeniedException(String classAndMethodName) {
		String debugStr = "failed security check on method invocation (" + classAndMethodName + ")";
		log.warn(debugStr);
		throw new AccessDeniedException(debugStr);
	}
}

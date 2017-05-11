package ca.inforealm.core.security.aop;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.security.annotation.Secured;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;

/**
 * Applies SAD-defined standard security advice to a method invocation.
 * 
 * TODO review generics warnings
 * 
 * @author Jason Mroz
 * @created July 12, 2008
 */
public abstract class AbstractSecurityAdvice implements MethodBeforeAdvice {

	/**
	 * All supported security annotations (not including Unsecured) - by
	 * default, at least one is required.
	 */
	protected Class supportedAnnotations[] = new Class[] { Secured.class, ValidUser.class };

	/**
	 * Determine if the method being invoked has any of the required security
	 * annotations, including Unsecured, but not both Unsecured and other
	 * annotations.
	 * 
	 * @param method
	 * @return true if at least one of the required security annotations, or
	 *         Unsecured, is present on the method
	 */
	protected boolean validateSecurityAnnotationPresence(Method method) {

		// check for the Dirty Unsecured Annotation
		final boolean unsecured = isUnsecuredMethod(method);

		// check for any supported security annotations
		final boolean anySecurity = hasAnySecurityAnnotations(method);

		// needs to have one of the above
		final boolean hasRelevantAttributes = unsecured || anySecurity;

		// but not both
		final boolean hasConflictingAttributes = unsecured && anySecurity;

		//
		return hasRelevantAttributes && !hasConflictingAttributes;
	}

	/**
	 * Determine if the method has any supported security annotations (not
	 * including Unsecured).
	 * 
	 * @param method
	 * @return
	 */
	protected boolean hasAnySecurityAnnotations(Method method) {

		for (Class clazz : supportedAnnotations) {
			if (method.isAnnotationPresent(clazz)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the method has the Unsecured annotation.
	 * 
	 * @param method
	 * @return
	 */
	protected boolean isUnsecuredMethod(Method method) {
		return method.isAnnotationPresent(Unsecured.class);
	}
}

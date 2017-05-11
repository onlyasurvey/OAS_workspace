package ca.inforealm.core.model.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation indicating that a field or method should be set (and
 * updated) to a value matching that retrieved from
 * {@link ca.inforealm.core.service.ConfigurationService}
 * 
 * @author Jason Mroz
 * 
 */
@Target( { METHOD })
@Retention(RUNTIME)
public @interface SetByConfiguration {
	
	/**
	 * Identifier (as used by
	 * {@link ca.inforealm.core.service.ConfigurationService#getConfigurationItem(String)}
	 * to map to the annotated element.
	 * 
	 * @return
	 */
	String value();
}

package ca.inforealm.core.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as having no security constraints, ie., a truly public method.
 * 
 * @author Jason Mroz
 * @Created July 12, 2008
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Unsecured {

}

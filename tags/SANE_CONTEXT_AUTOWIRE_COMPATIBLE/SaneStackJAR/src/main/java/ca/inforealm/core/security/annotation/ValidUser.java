package ca.inforealm.core.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for methods to denote that they do require a security
 * context but that the particulars are irrelevant; suitable for
 * application-wide functionality, for example, getUserDepartmentName().
 * 
 * Requires an authenticated user with at least one role.
 * 
 * @author Jason Mroz
 * 
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ValidUser {

}

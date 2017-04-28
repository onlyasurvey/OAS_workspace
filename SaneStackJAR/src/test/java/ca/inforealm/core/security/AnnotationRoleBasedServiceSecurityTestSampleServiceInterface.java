package ca.inforealm.core.security;

public interface AnnotationRoleBasedServiceSecurityTestSampleServiceInterface {

	public abstract void somethingRequiringUserRole();

	public abstract void somethingRequiringApplicationAdminRole();

	public abstract void somethingWithNoSecurityAnnotations();

	public abstract void somethingWithUnsecuredAnnotation();

	public abstract void somethingWithValidUserAnnotation();
}
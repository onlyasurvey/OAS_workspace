package ca.inforealm.core.security;

public final class GlobalRoles {

	/**
	 * Typical user.
	 */
	public final static String ROLE_USER = "ROLE_USER";

	/**
	 * SPECIAL: Superuser is analogous to UNIX root account: all security checks
	 * pass. DO NOT ASSIGN in an application, this is a system-wide value.
	 */
	public final static String ROLE_SUPERUSER = "ROLE_SUPERUSER";

	/**
	 * Application administrator: can manage users, role membership, and often
	 * application-specific features.
	 */
	public final static String ROLE_APPLICATION_ADMIN = "ROLE_APPLICATION_ADMIN";

	/**
	 * Can manage SANE configuration of the application.
	 */
	public final static String ROLE_CONFIGURATION_MANAGER = "ROLE_CONFIGURATION_MANAGER";

	/**
	 * Generic guest role.
	 */
	public final static String ROLE_GUEST = "ROLE_GUEST";

	/**
	 * Roles typically assigned to users, convenient for Secured annotations
	 * 
	 */
	public final static String TYPICAL_USER_ROLES[] = new String[] { ROLE_USER, ROLE_APPLICATION_ADMIN, ROLE_SUPERUSER };

}

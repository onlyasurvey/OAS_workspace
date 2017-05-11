package ca.inforealm.core.security;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.Test;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.UserAccount;

public class UserDetailsServiceImplTest extends AbstractTestDataCreatingBaseTest {

	@Test
	public void testLoadUserByUsername() {
		UserDetailsService service = getUserDetailsService();

		UserDetails nonExistent = service.loadUserByUsername("some random thing 123");
		assertNull("should not have retrieved a user", nonExistent);

		UserAccount newUser = createTestUser();
		assertNotNull(newUser);

		// clear out any Hibernate caches/etc
		flushAndClear();

		UserDetails newlyLoaded = service.loadUserByUsername(newUser.getUsername());
		assertNotNull("should have non-null return value", newlyLoaded);
		assertEquals("usernames should match", newlyLoaded.getUsername(), newUser.getUsername());
	}

	@Test
	public void testLoadUserByUsername_WithEmail() {

		final String TEST_EMAIL = "test@email.com";

		UserDetailsService service = getUserDetailsService();

		UserDetails nonExistent = service.loadUserByUsername(TEST_EMAIL);
		assertNull("should not have retrieved a user", nonExistent);

		UserAccount newUser = createTestUser();
		newUser.setEmail(TEST_EMAIL);
		assertNotNull(newUser);

		// clear out any Hibernate caches/etc
		flushAndClear();

		UserDetails newlyLoaded = service.loadUserByUsername(newUser.getEmail());
		assertNotNull("should have non-null return value", newlyLoaded);
		// UserDetails has no email address info; compare usernames
		assertEquals("usernames should match", newlyLoaded.getUsername(), newUser.getUsername());
	}

	@Test
	public void testGrantedAuthoritiesLoad_Pass() {

		getSaneContext().getMergedApplicationModel();

		UserAccount user = createTestUser();
		ActorRole ar = new ActorRole(user, getSaneContext().getRoleDefinition(ROLE_USER));
		getHibernateTemplate().persist(ar);

		//
		// now, the UserDetailsService is supposed to return a User that has one
		// GrantedAuthority, ROLE_USER
		//
		UserDetails acegiUser = getUserDetailsService().loadUserByUsername(user.getUsername());
		assertNotNull("should have returned an acegi user", acegiUser);
		assertEquals("should have 1 GrantedAuthority", 1, acegiUser.getAuthorities().length);
		assertEquals("GrantedAuthority[0] should be ROLE_USER", "ROLE_USER", acegiUser.getAuthorities()[0].getAuthority());

	}

	/**
	 * The code under test will chomp(50) a username in log.debug to prevent
	 * DOS; cover it
	 */
	@Test
	public void testLoad_FailAndRespectMaxlenInDebug() {

		//
		// now, the UserDetailsService is supposed to return a User that has one
		// GrantedAuthority, ROLE_USER
		//
		UserDetails acegiUser = getUserDetailsService()
				.loadUserByUsername(
						"someReallyReallyLongEmailsomeReallyReallyLongEmailsomeReallyReallyLongEmailsomeReallyReallyLongEmailsomeReallyReallyLongEmail@someReallyReallyLongEmailsomeReallyReallyLongEmail.com");
		assertNull("should have returned null", acegiUser);
	}
}

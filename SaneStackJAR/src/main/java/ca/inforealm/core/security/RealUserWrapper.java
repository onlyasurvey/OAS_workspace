package ca.inforealm.core.security;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

import ca.inforealm.core.model.UserAccount;

/**
 * Adds "real user" data (ie., UserAccount) to a UserDetails implementation.
 */
public class RealUserWrapper extends User implements UserDetails {

	/** The wrapped "real" user. */
	private UserAccount realUser;

	/**
	 * A set of session fixations (strings) which the application has published
	 * to the client. When session fixations are asserted they are removed from
	 * this set.
	 */
	private Set<String> sessionFixations;

	// ======================================================================

	public RealUserWrapper(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked, GrantedAuthority[] authorities) {

		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);

		sessionFixations = new HashSet<String>();
	}

	// ======================================================================

	/**
	 * Generate a new session fixation, adding it to the internal set of this
	 * user's security context.
	 * 
	 * @return 32-character MD5 string
	 */
	public String produceSessionFixation() {

		// translate to a predictable length of meaningless characters
		Md5PasswordEncoder encoder = new Md5PasswordEncoder();
		String retval = encoder.encodePassword(generateFixationSourceString(), null);

		// add to internal set
		sessionFixations.add(retval);

		//
		return retval;
	}

	/**
	 * Consume a session fixation, presumably due to it being sent by the client
	 * in a submission of whatever sort. Removes the passed value from the
	 * internal set.
	 * 
	 * @param fixationString
	 */
	public void consumeSessionFixation(String fixationString) {
		// anything unexpected in suspect in this context
		Assert.state(sessionFixations.contains(fixationString), "cannot consume non-existent session fixation");

		//
		sessionFixations.remove(fixationString);
	}

	/**
	 * Key method for generating a source string that produceSessionFixation
	 * uses
	 * 
	 * TODO REVIEW: sourceString algorithm
	 * 
	 * @return Pseudo-random string
	 */
	protected String generateFixationSourceString() {

		// TODO REVIEW: this is a basic algorithm
		return this.toString() + new Date().getTime() + new Random().nextFloat();
	}

	// ======================================================================

	/**
	 * @return the realUser
	 */
	public UserAccount getRealUser() {
		return realUser;
	}

	/**
	 * @param realUser
	 *            the realUser to set
	 */
	public void setRealUser(UserAccount realUser) {
		this.realUser = realUser;
	}

}

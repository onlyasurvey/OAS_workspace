package ca.inforealm.core.util;

import org.springframework.security.providers.dao.salt.SystemWideSaltSource;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;

public final class PasswordUtils {

	private static String key = "SANE_SALT_159";

	/**
	 * Encode a password using the salt defined as this.key.
	 * 
	 * @param password
	 * @return
	 */
	public static String encode(String password) {

		Md5PasswordEncoder encoder = new Md5PasswordEncoder();
		SystemWideSaltSource reflectionSaltSource = new SystemWideSaltSource();

		// reflectionSaltSource.setSystemWideSalt(key);
		Object salt = reflectionSaltSource.getSalt(null);
		String encodedpassword = encoder.encodePassword(password, salt);

		return encodedpassword;

	}

}

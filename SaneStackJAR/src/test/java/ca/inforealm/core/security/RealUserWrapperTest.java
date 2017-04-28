package ca.inforealm.core.security;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.security.GrantedAuthorityImpl;

import ca.inforealm.core.AbstractBaseTest;

public class RealUserWrapperTest extends AbstractBaseTest {
	protected RealUserWrapper newRealUserWrapper() {
		return new RealUserWrapper("someUser", "somePassword", true, true, true, true, new GrantedAuthorityImpl[0]);
	}

	@Test
	public void testSessionFixations_BasicPath() {
		RealUserWrapper user = newRealUserWrapper();

		String fixation = user.produceSessionFixation();

		assertNotNull("fixation should have been returned", fixation);
		assertEquals("expected 32-character MD5 string", 32, fixation.length());

		// should not throw anything
		user.consumeSessionFixation(fixation);
	}

	@Test
	public void testSessionFixations_CannotConsumeTwice() {

		RealUserWrapper user = newRealUserWrapper();

		// add
		String fixation = user.produceSessionFixation();
		// remove
		user.consumeSessionFixation(fixation);

		try {
			// doing this twice is unexpected
			user.consumeSessionFixation(fixation);

			fail("should have thrown");
		} catch (IllegalStateException ise) {
			// expected
		}
	}
}

package ca.inforealm.coreman.service;

import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.service.ActorQueryService;

public class ActorQueryServiceTest extends AbstractSaneManBaseTest {

	@Autowired
	private ActorQueryService actorQueryService;

	public void testQueryUsername_NoResults() {
		createAndSetSecureUserWithRoleUser();
		assertEquals("should have zero matches", 0, actorQueryService.findUserByAny("some random text" + getMBUN()).size());
	}

	public void testQueryUsername_Pass() {
		createAndSetSecureUserWithRoleUser();
		UserAccount user = createTestUser();
		assertEquals("should have 1 match", 1, actorQueryService.findUserByAny(user.getUsername()).size());
	}

	public void testQueryEmail_NoResults() {
		createAndSetSecureUserWithRoleUser();
		assertEquals("should have zero matches", 0, actorQueryService.findUserByAny("some@" + getMBUN()).size());
	}

	public void testQueryEmail_Pass() {
		createAndSetSecureUserWithRoleUser();
		UserAccount user = createTestUser();
		assertEquals("should have 1 match", 1, actorQueryService.findUserByAny(user.getEmail()).size());
	}

}

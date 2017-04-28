package com.oas.command.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;

public class ScaleAnswerProcessorTest extends AbstractOASBaseTest {

	@Test
	public void testProcessAnswer_Success_WithValue() {

		ScaleAnswerProcessor proc = new ScaleAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setAnswer("6");
		// require an answer
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command, true);
		assertNotNull("should return a populated answer object", answer);
		assertEquals("unexpected # answers", 1, answer.size());
	}

	@Test
	public void testProcessAnswer_Success_NoValue() {

		ScaleAnswerProcessor proc = new ScaleAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setAnswer(null);
		// no answer, set not required
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command, false);
		assertNotNull("should return a populated answer object", answer);
		assertEquals("unexpected # answers", 0, answer.size());
	}

	@Test
	public void testProcessAnswer_Fail_InvalidValue() {

		ScaleAnswerProcessor proc = new ScaleAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setAnswer("not a number");

		// validator is supposed to prevent this, so it's an illegal argument
		try {
			AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc, createAndSetSecureUserWithRoleUser(), command, false);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

}

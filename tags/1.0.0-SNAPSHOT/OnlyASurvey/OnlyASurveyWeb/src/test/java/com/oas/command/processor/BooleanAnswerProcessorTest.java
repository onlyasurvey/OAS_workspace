package com.oas.command.processor;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;

public class BooleanAnswerProcessorTest extends AbstractOASBaseTest {

	@Test
	public void testProcessAnswer_Success_True() {
		BooleanAnswerProcessor proc = new BooleanAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA("1");
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertNotNull("should return a populated answer object", answer);
	}

	@Test
	public void testProcessAnswer_Success_False() {
		BooleanAnswerProcessor proc = new BooleanAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA("0");
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertNotNull("should return a populated answer object", answer);
	}

	@Test
	public void testProcessAnswer_Success_Null() {
		BooleanAnswerProcessor proc = new BooleanAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA(null);
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertNotNull("should return a populated answer object", answer);
	}

	@Test
	public void testProcessAnswer_FailOnIllegalValueArgument() {
		BooleanAnswerProcessor proc = new BooleanAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA("bar29");
		try {
			AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc, createAndSetSecureUserWithRoleUser(), command);
			fail("should have thrown exception");
		} catch (RuntimeException e) {
			// expected
		}
	}

}

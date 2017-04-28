package com.oas.command.processor;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;

/**
 * NOTE any value, including null, is accepted because the processor is separate
 * from the validator.
 * 
 * @author xhalliday
 * @since September 13, 2008
 */
public class NoQuestionAnswerProcessorTest extends AbstractOASBaseTest {

	@Test
	public void successPath() {
		TextAnswerProcessor proc = new TextAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertTrue("should never be any answers", answer.isEmpty());
	}

}

package com.oas.command.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
public class TextAnswerProcessorTest extends AbstractOASBaseTest {

	@Test
	public void testProcessAnswer_Success_Text() {
		TextAnswerProcessor proc = new TextAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA("fancyAjax");
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertNotNull("should return a populated answer object", answer);
		assertEquals("unexpected # answers", 1, answer.size());
	}

	@Test
	public void testProcessAnswer_Success_Null() {
		TextAnswerProcessor proc = new TextAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setA(null);
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command);
		assertNotNull("should return a populated answer object", answer);
		assertEquals("unexpected # answers", 0, answer.size());
	}

}

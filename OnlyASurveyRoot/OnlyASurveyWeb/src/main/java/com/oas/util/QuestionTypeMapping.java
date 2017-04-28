package com.oas.util;

import com.oas.command.processor.AnswerProcessor;

/**
 * Utilities relating to question types.
 * 
 * @author xhalliday
 * @since September 8, 2008
 */

public class QuestionTypeMapping {
	private String viewName;
	// private Class<? extends AnswerProcessor> answerProcessor;
	private AnswerProcessor answerProcessor;

	public QuestionTypeMapping(String viewName, AnswerProcessor answerProcessor) {
		// public QuestionTypeMapping(String viewName, Class<? extends
		// AnswerProcessor> answerProcessor) {
		this.viewName = viewName;
		this.answerProcessor = answerProcessor;
	}

	public String getViewName() {
		return viewName;
	}

	public AnswerProcessor getAnswerProcessor() {
		return answerProcessor;
		// try {
		// return answerProcessor.newInstance();
		// } catch (InstantiationException ie) {
		// // TODO more grace
		// throw new RuntimeException(ie);
		// } catch (IllegalAccessException ie) {
		// // TODO more grace
		// throw new RuntimeException(ie);
		// }
	}
}

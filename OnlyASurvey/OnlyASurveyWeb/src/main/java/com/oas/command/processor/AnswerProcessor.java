package com.oas.command.processor;

import java.util.List;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;

/**
 * Defines a Command Processor
 * 
 * @author xhalliday
 * @since September 8, 2008
 */
public interface AnswerProcessor {

	/**
	 * Process the request to add/update Answer(s) to the Question in the
	 * context of the given Response.
	 * 
	 * @param request
	 * @param survey
	 * @param response
	 * @param question
	 */
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command);
}

package com.oas.service;

import java.util.Collection;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

public interface ResponseService extends AbstractServiceInterface {

	public void save(Response response);

	public Response findById(Long id);

	/**
	 * Create a new Response and return it.
	 * 
	 * @param surveyId
	 * @param supportedLanguage
	 * @param ipAddress
	 * 
	 * @return
	 */
	public Response createResponse(Survey survey, SupportedLanguage supportedLanguage, String ipAddress);

	/**
	 * Create or update a list of answers.
	 * 
	 * @param answer
	 * @param response
	 *            The target response. All answers must belong to the response.
	 * @param question
	 *            The target question. All answers must belong to the question.
	 */
	public void saveAnswerList(Collection<Answer> answerList, Response response, Question question);

	/**
	 * Close a response, marking it as complete and preventing further changes.
	 * 
	 * @param response
	 */
	public void closeResponse(Response response);

}

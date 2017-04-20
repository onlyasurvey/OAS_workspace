package com.oas.service;

import java.util.Collection;
import java.util.List;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Answer;
import com.oas.model.Invitation;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ResponseChoiceHistory;
import com.oas.model.ResponseQuestionHistory;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;

/**
 * Service for Responses to Surveys.
 * 
 * @author xhalliday
 * @since October 9, 2008
 */
public interface ResponseService extends AbstractServiceInterface {

	/**
	 * Save a Response.
	 * 
	 * @param response
	 */
	public void save(Response response);

	/**
	 * Create a new Response and return it or return a STARTED but not closed
	 * Response based on {@link Invitation}.
	 * 
	 * @param survey
	 *            The Survey to respond to
	 * @param supportedLanguage
	 *            The language of the Respondent
	 * @param ipAddress
	 *            IP address of the Respondent
	 * @param surveyPassword
	 *            Password entered by the Respondent
	 * @param invitation
	 *            (Optional) {@link Invitation} to respond, tying response to an
	 *            Invitation to allow updating the {@link Invitation}'s status
	 *            later.
	 * 
	 * @return {@link Response}
	 */
	public Response createResponse(Survey survey, SupportedLanguage supportedLanguage, String ipAddress, String surveyPassword,
			Invitation invitation);

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
	 * Load any existing Answer's for the Questions in the Response.
	 * 
	 * @param response
	 * @param question
	 * @return
	 */
	public List<Answer> getAnswerList(Response response, Question question);

	/**
	 * Close a response, marking it as complete and preventing further changes.
	 * This removes all answers that are not in a
	 * {@link ResponseQuestionHistory}.
	 * 
	 * @param response
	 */
	public void closeResponse(Response response);

	// ======================================================================

	/**
	 * Get the Question History for the given Response in the appropriate order.
	 */
	List<ResponseQuestionHistory> getQuestionHistory(Response response);

	/**
	 * Get the Choice History for the given Response and Question in the
	 * appropriate order.
	 * 
	 * @param response
	 * @param question
	 * @return
	 */
	List<ResponseChoiceHistory> getQuestionChoiceHistory(Response response, ChoiceQuestion question);

	/**
	 * Update the Question Answer History list setting the passed question as
	 * the Last Shown.
	 * 
	 * @param response
	 * @param question
	 */
	void setLastShownQuestionHistory(Response response, Question question);

	/**
	 * Truncate the Response Question History to the index specified. This is
	 * for when users use the Browser Back then change their answer and
	 * branching rules make the Next in line irrelevant. It is only done when
	 * the Next Question changes to ensure that Browser Forward works until a
	 * change is made mid-way through the history.
	 * 
	 * @param response
	 *            The response whose question history to modify
	 * @param index
	 *            The index (zero-based) to be the new last element.
	 */
	void truncateQuestionHistory(Response response, int index);

	/**
	 * Determine if the history is currently sitting at the first question.
	 * 
	 * @param response
	 * @return boolean
	 */
	boolean isHistoryAtStart(Response response);

	/**
	 * Get the latest index in the question history - ie, it's size - 1.
	 * 
	 * @param response
	 * @return int
	 */
	int getHighestHistoryPosition(Response response);

	/**
	 * Get the last question that was shown to a user - the end of the Question
	 * History.
	 * 
	 * @param response
	 * @return Question
	 */
	Question getLastShownQuestion(Response response);

	/**
	 * Adds a ResponseQuestionHistory at the end of this list with this
	 * question, making it the Last Shown Question.
	 * 
	 * @param response
	 * @param question
	 */
	void addQuestionToHistory(Response response, Question question);

	// /**
	// * Update the Question Answer History list removing the last item on the
	// * list - ie, going back by one.
	// *
	// * @param response
	// * @param question
	// *
	// * @return The new Last Shown Question
	// */
	// Question backLastShownQuestionHistory(Response response);

	/**
	 * Send an email to a Respondent with a link back to their Response.
	 * 
	 * @param response
	 * @param email
	 * @return
	 */
	boolean sendRespondentLink(Response response, String email);

}

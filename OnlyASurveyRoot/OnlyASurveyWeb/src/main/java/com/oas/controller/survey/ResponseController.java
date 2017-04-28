package com.oas.controller.survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.command.processor.AnswerProcessor;
import com.oas.model.Answer;
import com.oas.model.Attachment;
import com.oas.model.AttachmentPayload;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ResponseChoiceHistory;
import com.oas.model.ResponseQuestionHistory;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.ScaleAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.service.BranchingService;
import com.oas.service.DomainModelService;
import com.oas.util.Constants;
import com.oas.util.Keys;
import com.oas.util.QuestionTypeUtil;
import com.oas.validator.AnswerValidator;

/**
 * Handles respondent answers to a Survey.
 * 
 * @author xhalliday
 */
@Controller
public class ResponseController extends AbstractPublicFacingResponseController {

	private static final String QUESTION_404_FLAG = "404_FLAG";

	private static final String CLOSED_FLAG = "CLOSED_FLAG";

	private static final String NULL_RESPONSE_FLAG = "NULL_RESPONSE_FLAG";

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/** Branching service, for determining next question, skip rules, etc. */
	@Autowired
	private BranchingService branchingService;

	@Autowired
	public ResponseController(AnswerValidator validator) {
		//
		setValidator(validator);

		// validation is invoked manually
		setValidateOnBinding(false);
	}

	// ======================================================================

	/**
	 * Determine if the response can be "found" - it is not null, not deleted,
	 * and it's parent Survey is not deleted.
	 * 
	 * @param response
	 *            {@link Response}
	 * @return boolean
	 */
	private boolean isFound(Response response) {
		if (response == null) {
			return false;
		}
		Survey survey = response.getSurvey();
		return response.isDeleted() == false && survey.isDeleted() == false;
	}

	// ======================================================================

	@RequestMapping("/res/*.html")
	public ModelAndView startResponse(HttpServletRequest request, HttpServletResponse httpResponse) {

		Response response = getEntityFromRestfulUrl(Response.class, request, false);
		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		Survey survey = response.getSurvey();
		Assert.notNull(survey);

		applySurveyTemplateOption(request, httpResponse, survey);

		if (isClosedResponse(response)) {
			return returnClosedResponseMessage(response);
		}

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			//
			return showPausedMessage(survey);
		}

		if (survey.getQuestions().size() == 0) {
			// special case: no questions
			// this is possible by creating a valid survey, publishing it, then
			// deleting it's questions

			// show the General Survey Not Available error page
			applyWideLayout(request);
			return new ModelAndView("/survey/notAvailableGeneral");
		}

		Question firstQuestion = survey.getQuestions().get(0);
		// Question firstQuestion = surveyService.findFirstQuestion(survey);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("response", response);
		model.put("firstQuestion", firstQuestion);
		model.put("welcomeMessage", domainModelService.findObjectText(survey, "welcomeMessage"));

		return new ModelAndView("/response/start", model);
	}

	/**
	 * Show a question ?qId for the response identified by the URL
	 * 
	 * @return
	 */
	@RequestMapping(value = "/res/q/*.html", method = RequestMethod.GET)
	public ModelAndView showQuestion(HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {

		// source data
		Map<String, Object> model = referenceData(request);

		if (isNullResponse(model)) {
			return nullResponse(request);
		}

		if (is404(model)) {
			return return404(model, request);
		}

		Response response = (Response) model.get("response");
		Assert.notNull(response);

		if (isClosedResponse(model)) {
			return returnClosedResponseMessage(response);
		}

		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey);
		Question question = (Question) model.get("question");
		Assert.notNull(question);

		// apply the appropriate look and feel for the survey
		applySurveyTemplateOption(request, httpResponse, survey);

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			return showPausedMessage(survey);
		}

		// add question image payload to request to be used by view
		addQuestionImage(request, question);

		// the appropriate view for the question
		String viewName = QuestionTypeUtil.getViewNameForQuestion(question);

		//
		return new ModelAndView(viewName, model);
	}

	protected void addQuestionImage(HttpServletRequest request, Question question) {
		// image attachment
		Attachment attachment = surveyService.getQuestionImage(question);

		if (attachment == null) {
			// nothing to do
			return;
		}

		// user's language
		SupportedLanguage language = getCurrentSupportedLanguage();

		// get payload for user's language
		AttachmentPayload payload = attachment.getPayloads().get(language);
		if (payload == null) {
			// take the first available as a fallback
			if (attachment.getPayloads().size() > 0) {
				Set<SupportedLanguage> keys = attachment.getPayloads().keySet();
				SupportedLanguage firstKey = keys.iterator().next();
				payload = attachment.getPayloads().get(firstKey);
			}
		}

		// add attribute with this payload, if any
		if (payload != null) {
			request.setAttribute("questionImage", payload);
		}
	}

	/**
	 * Send a link to the respondent via email to resume their survey later.
	 * 
	 * @param request
	 * @return {@link ModelAndView}
	 * @throws Exception
	 */
	@RequestMapping(value = "/res/q/*.html", method = RequestMethod.POST, params = { "_knildnes" })
	public ModelAndView sendSaveLink(HttpServletRequest request, HttpServletResponse httpResponse, String email) throws Exception {

		// serves as both error and success message holder
		Errors errors = new BindException(email, "email");

		// default to error state
		String message = "response.saveAndReturnLater.error.invalidEmail";
		boolean success = false;

		// parse from URL
		Response response = getResponseFromRestfulUrl(request);
		ensureResponseIsValid(response);

		// check email
		if (EmailValidator.getInstance().isValid(email)) {

			// send the email
			success = responseService.sendRespondentLink(response, email);
		}

		if (success) {
			// success!
			message = "response.saveAndReturnLater.emailSent";
			success = true;

		} else {

			// error, as per default values
		}

		errors.reject(message);

		// Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> model = referenceData(request);
		// model.put("response", response);
		model.put("email", email);
		if (success) {
			model.put("statusMessage", message);
		} else {
			model.put("errorMessage", message);
		}

		// apply the appropriate look and feel for the survey
		applySurveyTemplateOption(request, httpResponse, response.getSurvey());
		return new ModelAndView("/response/saveAndReturnLater", model);
	}

	/**
	 * Saves an answer to a given question.
	 * 
	 * TODO method is overly complex
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/res/q/*.html", method = RequestMethod.POST)
	public ModelAndView saveAnswer(HttpServletRequest request, HttpServletResponse httpResponse) throws Exception {

		// determine whether to show the form or redirect
		boolean showForm = false;

		// whether the user selected "Save and return later" which will save
		// answers if they validate, otherwise ignore them and in either case
		// show them a different view
		boolean isSave = false;

		boolean forward = userClickedForward(request);
		boolean back = userClickedBack(request);

		// the FORWARD||BACK assert is failing very rarely in production;
		// possibly an end user's screen reader invoking submit without pressing
		// a button.
		// since both Back and Save and Continue Later are checked herein,
		// therefore the decision is to default to forward.
		if (!(forward || back)) {
			// neither pressed; default to forward
			forward = true;
		}

		//
		Errors errors = null;

		List<Answer> answerList = null;

		// reference data
		Map<String, Object> model = referenceData(request);
		if (is404(model)) {
			return return404(model, request);
		}

		Response response = (Response) model.get("response");
		Assert.notNull(response);

		if (isClosedResponse(model)) {
			return returnClosedResponseMessage(response);
		}

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey);
		Question question = (Question) model.get("question");
		Assert.notNull(question);

		int questionNumber = (Integer) model.get("questionNumber");

		@SuppressWarnings("unchecked")
		List<ResponseQuestionHistory> questionHistory = (List<ResponseQuestionHistory>) model.get("questionHistory");

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			//
			applySurveyTemplateOption(request, httpResponse, survey);
			return showPausedMessage(survey);
		}

		// user requesting to save and return later. BugzID: 43
		if (isSave(request)) {
			isSave = true;
		}

		//
		// if the user clicked back, we abandon any changes here
		//
		if (back) {
			//
			// abandon any answers to this question, pop the last question off
			// the history and redirect: handled below
			//

		} else {

			//
			// MUST be forward or save
			//
			// this assert is failing very rarely in production; possibly an end
			// user's screen reader invoking submit without pressing a button.
			// since both Back and Save and Continue Later are checked herein,
			// therefore the decision is to default to forward.

			// Assert.isTrue(isSave || forward,
			// "either Back or Forward expected; response #" + response.getId()
			// + ", question #" + question.getId());

			// processor of the appropriate type for this question type
			AnswerProcessor processor = QuestionTypeUtil.getAnswerProcessor(question);
			Assert.notNull(processor, "unable to find processor for question type for #" + question.getId());

			// bind answer data
			SimpleAnswerCommand command = formBackingObject(request, question);

			// validation is invoked manually
			ServletRequestDataBinder binder = bindAndValidate(request, command);
			errors = new BindException(binder.getBindingResult());
			// Assert.isTrue(errors.getErrorCount() == 0,
			// "error during base bind: expected no validation errors");
			if (errors.getErrorCount() != 0) {
				String message = "error during base bind: expected no validation errors but got " + errors.getErrorCount() + "; ";
				// @SuppressWarnings("unchecked")
				for (ObjectError error : ((List<ObjectError>) errors.getAllErrors())) {
					message += error.toString() + "; ";
				}
				log.error(message);
				// throw new IllegalArgumentException(message);

			}

			// invoke with required parameter
			((AnswerValidator) getValidator()).validate(command, errors, question);

			if (errors.hasErrors()) {
				// redisplay the form if it had any errors
				if (isSave) {
					// ignore validation, do no save
				} else {
					showForm = true;
				}

			} else {
				// process the answer command to get zero or more answers
				answerList = processor.processAnswer(response.getSurvey(), response, question, command);

				// persist the changes to the answers
				Assert.notNull(answerList);
				responseService.saveAnswerList(answerList, response, question);

				// move history

				showForm = false;
			}
		} // user clicked forward or save

		if (showForm) {
			//
			applySurveyTemplateOption(request, httpResponse, survey);

			// stuff errors
			model.put("errors", errors);

			// show error
			return showForm(request, (BindException) errors, QuestionTypeUtil.getViewNameForQuestion(question), model);

		} else {

			if (isSave) {
				// at this point show them the save view/form.

				applySurveyTemplateOption(request, httpResponse, survey);
				return new ModelAndView("/response/saveAndReturnLater", model);
			}

			// update the history and get the redirect target question
			Question redirectQuestion = updateQuestionHistory(request, question, response, questionHistory, questionNumber);

			// if NEXT or BACK directs to a specific question
			//
			if (forward) {
				if (redirectQuestion == null) {
					// no redirect question - user at beginning or end user
					// clicked Forward - we're at the end of the response
					responseService.closeResponse(response);
				}
			}

			// flow
			String redirectUrl = getRedirectUrl(redirectQuestion, request, response, questionNumber);
			//
			return new ModelAndView(new RedirectView(redirectUrl, true));
		}
	}

	/**
	 * Show the Thank You Message.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/tks/*.html")
	public ModelAndView doThanks(HttpServletRequest request, HttpServletResponse httpResponse) {

		//
		Response response = getResponseFromRestfulUrl(request);
		Assert.notNull(response);

		return doThanks(response, request, httpResponse);
	}

	public ModelAndView doThanks(Response response, HttpServletRequest request, HttpServletResponse httpResponse) {

		Survey survey = response.getSurvey();
		Assert.notNull(survey);

		// change decorator
		applySurveyTemplateOption(request, httpResponse, survey);

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			//
			applySurveyTemplateOption(request, httpResponse, survey);
			return showPausedMessage(survey);
		}

		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("survey", survey);
		model.put("response", response);
		model.put("thanksMessage", domainModelService.findObjectText(survey, "thanksMessage"));

		return new ModelAndView("/response/thanksMessage", model);
	}

	protected void ensureQuestionBelongs(Question question, Survey survey) {
		Assert.notNull(question);
		Assert.notNull(survey);
		Assert.isTrue(question.getSurvey().equals(survey), "wrong survey");
	}

	/**
	 * Based on the request parameter Keys.BACK and Keys.FORWARD, and given the
	 * relative position of question within the survey, determine which question
	 * should be presented to the user next and update the Response Question
	 * History list. Returns the next question to presented.
	 * 
	 * @todo Refactor this into the service; it's request-y but has too much
	 *       logic to be here; pass a FORWARD/BACK enum instead.
	 * 
	 * @param request
	 * @param question
	 * @param response
	 * @param questionHistory
	 * @param questionNumber
	 * @return
	 */
	protected Question updateQuestionHistory(HttpServletRequest request, Question question, Response response,
			List<ResponseQuestionHistory> questionHistory, int questionNumber) {

		Assert.notNull(request);
		Assert.notNull(question);
		Assert.notNull(response);
		Assert.notNull(questionHistory);

		boolean forward = userClickedForward(request);
		boolean back = userClickedBack(request);

		// the FORWARD||BACK assert is failing very rarely in production;
		// possibly an end user's screen reader invoking submit without pressing
		// a button.
		// since both Back and Save and Continue Later are checked herein,
		// therefore the decision is to default to forward.
		if (!(forward || back)) {
			// neither pressed; default to forward
			forward = true;
		}

		int highest = questionHistory.size() - 1;

		if (forward) {

			// determine next question to show
			//
			// move the history forward
			//
			Question retval = branchingService.nextQuestion(question, response);

			if (retval != null) {

				boolean doSetLast = true;

				// is user answering a question in the middle of the
				// Response Question History?
				if (questionNumber < (highest + 1)) {
					// Since they are saving answers
					// (ie, going forward) we truncate the History list if the
					// next question is not equal to the next one in the history
					// - ie, if branching has changed the history.

					// questionNumber is one-based, question history is
					// zero-based, therefore
					// nextIndex = (questionNumber - 1 + 1) = questionNumber
					int nextIndex = questionNumber;
					Question nextInHistory = questionHistory.get(nextIndex).getId().getQuestion();

					if (retval.equals(nextInHistory)) {
						// no change - Browser Forward / Browser History (ie go
						// back to Question 7 after going back to Question 2)
						// works
						doSetLast = false;
					} else {
						// truncate the history to the current one -
						// setLastShownQuestionHistory below will make the
						// history show this question was answered and then the
						// next one was, dropping any previous history that's
						// now changed by branching
						responseService.truncateQuestionHistory(response, questionNumber - 1);
					}
				}

				if (doSetLast) {
					// history only added if there is a "next" question and the
					// user is NOT re-answering something in the middle
					responseService.setLastShownQuestionHistory(response, retval);
				}
			}

			return retval;

		} else if (back) {

			// if user is at the first question then just return null
			if (questionNumber == 1) {
				return null;
			}

			// abandon any answer and pop the last question off the history
			// Question retval =
			// responseService.backLastShownQuestionHistory(response);
			// get one question back in the history
			// -1 to make it zero-based, -1 to go back one
			ResponseQuestionHistory item = questionHistory.get(questionNumber - 1 - 1);
			Assert.notNull(item, "invalid: no history for index");

			Question retval = item.getId().getQuestion();

			return retval;

		} else {
			//
			// should never get here
			//
			final String message = "back or forward expected";
			log.error(message);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Determine the redirect URL to use for a question.
	 * 
	 * @param redirectQuestion
	 * @param request
	 * @param response
	 * @param questionNumber
	 * 
	 * @return Context relative URL.
	 */
	protected String getRedirectUrl(Question redirectQuestion, HttpServletRequest request, Response response, int questionNumber) {

		String redirectUrl = null;

		boolean forward = userClickedForward(request);
		boolean back = userClickedBack(request);

		// the FORWARD||BACK assert is failing very rarely in production;
		// possibly an end user's screen reader invoking submit without pressing
		// a button.
		// since both Back and Save and Continue Later are checked herein,
		// therefore the decision is to default to forward.
		if (!(forward || back)) {
			// neither pressed; default to forward
			forward = true;
		}

		if (redirectQuestion == null) {
			return getWelcomeOrThanksPage(request, response);
		} else {
			// go to appropriate question
			// String params = "?qId=" + redirectQuestion.getId();

			String params = "?n=";
			if (back) {
				//
				params += (questionNumber - 1);
			} else if (forward) {
				//
				params += (questionNumber + 1);
			} else {
				// should never get here due to logic above
				throw new IllegalArgumentException("expected back or forward");
			}

			redirectUrl = "/html/res/q/" + response.getId() + ".html" + params;
		}
		return redirectUrl;
	}

	/**
	 * Get the Welcome or Thanks page URL, depending on the state of the
	 * request.
	 * 
	 * @param request
	 * @param response
	 * @return @return Context relative URL.
	 */
	protected String getWelcomeOrThanksPage(HttpServletRequest request, Response response) {

		boolean forward = userClickedForward(request);
		boolean back = userClickedBack(request);

		// the FORWARD||BACK assert is failing very rarely in production;
		// possibly an end user's screen reader invoking submit without pressing
		// a button.
		// since both Back and Save and Continue Later are checked herein,
		// therefore the decision is to default to forward.
		if (!(forward || back)) {
			// neither pressed; default to forward
			forward = true;
		}

		// either at beginning or end
		if (back) {
			// go BACK from FIRST question TO the WELCOME page
			return "/html/res/" + response.getId() + ".html";
		} else if (forward) {
			// go FORWARD from LAST question TO the THANKS page
			return "/html/tks/" + response.getId() + ".html";
		} else {
			// should never happen due to logic above
			throw new IllegalArgumentException("back or forward submission expected");
		}
	}

	protected boolean userClickedBack(HttpServletRequest request) {
		String param = request.getParameter(Keys.BACK);
		return param != null;
	}

	protected boolean userClickedForward(HttpServletRequest request) {
		String param = request.getParameter(Keys.FORWARD);
		return param != null;
	}

	// ======================================================================

	protected SimpleAnswerCommand formBackingObject(HttpServletRequest request, Question question) {

		Assert.notNull(request);
		Assert.notNull(question);

		SimpleAnswerCommand retval = new SimpleAnswerCommand();

		// need to pre-populate choiceIdList
		if (question.isChoiceQuestion()) {
			// this will load choices from the DB, but so will the view anyway;
			// no efficiency problem
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
			int choiceCount = choiceQuestion.getChoices().size();
			if (question.isAllowOtherText()) {
				choiceCount++;
			}
			retval.setChoiceIdList(new long[choiceCount]);

			// question allows summing?
			if (choiceQuestion.isSummingQuestion()) {

				retval.setSumByChoiceId(new HashMap<Long, Integer>(choiceCount));

				// initialize internal map
				for (Choice choice : choiceQuestion.getChoices()) {
					retval.getSumByChoiceId().put(choice.getId(), 0);
				}

				Assert.isTrue(retval.getSumByChoiceId().size() == choiceCount,
						"defect: number of sum map keys != number of choices");
			}

		} else if (question.isScaleQuestion()) {
			int max = ((ScaleQuestion) question).getMaximum().intValue();
			Assert.isTrue(max < 1000, "sanity check failed: maximumValue ! < 1000");
			long[] data = new long[max];

			for (int i = 0; i < max; i++) {
				data[i] = i + 1;
			}

			retval.setChoiceIdList(data);
		} else if (question.isPageQuestion()) {
			retval.setPageContent(domainModelService.findObjectText(question, Constants.ObjectTextKeys.PAGE_CONTENT));
		}

		return retval;
	}

	private void applyAnswersToCommand(Question question, SimpleAnswerCommand command, List<Answer> answerList) {

		// indicates if a TextAnswer would be a proper answer or "other text"
		// answer
		boolean textIsOther = true;

		// command.g
		if (question.isChoiceQuestion()) {
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;

			if (!choiceQuestion.isSummingQuestion()) {
				//
				// typical multiple-choice question
				//

				int i = 0;
				for (Answer answer : answerList) {
					if (answer.isChoiceAnswer()) {
						ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
						command.getChoiceIdList()[i++] = choiceAnswer.getValue().getId();
					}
				}

				// this is only done for select lists to ensure that a default
				// is not accidentally selected
				if (question.isSelectQuestion()) {
					if (answerList.isEmpty()) {
						command.getChoiceIdList()[0] = -2;
					}
				}

			} else {
				//
				// summing question
				//
				for (Answer answer : answerList) {
					if (answer.isChoiceAnswer()) {
						ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
						command.getSumByChoiceId().put(choiceAnswer.getValue().getId(), choiceAnswer.getSumValue());
					}
				}
			}
		} else if (question.isScaleQuestion()) {
			ScaleAnswer answer = null;
			for (Answer possibleAnswer : answerList) {
				if (possibleAnswer.isScaleAnswer()) {
					answer = (ScaleAnswer) possibleAnswer;
					break;
				}
			}

			if (answer != null) {
				long value = answer.getValue();
				for (long choiceValue : command.getChoiceIdList()) {
					if (choiceValue == value) {
						command.setAnswer(Long.toString(value));
					}
				}
			}
		} else if (question.isTextQuestion()) {
			// sets the real text answer
			textIsOther = false;
		}

		//
		// text answers can apply to most question types
		//
		for (Answer answer : answerList) {
			if (answer.isTextAnswer()) {
				if (textIsOther) {
					// "other" text
					command.setOtherText(answer.getSimpleValue());

					// if it's a choice question, select the "other" option
					if (question.isChoiceQuestion()) {
						int len = command.getChoiceIdList().length;
						// last item will have been initialized in
						// formBackingObject because question.isAllowOtherText
						command.getChoiceIdList()[len - 1] = -1L;
					}

				} else {
					// regular text answer
					command.setAnswer(answer.getSimpleValue());
				}
			}
		}

	}

	protected boolean is404(Map<String, Object> model) {
		Assert.notNull(model);
		return model.containsKey(QUESTION_404_FLAG);
	}

	protected boolean isNullResponse(Map<String, Object> model) {
		Assert.notNull(model);
		return model.containsKey(NULL_RESPONSE_FLAG);
	}

	protected boolean isClosedResponse(Map<String, Object> model) {
		Assert.notNull(model);
		return model.containsKey(CLOSED_FLAG);
	}

	protected boolean isClosedResponse(Response response) {
		Assert.notNull(response);
		return response.isClosed();
	}

	/**
	 * Construct a {@link ModelAndView} showing the user a "no such question"
	 * error.
	 * 
	 * @param model
	 *            Model
	 * @return {@link ModelAndView}
	 */
	protected ModelAndView return404(Map<String, Object> model, HttpServletRequest request) {

		Survey survey = getSurveyFromModel(model);
		Response response = getResponseFromModel(model);

		String message = "Respondent requested question number that does not exist in their history; ";

		if (survey == null) {
			log.error("Unable to get survey from model");
			message += "null survey; ";
		} else {
			message += "survey #" + survey.getId();
		}

		if (response == null) {
			log.error("Unable to get survey from model");
			message += "null response; ";
		} else {
			message += "response #" + response.getId();
		}

		message += "n=" + request.getParameter("n");

		log.error(message);
		return new ModelAndView("/response/noSuchQuestionNumber", model);
	}

	/**
	 * Construct a {@link ModelAndView} telling the user that the response was
	 * not found, that it may be part of a closed survey or have been deleted,
	 * etc.
	 * 
	 * @param model
	 *            Model
	 * @return {@link ModelAndView}
	 */
	protected ModelAndView nullResponse(HttpServletRequest request) {

		String message = "Requested Response does not exist: " + request.getRequestURI();
		log.error(message);
		return new ModelAndView("/response/notFound");
	}

	/**
	 * Construct a {@link ModelAndView} telling the user that the response has
	 * already been closed. Shows the usual Thanks page with an additional note.
	 * 
	 * @param model
	 *            Model
	 * @return {@link ModelAndView}
	 */
	protected ModelAndView returnClosedResponseMessage(Response response) {
		log.error("Respondent attempted to use the Response after it had been closed");

		Survey survey = response.getSurvey();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("response", response);
		model.put("thanksMessage", domainModelService.findObjectText(survey, "thanksMessage"));
		model.put("additionalMessage", "response.responseClosed");

		return new ModelAndView("/response/thanksMessage", model);
	}

	/**
	 * TODO this method is too complex
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {

		// the model
		Map<String, Object> model = new HashMap<String, Object>();

		// parse from URL
		Response response = getResponseFromRestfulUrl(request, false);
		if (response == null) {
			model.put(NULL_RESPONSE_FLAG, "true");
			return model;
		}

		// get the requested survey
		Survey survey = response.getSurvey();

		model.put("survey", survey);
		model.put("response", response);

		if (response.isClosed()) {
			// response has been closed
			model.put(CLOSED_FLAG, "true");
			return model;
		}

		// question number
		Integer questionNumber = null;
		try {
			if (StringUtils.hasText(request.getParameter("n"))) {
				questionNumber = ServletRequestUtils.getIntParameter(request, "n");
			}
		} catch (ServletRequestBindingException e) {
			// swallow exception - handled gracefully below
		}

		//
		List<ResponseQuestionHistory> questionHistoryList = responseService.getQuestionHistory(response);

		if (questionNumber == null) {
			// use last question in history
			questionNumber = questionHistoryList.size();
		}

		if (questionHistoryList.size() < questionNumber || questionNumber < 1) {
			// no such question number in the history - probably clicked forward
			// too many times or is messing with the URL
			model.put(QUESTION_404_FLAG, "true");
			return model;
		}

		Question question = null;
		{
			ResponseQuestionHistory questionHistory = questionHistoryList.get(questionNumber - 1);
			Assert.notNull(questionHistory, "incorrect history parameter");

			// the current question
			question = questionHistory.getId().getQuestion();
			Assert.notNull(question, "no such question");

			// validate question ownership, etc
			ensureQuestionBelongs(question, survey);

			// command
			SimpleAnswerCommand command = formBackingObject(request, question);

			// existing answers to the current question
			List<Answer> answerList = responseService.getAnswerList(response, question);
			applyAnswersToCommand(question, command, answerList);

			// how many questions exist - more efficient than questions.size
			Long numQuestions = new Long(surveyService.countQuestions(survey));
			Assert.notNull(numQuestions);

			//
			model.put("numQuestions", numQuestions);
			model.put("question", question);
			model.put("percentComplete", getPercentComplete(question, numQuestions));
			model.put("answerList", answerList);
			model.put("command", command);
			model.put("questionHistory", questionHistoryList);
		}

		Assert.notNull(question, "unable to determine question");

		if (question.isChoiceQuestion()) {
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;

			// use the history for this, since it will have been initialized
			List<ResponseChoiceHistory> choiceHistory = responseService.getQuestionChoiceHistory(response, choiceQuestion);
			if (choiceHistory == null) {
				String message = "Missing ResponceChoiceHistory for Response #" + response.getId() + ", Question #"
						+ question.getId() + "! falling back to question.getChoices";
				log.error(message);
				throw new RuntimeException(message);
			}

			List<Choice> choiceList = new ArrayList<Choice>(choiceHistory.size());
			for (ResponseChoiceHistory history : choiceHistory) {
				choiceList.add(history.getId().getChoice());
			}

			model.put("choiceList", choiceList);
			// model.put("choiceHistory", choiceHistory);

		}

		// the model
		model.put("questionNumber", questionNumber);
		return model;
	}

	protected void ensureResponseIsValid(Response response) {
		Assert.notNull(response);
		Assert.notNull(response.getId());
	}

	// ======================================================================

	/**
	 * TODO seems dirty
	 * 
	 * @param question
	 * @param numQuestions
	 * @return
	 */
	@SuppressWarnings("cast")
	protected Long getPercentComplete(Question question, Long numQuestions) {
		float nQ = (float) numQuestions;
		float dO = (float) question.getDisplayOrder();
		long pC = (long) ((float) (dO / nQ) * 100);
		Long percentComplete = new Long("" + pC);

		return percentComplete;
	}
}
package com.oas.controller.survey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.command.processor.AnswerProcessor;
import com.oas.controller.AbstractOASController;
import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.service.BranchingService;
import com.oas.service.DomainModelService;
import com.oas.util.Keys;
import com.oas.util.QuestionTypeUtil;
import com.oas.validator.AnswerValidator;

/**
 * Handles respondent answers to a Survey.
 * 
 * @author xhalliday
 */
@Controller
public class ResponseController extends AbstractOASController {

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

	private boolean isFound(Response response) {
		Survey survey = response.getSurvey();
		return response != null && response.isDeleted() == false && survey.isPublished() && survey.isDeleted() == false;
	}

	// ======================================================================

	@RequestMapping("/res/*.html")
	public ModelAndView startResponse(HttpServletRequest request) {

		Response response = getEntityFromRestfulUrl(Response.class, request, false);
		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		Survey survey = response.getSurvey();
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

		applySurveyTemplateOption(request, survey);

		return new ModelAndView("/response/start", model);
	}

	/**
	 * Show a question ?qId for the response identified by the URL
	 * 
	 * @return
	 */
	@RequestMapping(value = "/res/q/*.html", method = RequestMethod.GET)
	public ModelAndView showQuestion(HttpServletRequest request) throws Exception {

		// source data
		Map<String, Object> model = referenceData(request);
		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey);
		Question question = (Question) model.get("question");
		Assert.notNull(question);
		Response response = (Response) model.get("response");
		Assert.notNull(response);

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		// apply the appropriate look and feel for the survey
		applySurveyTemplateOption(request, survey);

		// the appropriate view for the question
		String viewName = QuestionTypeUtil.getViewNameForQuestion(question);

		//
		return new ModelAndView(viewName, model);
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
	public ModelAndView saveAnswer(HttpServletRequest request) throws Exception {

		// sanity
		// assertUserClickedBackOrForward(request);

		// determine whether to show the form or redirect
		boolean showForm = false;

		List<Answer> answerList = null;

		// reference data
		Map<String, Object> model = referenceData(request);
		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey);
		Question question = (Question) model.get("question");
		Assert.notNull(question);
		Response response = (Response) model.get("response");
		Assert.notNull(response);

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		// processor of the appropriate type for this question type
		AnswerProcessor processor = QuestionTypeUtil.getAnswerProcessor(question);
		Assert.notNull(processor, "unable to find processor for question type for #" + question.getId());

		// bind answer data
		Object command = formBackingObject(request, question);
		// validation is invoked manually
		ServletRequestDataBinder binder = bindAndValidate(request, command);
		BindException errors = new BindException(binder.getBindingResult());
		Assert.isTrue(errors.getErrorCount() == 0, "error during base bind: expected no validation errors");

		// invoke with required parameter
		((AnswerValidator) getValidator()).validate(command, errors, question);

		if (errors.hasErrors()) {
			// errors
			if (userClickedForward(request)) {

				// redisplay the form if any errors AND the user is trying to go
				// forward
				showForm = true;
			} else if (userClickedBack(request)) {
				// user clicked back - ignore validation failure and flow to the
				// navigation part below
				showForm = false;
			} else {
				showForm = true;
			}
		} else {
			// process the answer command to get zero or more answers
			answerList = processor.processAnswer(response.getSurvey(), response, question, (SimpleAnswerCommand) command);

			// sanity
			// Assert.notNull(answerList);

			// persist the changes to the answers
			Assert.notNull(answerList);
			responseService.saveAnswerList(answerList, response, question);

			showForm = false;
		}

		if (showForm) {
			applySurveyTemplateOption(request, survey);

			// stuff errors
			model.put("errors", errors);

			// show error
			return showForm(request, errors, QuestionTypeUtil.getViewNameForQuestion(question), model);
		} else {

			// if NEXT or BACK directs to a specific question
			Question redirectQuestion = getRedirectQuestion(request, question, response);

			if (redirectQuestion == null) {
				// no redirect question - user at beginning or end
				if (userClickedForward(request)) {
					// user clicked Forward - we're at the end of the response
					responseService.closeResponse(response);
				}
			}

			// flow
			String redirectUrl = getRedirectUrl(redirectQuestion, request, response);
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
	public ModelAndView doThanks(HttpServletRequest request) {

		//
		Response response = getResponseFromRestfulUrl(request);
		Assert.notNull(response);

		// determine if the response should be Not Found, eg., deleted
		if (!isFound(response)) {
			return new ModelAndView("/response/notFound");
		}

		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("survey", response.getSurvey());
		model.put("response", response);
		model.put("thanksMessage", domainModelService.findObjectText(response.getSurvey(), "thanksMessage"));

		// change decorator
		applySurveyTemplateOption(request, response.getSurvey());

		return new ModelAndView("/thanks/view", model);
	}

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
		}

		return retval;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		// parse from URL
		Response response = getResponseFromRestfulUrl(request);
		ensureResponseIsValid(response);

		// get the requested survey
		Survey survey = response.getSurvey();

		// get the requested question
		Long questionId = getId(request, "qId");
		Question question = surveyService.findQuestionById(questionId);
		Assert.notNull(question);

		// validate question ownership, etc
		ensureQuestionBelongs(question, survey);

		// how many questions exist - more efficient than questions.size
		Long numQuestions = new Long(surveyService.countQuestions(survey));
		Assert.notNull(numQuestions);

		// the model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("response", response);
		model.put("question", question);
		model.put("numQuestions", numQuestions);
		model.put("percentComplete", getPercentComplete(question, numQuestions));
		model.put("command", formBackingObject(request, question));
		return model;
	}

	protected void ensureResponseIsValid(Response response) {
		// TODO validate it's not finalized, etc.
		Assert.notNull(response);
		Assert.notNull(response.getId());
	}

	protected void ensureQuestionBelongs(Question question, Survey survey) {
		Assert.notNull(question);
		Assert.notNull(survey);
		Assert.isTrue(question.getSurvey().equals(survey), "wrong survey");
	}

	/**
	 * Based on the request parameter Keys.BACK and Keys.FORWARD, and given the
	 * relative position of question within the survey, determine which question
	 * should be displayed to the user on redirect.
	 * 
	 * @param request
	 * @param question
	 * @return
	 */
	protected Question getRedirectQuestion(HttpServletRequest request, Question question, Response response) {

		Assert.notNull(request);
		Assert.notNull(question);

		if (userClickedBack(request)) {
			return surveyService.findQuestionBefore(question);
		}

		if (userClickedForward(request)) {
			// return surveyService.findQuestionAfter(question);
			return branchingService.nextQuestion(question, response);
		}

		// no question available
		return null;
	}

	protected String getRedirectUrl(Question redirectQuestion, HttpServletRequest request, Response response) {

		String redirectUrl = null;

		if (redirectQuestion == null) {

			// either at beginning or end
			if (userClickedBack(request)) {
				// go BACK from FIRST question TO the WELCOME page
				redirectUrl = "/html/res/" + response.getId() + ".html";
			} else if (userClickedForward(request)) {
				// go FORWARD from LAST question TO the THANKS page
				redirectUrl = "/html/tks/" + response.getId() + ".html";
			} else {
				throw new IllegalArgumentException("back or forward submission expected");
			}
		} else {
			// go to appropriate question
			redirectUrl = "/html/res/q/" + response.getId() + ".html?qId=" + redirectQuestion.getId();
		}
		return redirectUrl;
	}

	protected void assertUserClickedBackOrForward(HttpServletRequest request) {
		Assert.isTrue(userClickedBack(request) || userClickedForward(request), "back or forward required");
	}

	protected boolean userClickedBack(HttpServletRequest request) {
		String param = request.getParameter(Keys.BACK);
		return param != null;
	}

	protected boolean userClickedForward(HttpServletRequest request) {
		String param = request.getParameter(Keys.FORWARD);
		return param != null;
	}

	/**
	 * TODO seems dirty
	 * 
	 * @param question
	 * @param numQuestions
	 * @return
	 */
	protected Long getPercentComplete(Question question, Long numQuestions) {
		float nQ = (float) numQuestions;
		float dO = (float) question.getDisplayOrder();
		long pC = (long) ((float) (dO / nQ) * 100);
		Long percentComplete = new Long("" + pC);

		return percentComplete;
	}
}
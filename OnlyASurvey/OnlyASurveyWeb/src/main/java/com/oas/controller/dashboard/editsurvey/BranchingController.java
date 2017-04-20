package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.rules.BranchingRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;
import com.oas.security.SecurityAssertions;
import com.oas.service.BranchingService;

/**
 * Allows the user to manage branching options on Questions.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
@Controller
public class BranchingController extends AbstractOASController {

	/** Service that backs up this controller. */
	@Autowired
	private BranchingService branchingService;

	/**
	 * TODO these enums are ugly. They exist because the real enum type is a
	 * String.
	 */
	public enum WHEN {
		OTHER_ANSWER(1), OTHER_EMPTY(2), CHOICE_ON(3), CHOICE_OFF(4), DEFAULT(99);

		final int value;

		WHEN(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return Integer.valueOf(value).toString();
		}
	}

	/**
	 * TODO these enums are ugly. They exist because the real enum type is a
	 * String.
	 */
	public enum WHAT {
		SKIP_QUESTION(1), SHOW_QUESTION(2);

		final int value;

		WHAT(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return Integer.valueOf(value).toString();
		}
	}

	// ======================================================================

	/**
	 * Show all rules currently applied to a Question.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qbr/rm/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView deleteRule(HttpServletRequest request, @RequestParam(value = "_en", required = false) Long entryRuleId,
			@RequestParam(value = "_ex", required = false) Long exitRuleId) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		// the rule
		BranchingRule subject = getRule(entryRuleId, exitRuleId);
		Assert.notNull(subject, "unable to find rule");

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("question", question);
		model.put("subject", subject);

		return new ModelAndView("/dashboard/manage/branching/deleteRule", model);
	}

	protected BranchingRule getRule(Long entryRuleId, Long exitRuleId) {

		if (entryRuleId != null) {
			return branchingService.findEntryRule(entryRuleId);
		} else if (exitRuleId != null) {
			return branchingService.findExitRule(exitRuleId);
		} else {
			throw new IllegalArgumentException("no rule ID specified");
		}
	}

	@RequestMapping(value = "/db/mgt/qbr/rm/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView deleteRuleSubmit(HttpServletRequest request,
			@RequestParam(value = "_en", required = false) Long entryRuleId,
			@RequestParam(value = "_ex", required = false) Long exitRuleId) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		ModelAndView redirectOut = createRedirect(request, "/html/db/mgt/qbr/" + question.getId() + ".html");

		if (isCancel(request)) {
			return redirectOut;
		}

		// the rule
		BranchingRule subject = getRule(entryRuleId, exitRuleId);
		Assert.notNull(subject, "unable to find rule");

		// action
		branchingService.deleteRule(subject);

		return redirectOut;
	}

	// ======================================================================
	//
	// SHOW RULES
	//
	// ======================================================================

	/**
	 * Show all rules currently applied to a Question.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qbr/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView showRules(HttpServletRequest request) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("question", question);
		model.put("entryRules", branchingService.findEntryRules(question));
		model.put("exitRules", branchingService.findExitRules(question));
		model.put("references", branchingService.findReferences(question));

		return new ModelAndView("/dashboard/manage/branching/showRules", model);
	}

	// ======================================================================
	//
	// ENTRY RULE
	//
	// ======================================================================

	/**
	 * Add an "Entry Rule" form.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qbr/aenr/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView addEntryRule(HttpServletRequest request) {
		return addEntryRule(request, null);
	}

	/**
	 * Add an "Entry Rule" form.
	 * 
	 * @param request
	 * @param errors
	 * @return
	 */
	public ModelAndView addEntryRule(HttpServletRequest request, Errors errors) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("question", question);
		model.put("errors", errors);

		return new ModelAndView("/dashboard/manage/branching/addEntryRule", model);
	}

	/**
	 * Add an "Entry Rule" submit handler.
	 * 
	 * @param request
	 * @param wn
	 *            When.
	 * @param wt
	 *            What.
	 * @param wnOp
	 *            When Option - Question ID
	 * @param wnChOp
	 *            When Option - Choice ID
	 * 
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qbr/aenr/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView addEntryRuleSubmit(HttpServletRequest request, @RequestParam(required = false) int wn,
			@RequestParam(required = false) int wt, @RequestParam(required = false) Long wnOp,
			@RequestParam(required = false) Long wnChOp) {
		// , int wn, int wt, int wnOp, int wtOp
		// int wn = 0;
		// int wt = 0;
		// int wnOp = 0;
		// int wnChOp = 0;

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/qbr/" + question.getId() + ".html");
		}

		// basic validation: front-end doesn't allow this condition
		//
		// "|| wnChOp == 0" can be empty if no multiple choice questions exist
		// 
		if (wn == 0 || wt == 0) {
			Errors errors = new BindException(new Object(), "command");
			errors.reject("branching.addEntryRule.error");
			return addEntryRule(request, errors);
		}

		//
		EntryRuleType type = null;
		EntryRuleAction action = null;
		Long otherObjectId = null;

		// what to do
		if (WHAT.SHOW_QUESTION.value == wt) {
			action = EntryRuleAction.SHOW_QUESTION;
		} else if (WHAT.SKIP_QUESTION.value == wt) {
			action = EntryRuleAction.SKIP_QUESTION;
		}

		// when this condition matches
		if (WHEN.OTHER_ANSWER.value == wn) {

			type = EntryRuleType.OTHER_ANSWERED;
			otherObjectId = wnOp;

		} else if (WHEN.OTHER_EMPTY.value == wn) {

			type = EntryRuleType.OTHER_EMPTY;
			otherObjectId = wnOp;

		} else if (WHEN.CHOICE_ON.value == wn) {

			type = EntryRuleType.OTHER_ANSWERED;
			otherObjectId = wnChOp;

		} else if (WHEN.CHOICE_OFF.value == wn) {

			type = EntryRuleType.OTHER_EMPTY;
			otherObjectId = wnChOp;

		} else if (WHEN.DEFAULT.value == wn) {

			type = EntryRuleType.DEFAULT;

		}

		Assert.notNull(action, "illegal action");
		Assert.notNull(type, "illegal type");
		if (!EntryRuleType.DEFAULT.equals(type)) {
			// check unless it's the default rule
			Assert.notNull(otherObjectId, "illegal other ID");
		}

		// persist
		branchingService.createEntryRule(question, type, action, otherObjectId);

		return createRedirect(request, "/html/db/mgt/qbr/" + question.getId() + ".html");
	}

	// ======================================================================
	//
	// EXIT RULE
	//
	// ======================================================================

	@RequestMapping(value = "/db/mgt/qbr/aexr/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView addExitRule(HttpServletRequest request) {
		return addExitRule(request, null);
	}

	public ModelAndView addExitRule(HttpServletRequest request, Errors errors) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("question", question);
		model.put("errors", errors);

		return new ModelAndView("/dashboard/manage/branching/addExitRule", model);
	}

	@RequestMapping(value = "/db/mgt/qbr/aexr/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView addExitRuleSubmit(HttpServletRequest request, String type, String action, Long jumpToQuestionId,
			Long choiceId) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/qbr/" + question.getId() + ".html");
		}

		// basic validation: front-end doesn't allow this condition
		if ((!StringUtils.hasText(type)) || (!StringUtils.hasText(action))) {
			Errors errors = new BindException(new Object(), "command");
			errors.reject("branching.addExitRule.error");
			return addExitRule(request, errors);
		}

		// service does more intensive relationship checks
		branchingService.createExitRule(question, ExitRuleType.valueOf(type), ExitRuleAction.valueOf(action), jumpToQuestionId,
				choiceId);

		//
		return createRedirect(request, "/html/db/mgt/qbr/" + question.getId() + ".html");
	}
	// ======================================================================

}

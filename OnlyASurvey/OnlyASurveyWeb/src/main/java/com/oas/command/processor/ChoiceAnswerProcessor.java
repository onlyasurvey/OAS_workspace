package com.oas.command.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.util.Constants;

public class ChoiceAnswerProcessor extends AbstractAnswerProcessor {

	@Override
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command) {
		// data validation assertions
		super.processAnswer(survey, response, question, command);

		//
		Assert.notNull(survey);
		Assert.notNull(response);
		Assert.notNull(question);
		Assert.notNull(command.getChoiceIdList());
		Assert.isAssignable(ChoiceQuestion.class, question.getClass());

		// get choices mapped by ID
		ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
		Map<Long, Choice> choiceMap = getChoiceMap(choiceQuestion);

		// the real deal
		List<Answer> retval = new ArrayList<Answer>(command.getChoiceIdList().length);

		// if this is a summing question, add sums to answer
		if (choiceQuestion.isSummingQuestion()) {
			for (Choice choice : choiceQuestion.getChoices()) {
				Assert.notNull(command.getSumByChoiceId(), "summing question has no null sum map in answer");

				// MUST have a choice in the question's data, otherwise
				// validator
				// has failed in we are in an invalid state
				Assert.notNull(choice, "invalid choice selected");

				// integrity
				Assert.isTrue(choice.getQuestion().getId().equals(question.getId()), "invalid choice: not part of the question");

				// only add if there's a non-zero value
				Integer sumValue = command.getSumByChoiceId().get(choice.getId());
				if (sumValue != null && sumValue != 0) {
					// set the single choice this entity represents
					ChoiceAnswer answer = new ChoiceAnswer(response, question, choice);
					answer.setSumValue(sumValue);
					retval.add(answer);
				}
			}
		} else {
			// for each specified ID, add the choice to the answer
			for (long choiceId : command.getChoiceIdList()) {

				// ignore -1 - "Other Text"
				if (choiceId == Constants.OTHER_TEXT_ID) {
					continue;
				}

				// ignore -2 - "None of the above"
				if (choiceId == Constants.NONE_OF_THE_ABOVE_ID) {
					continue;
				}

				// get the choice by ID
				Choice choice = choiceMap.get(choiceId);

				// MUST have a choice in the question's data, otherwise
				// validator
				// has failed in we are in an invalid state
				Assert.notNull(choice, "invalid choice selected");

				// integrity
				Assert.isTrue(choice.getQuestion().getId().equals(question.getId()), "invalid choice: not part of the question");

				// set the single choice this entity represents
				ChoiceAnswer answer = new ChoiceAnswer(response, question, choice);

				retval.add(answer);
			}
		}

		// add text answer if Other Text is allowed and present
		if (question.isAllowOtherText() && StringUtils.hasText(command.getOtherText())) {
			retval.add(new TextAnswer(response, question, command.getOtherText()));
		}

		// handle isRequired: validator should prevent user from getting here,
		// so this is an error
		Assert.isTrue((!question.isRequired()) || (question.isRequired() && !CollectionUtils.isEmpty(retval)),
				"invalid input: no answers");

		// check for error: multiple-choice where !unlimited but user specified
		// both a choice and Other Text
		ChoiceQuestion cq = (ChoiceQuestion) question;
		Assert.isTrue(!((!cq.isUnlimited()) && retval.size() > 1),
				"invalid input: got multiple answers where only 1 (or Other Text) is allowed");

		return retval;
	}

	private Map<Long, Choice> getChoiceMap(ChoiceQuestion question) {
		Map<Long, Choice> retval = new HashMap<Long, Choice>();
		for (Choice choice : question.getChoices()) {
			retval.put(choice.getId(), choice);
		}
		return retval;
	}
}

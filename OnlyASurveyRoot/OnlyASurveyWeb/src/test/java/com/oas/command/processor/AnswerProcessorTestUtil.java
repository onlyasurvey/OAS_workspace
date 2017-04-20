package com.oas.command.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.util.Assert;

import ca.inforealm.core.model.Actor;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;

abstract public class AnswerProcessorTestUtil {

	public static Collection<Answer> getAnswerFor(ScenarioDataUtilInterface scenarioDataUtil, AnswerProcessor processor,
			Actor owner, SimpleAnswerCommand command) {
		return getAnswerFor(scenarioDataUtil, processor, owner, command, true);
	}

	public static Collection<Answer> getAnswerFor(ScenarioDataUtilInterface scenarioDataUtil, AnswerProcessor processor,
			Actor owner, SimpleAnswerCommand command, boolean questionRequired) {
		Survey survey = scenarioDataUtil.createTypicalScenario1(owner);
		scenarioDataUtil.persist(survey);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);
		Assert.notNull(response);

		Question question = survey.getQuestions().get(0);
		question.setRequired(questionRequired);
		Assert.notNull(question);

		// invoke the answer processor, producing an Answer model of the
		// appropriate type
		return processor.processAnswer(survey, response, question, command);
	}

	public static List<Answer> getAnswerForChoice(ScenarioDataUtilInterface scenarioDataUtil, AnswerProcessor processor,
			Actor owner, SimpleAnswerCommand command, final int choiceCount, boolean setUnlimited, boolean summing, int sums[]) {
		Survey survey = scenarioDataUtil.createTypicalScenario1(owner);
		scenarioDataUtil.persist(survey);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);
		Assert.notNull(response);

		ChoiceQuestion question = null;
		long idList[] = new long[choiceCount];

		for (Question aQuestion : survey.getQuestions()) {
			if (aQuestion.isChoiceQuestion()) {
				question = (ChoiceQuestion) aQuestion;
				Assert.notEmpty(question.getChoices());
				break;
			}
		}

		Assert.notNull(question, "couldn't find question");

		if (summing) {
			question.setMaximumSum(100);
			command.setSumByChoiceId(new HashMap<Long, Integer>(choiceCount));
		}

		// 
		int addCount = 0;
		for (Choice choice : question.getChoices()) {
			idList[addCount] = choice.getId();

			if (summing) {
				command.getSumByChoiceId().put(choice.getId(), sums[addCount]);
			}

			addCount++;
			if (choiceCount == addCount) {
				break;
			}
		}

		Assert.isTrue(choiceCount == addCount, "couldn't add requested number of choices");

		command.setChoiceIdList(idList);

		Assert.isTrue(choiceCount == addCount, "unable to add desired number of choices to answer: " + addCount + " vs "
				+ choiceCount);

		Assert.notNull(question, "there appears to be no choice questions in the scenario data");
		Assert.notNull(command.getChoiceIdList());
		if (choiceCount > 0) {
			Assert.isTrue(command.getChoiceIdList().length > 0);
		}

		question.setUnlimited(setUnlimited);

		// invoke the answer processor, producing an Answer model of the
		// appropriate type
		return processor.processAnswer(survey, response, question, command);
	}
}

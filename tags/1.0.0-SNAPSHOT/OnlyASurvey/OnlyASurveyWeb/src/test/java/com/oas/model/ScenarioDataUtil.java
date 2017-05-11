package com.oas.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.answer.BooleanAnswer;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.ScaleAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;
import com.oas.model.templating.SurveyLogo.PositionType;
import com.oas.service.SupportedLanguageService;

public class ScenarioDataUtil extends AbstractServiceImpl implements ScenarioDataUtilInterface {

	public final static String QUESTION_1_NAME = "Boolean Question 1";

	public final static String QUESTION_2_NAME = "Text Question 1";

	public final static String QUESTION_4_NAME = "Essay Question 1";

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	@Override
	@Unsecured
	public Survey createTypicalScenario1(Actor owner) {
		return createTypicalScenario1(owner, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.oas.model.ScenarioDataUtilInterface#createTypicalScenario1(ca.gc.
	 * ic.sane.model.Actor)
	 */
	@Unsecured
	public Survey createTypicalScenario1(Actor owner, boolean persist) {
		return createTypicalScenario1(owner, persist, false);
	}

	@Unsecured
	public Survey createTypicalScenario1(Actor owner, boolean persist, boolean published) {

		// by default, scenario 1 data is valid

		Survey survey = new Survey(owner);
		survey.setPublished(published);

		// templating option
		survey.setTemplateOption(SurveyTemplateOption.MAGIC);

		// questions
		Question q1 = new BooleanQuestion(survey);
		q1.addObjectName(supportedLanguageService.findByCode("eng"), QUESTION_1_NAME);
		q1.addObjectName(supportedLanguageService.findByCode("fra"), QUESTION_1_NAME);
		q1.setAllowOtherText(false);
		q1.setDisplayOrder(0L);
		q1.setRequired(true);

		TextQuestion q2 = new TextQuestion(survey);
		q2.addObjectName(supportedLanguageService.findByCode("eng"), QUESTION_2_NAME);
		q2.addObjectName(supportedLanguageService.findByCode("fra"), QUESTION_2_NAME);
		q2.setFieldDisplayLength(40);
		q2.setMaximumLength(50);
		q2.setNumRows(1);
		q2.setAllowOtherText(false);
		q2.setDisplayOrder(1L);
		q2.setRequired(true);

		ChoiceQuestion q3 = new ChoiceQuestion(survey);
		q3.addObjectName(supportedLanguageService.findByCode("eng"), "Choice Question 1");
		q3.addObjectName(supportedLanguageService.findByCode("fra"), "Choice Question 1");
		q3.setAllowOtherText(false);
		q3.setDisplayOrder(2L);
		q3.setRequired(true);

		Choice c1 = new Choice(q3, 0L);
		Choice c2 = new Choice(q3, 1L);
		Choice c3 = new Choice(q3, 2L);
		c1.addObjectName(supportedLanguageService.findByCode("eng"), "Fancy Choice 1");
		c1.addObjectName(supportedLanguageService.findByCode("fra"), "Fancy Choice 1");
		c2.addObjectName(supportedLanguageService.findByCode("eng"), "Fancy Choice 2");
		c2.addObjectName(supportedLanguageService.findByCode("fra"), "Fancy Choice 2");
		c3.addObjectName(supportedLanguageService.findByCode("eng"), "Fancy Choice 3");
		c3.addObjectName(supportedLanguageService.findByCode("fra"), "Fancy Choice 3");
		q3.addChoice(c1);
		q3.addChoice(c2);
		q3.addChoice(c3);

		TextQuestion q4 = new TextQuestion(survey);
		q4.addObjectName(supportedLanguageService.findByCode("eng"), QUESTION_4_NAME);
		q4.addObjectName(supportedLanguageService.findByCode("fra"), QUESTION_4_NAME);
		q4.setFieldDisplayLength(0);
		q4.setMaximumLength(2000);
		q4.setNumRows(8);
		q4.setAllowOtherText(false);
		q4.setDisplayOrder(3L);
		q4.setRequired(true);

		ScaleQuestion q5 = new ScaleQuestion(survey, 1L, 10L, 4L);
		q5.setRequired(true);
		q5.addObjectName(supportedLanguageService.findByCode("eng"), "Scale Question 1");
		q5.addObjectName(supportedLanguageService.findByCode("fra"), "Scale Question 1");

		survey.addQuestion(q1);
		survey.addQuestion(q2);
		survey.addQuestion(q3);
		survey.addQuestion(q4);
		survey.addQuestion(q5);

		Collection<SupportedLanguage> languageList = supportedLanguageService.getSupportedLanguages();
		Assert.isTrue(languageList.size() > 0, "no supported languages");
		for (SupportedLanguage language : languageList) {

			// add support for language
			survey.getSurveyLanguages().add(new SurveyLanguage(survey, language));

			// add name in language
			survey.addObjectName(language, "someTestSurvey");

			// resources per language
			survey.addObjectResource(language, "welcomeMessage", "Welcome to our survey! [" + language.getIso3Lang() + "]");
			survey.addObjectResource(language, "thanksMessage", "Thanks for taking the time! [" + language.getIso3Lang() + "]");
			survey.addObjectResource(language, "pausedMessage", "The survey is paused [" + language.getIso3Lang() + "]");
			survey.addObjectResource(language, "closedMessage", "This survey has been closed. [" + language.getIso3Lang() + "]");

			// template in language
			Template template = new Template(survey, language);
			template.setBaseUrl("http://localhost");
			template.setImportedFromUrl("http://localhost/test.html");
			template.setBeforeContent("<html>");
			template.setAfterContent("</html>");
			template.setTemplateType(TemplateType.OAS_COMMENTS);
			template.setHashAtImport("01234567890123456789012345678901");
			survey.getTemplates().put(language, template);

			// logo images
			SurveyLogo logo = new SurveyLogo(survey, language);
			logo.setWidth(300);
			logo.setHeight(225);
			logo.setSize(13284);
			logo.setContentType("image/jpeg");
			logo.setPayload("fdlskdflskdjflskfd");
			logo.setUploadTime(new Date());
			logo.setPosition(PositionType.LEFT);
			logo.setAltText("Logo Text");

			survey.getLogos().add(logo);
		}

		if (persist) {
			// store the survey
			dataAccessObject.persist(survey);

			// a single, default response
			addDefaultResponse(survey);

			// add default entry/exit rules but only when persist==true
			addDefaultRules(survey);

		}

		// return survey definition
		return survey;
	}

	/**
	 * Adds a single, default response. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 */
	@Override
	@Unsecured
	public Response addDefaultResponse(Survey survey) {

		Assert.notNull(survey, "no survey");
		Assert.notNull(survey.getId(), "cannot call addDefaultResponse on an unsaved Survey");

		Response response = new Response(survey);
		response.setIpAddress("127.0.0.1");
		response.setSupportedLanguage(supportedLanguageService.findByCode("eng"));
		response.addAnswer(new BooleanAnswer(response, survey.getQuestions().get(0), true));
		response.addAnswer(new TextAnswer(response, survey.getQuestions().get(1), "textAnswer"));
		response.addAnswer(new ChoiceAnswer(response, survey.getQuestions().get(2), ((ChoiceQuestion) survey.getQuestions()
				.get(2)).getChoices().get(1)));
		response.addAnswer(new ScaleAnswer(response, survey.getQuestions().get(4), 5L));

		persist(response);

		return response;
	}

	/**
	 * Adds the default scenario rules. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 * 
	 * @param survey
	 */
	@Override
	@Unsecured
	public void addDefaultRules(Survey survey) {

		Assert.notNull(survey, "no survey");
		Assert.notNull(survey.getId(), "cannot call addDefaultRules on an unsaved Survey");

		Question q1 = survey.getQuestions().get(0);
		Question q2 = survey.getQuestions().get(1);
		Question q5 = survey.getQuestions().get(4);

		// question rules
		persist(new EntryRule(q1, null, EntryRuleType.DEFAULT, EntryRuleAction.SHOW_QUESTION, 0));

		persist(new EntryRule(q2, null, EntryRuleType.DEFAULT, EntryRuleAction.SHOW_QUESTION, 0));
		persist(new EntryRule(q2, q1, EntryRuleType.OTHER_ANSWERED, EntryRuleAction.SKIP_QUESTION, 1));

		// forced exit
		persist(new ExitRule(q5, null, null, ExitRuleType.DEFAULT, ExitRuleAction.FORCE_FINISH, 0));
	}

	/**
	 * Add historical response data to a survey, being responses 1 and 2 days
	 * ago, and 1 and 2 months ago.
	 */
	@Unsecured
	public void addHistoricalResponseData(Survey survey) {
		long oneDay = (1000 * 60 * 60 * 24);
		long lotsOfDays = oneDay * 30;
		Date today = new Date();
		Date yesterday = new Date(today.getTime() - oneDay);
		Date dayBefore = new Date(today.getTime() - (oneDay * 2));
		Date monthBefore = new Date(today.getTime() - (lotsOfDays));
		Date monthBeforeThat = new Date(today.getTime() - (lotsOfDays * 2));

		// default data has 1
		// add within a few days of now
		SupportedLanguage language = supportedLanguageService.findByCode("eng");
		persist(new Response(survey, yesterday, language, "127.0.0.1"));
		persist(new Response(survey, dayBefore, language, "127.0.0.1"));

		// add within a few months of now
		persist(new Response(survey, monthBefore, language, "127.0.0.1"));
		persist(new Response(survey, monthBeforeThat, language, "127.0.0.1"));

		// if (persist) {
		// getHibernateTemplate().persist(survey);
		// }
	}

	@Override
	@Unsecured
	public Survey createMonthlyReportTestSurvey(Actor owner) {

		// this month
		Calendar cal1 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal1.add(Calendar.MONTH, 0);

		// 1 month ago
		Calendar cal2 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal2.add(Calendar.MONTH, -1);

		// 2 months ago
		Calendar cal3 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal3.add(Calendar.MONTH, -2);

		// 3 months ago
		Calendar cal4 = DateUtils.truncate(Calendar.getInstance(), Calendar.MONTH);
		cal4.add(Calendar.MONTH, -3);

		return createMonthlyReportTestSurvey(owner, cal1, cal2, cal3, cal4);
	}

	@Override
	@Unsecured
	public Survey createMonthlyReportTestSurvey(Actor owner, Calendar cal1, Calendar cal2, Calendar cal3, Calendar cal4) {

		long displayOrder = 1;
		//
		Survey survey = new Survey(owner);

		TextQuestion textQuestion = new TextQuestion(survey);
		ChoiceQuestion choiceQuestion = new ChoiceQuestion(survey);
		ScaleQuestion scaleQuestion = new ScaleQuestion(survey);

		survey.addQuestion(textQuestion);
		survey.addQuestion(choiceQuestion);
		survey.addQuestion(scaleQuestion);

		//
		textQuestion.setDisplayOrder(displayOrder++);
		textQuestion.setNumRows(1);
		textQuestion.setMaximumLength(100);
		textQuestion.setFieldDisplayLength(25);

		//
		choiceQuestion.setDisplayOrder(displayOrder++);

		Choice choice1 = new Choice(choiceQuestion, 0L);
		Choice choice2 = new Choice(choiceQuestion, 1L);
		Choice choice3 = new Choice(choiceQuestion, 2L);

		choiceQuestion.addChoice(choice1);
		choiceQuestion.addChoice(choice2);
		choiceQuestion.addChoice(choice3);

		//
		scaleQuestion.setDisplayOrder(displayOrder++);
		scaleQuestion.setMinimum(1L);
		scaleQuestion.setMaximum(7L);

		//
		// save
		//
		persist(survey);

		//
		// response
		//
		Response response1 = new Response(survey, cal1.getTime(), supportedLanguageService.findByCode("eng"), "127.0.0.1");
		Response response2 = new Response(survey, cal2.getTime(), supportedLanguageService.findByCode("eng"), "127.0.0.1");
		Response response3 = new Response(survey, cal3.getTime(), supportedLanguageService.findByCode("eng"), "127.0.0.1");
		Response response4 = new Response(survey, cal4.getTime(), supportedLanguageService.findByCode("eng"), "127.0.0.1");

		persist(response1);
		persist(response2);
		persist(response3);
		persist(response4);

		//

		response1.addAnswer(new TextAnswer(response1, textQuestion, "yes"));
		response2.addAnswer(new TextAnswer(response2, textQuestion, "no"));
		response3.addAnswer(new TextAnswer(response3, textQuestion, "maybe"));
		response4.addAnswer(new TextAnswer(response4, textQuestion, "surely"));

		//

		// response 1 - selects 1 choice
		response1.addAnswer(new ChoiceAnswer(response1, choiceQuestion, choice1));
		response1.addAnswer(new TextAnswer(response1, choiceQuestion, "other text"));
		response3.addAnswer(new ScaleAnswer(response1, scaleQuestion, scaleQuestion.getMinimum()));

		// response 2 - selects 2 choices
		response2.addAnswer(new ChoiceAnswer(response2, choiceQuestion, choice1));
		response2.addAnswer(new ChoiceAnswer(response2, choiceQuestion, choice2));
		response2.addAnswer(new ScaleAnswer(response2, scaleQuestion, scaleQuestion.getMaximum()));

		// response 3 - selects 3 choices
		response3.addAnswer(new ChoiceAnswer(response3, choiceQuestion, choice1));
		response3.addAnswer(new ChoiceAnswer(response3, choiceQuestion, choice2));
		response3.addAnswer(new ChoiceAnswer(response3, choiceQuestion, choice3));
		response3.addAnswer(new ScaleAnswer(response3, scaleQuestion, scaleQuestion.getMaximum()));

		// response 4 - selects 1 choice
		response4.addAnswer(new ChoiceAnswer(response4, choiceQuestion, choice1));
		response3.addAnswer(new ScaleAnswer(response4, scaleQuestion, scaleQuestion.getMaximum()));

		return survey;
	}

	@Override
	@Unsecured
	public void createResponseData(Survey survey, int responseCount) {

		SupportedLanguage english = supportedLanguageService.findByCode("eng");
		Collection<Object> additionalSaves = new ArrayList<Object>();

		// test data...
		TextQuestion textQuestion = null;
		for (Question thisQuestion : survey.getQuestions()) {
			if (thisQuestion.isTextQuestion()) {
				textQuestion = (TextQuestion) thisQuestion;
				break;
			}
		}
		Assert.notNull(textQuestion, "passed Survey data does not include a TextQuestion");

		ScaleQuestion scaleQuestion = null;
		for (Question thisQuestion : survey.getQuestions()) {
			if (thisQuestion.isScaleQuestion()) {
				scaleQuestion = (ScaleQuestion) thisQuestion;
				break;
			}
		}
		Assert.notNull(scaleQuestion, "passed Survey data does not include a ScaleQuestion");

		ChoiceQuestion choiceQuestion = null;
		for (Question thisQuestion : survey.getQuestions()) {
			if (thisQuestion.isChoiceQuestion()) {
				choiceQuestion = (ChoiceQuestion) thisQuestion;
				break;
			}
		}
		Assert.notNull(choiceQuestion, "passed Survey data does not include a ChoiceQuestion");
		Choice choice = choiceQuestion.getChoices().get(0);
		Assert.notNull(choice, "passed Survey data does not include a ChoiceQuestion that has Choices");

		for (int i = 0; i < responseCount; i++) {

			Response response = new Response(survey, new Date(), english, "127.0.0.1");
			// survey.addResponse(response);

			additionalSaves.add(response);
			additionalSaves.add(new TextAnswer(response, textQuestion, "some value"));
			additionalSaves.add(new ScaleAnswer(response, scaleQuestion, 1L));
			additionalSaves.add(new ChoiceAnswer(response, choiceQuestion, choice));
		}

		persist(survey);
		getHibernateTemplate().saveOrUpdateAll(additionalSaves);
	}

	@Override
	@Unsecured
	public Response firstResponse(final Survey survey) {
		Response retval = (Response) execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				//
				Criteria crit = session.createCriteria(Response.class);

				crit.add(Restrictions.eq("survey", survey));
				crit.addOrder(Order.desc("id"));
				crit.setMaxResults(1);
				return crit.uniqueResult();
			}
		});

		return retval;
	}
}

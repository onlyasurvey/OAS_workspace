package com.oas.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.apache.commons.validator.EmailValidator;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ResponseChoiceHistory;
import com.oas.model.ResponseQuestionHistory;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.service.DomainModelService;
import com.oas.service.ResponseService;
import com.oas.service.invitations.InvitationService;

/**
 * Service for interacting with surveys.
 * 
 * @author xhalliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ResponseServiceImpl extends AbstractServiceImpl implements ResponseService {

	// /** Core Configuration service. */
	// @Autowired
	// private ConfigurationService configurationService;

	/** Resource message service. */
	@Autowired
	private MessageSource messageSource;

	/** Mail sending service. */
	@Autowired
	private JavaMailSender javaMailSender;

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/** For updating invitation status. */
	@Autowired
	private InvitationService invitationService;

	// ======================================================================

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Response response) {
		Assert.notNull(response);
		persist(response);
	}

	/** {@inheritDoc} */
	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Response createResponse(Survey survey, SupportedLanguage supportedLanguage, String ipAddress, String surveyPassword,
			Invitation invitation) {
		Assert.notNull(survey);

		// for Password Protected Surveys
		if (StringUtils.hasText(survey.getGlobalPassword())) {
			log.error("createResponse called with incorrect password: front-end is supposed to handle this");
			Assert.isTrue(survey.getGlobalPassword().equals(surveyPassword), "incorrect password");
		}

		//
		Response retval = extractResponseFromInvitation(invitation);

		// no existing response?
		if (retval == null || retval.isDeleted()) {
			// start from scratch
			log.debug("creating new response for survey #" + survey.getId());
			retval = new Response(survey, new Date(), supportedLanguage, ipAddress);
			persist(retval);

			// this only happens for a { new Response() }
			log.debug("creating new question history for response #" + retval.getId());
			addQuestionToHistory(retval, survey.getQuestions().get(0));

		} else {
			Assert.isTrue(!retval.isClosed(), "response is already closed");
		}

		// link Invitation
		if (invitation != null) {
			log.debug("Linking invitation #" + invitation.getId() + " with response #" + retval.getId());
			Assert.notNull(invitation, "cannot link null Invite");
			invitation.setResponse(retval);
			invitation.setStatus(InvitationStatusType.STARTED);
			//
			persist(invitation);
		}

		//
		Assert.notNull(retval, "unable to start Response");
		return retval;
	}

	/**
	 * Extract a {@link Response} given the {@link Invitation}. Returns null if
	 * a linked Response is deleted.
	 * 
	 * @param invitation
	 *            {@link Invitation}
	 * @return {@link Response}
	 */
	/* package */Response extractResponseFromInvitation(Invitation invitation) {

		Response retval = null;

		// handle invitation, if present
		if (invitation != null) {
			retval = invitation.getResponse();
		}

		if (retval == null || retval.isDeleted()) {
			//
			// the original response was deleted: this invite is still
			// marked as RESPONDED so just let it go and handle as
			// default case
			// http://redmine.itsonlyasurvey.com/issues/show/110

			return null;
		}

		return retval;
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveAnswerList(Collection<Answer> answerList, Response response, Question question) {

		Assert.notNull(answerList);
		Assert.notNull(response);
		Assert.notNull(question);
		Assert.isTrue(question.getSurvey().getId().equals(response.getSurvey().getId()), "question and response don't match");

		// sanity
		for (Answer answer : answerList) {
			Assert.isTrue(answer.getResponse().getId().equals(response.getId()));
			Assert.isTrue(answer.getQuestion().getId().equals(question.getId()));
		}

		// delete existing
		deleteAnswers(response, question);

		// save any new
		getHibernateTemplate().saveOrUpdateAll(answerList);
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public List<Answer> getAnswerList(Response response, Question question) {
		return find("from Answer where response = ? and question = ?", new Object[] { response, question });
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	private void deleteAnswers(Response response, Question question) {
		// TODO this is inefficient
		getHibernateTemplate().deleteAll(
				find("from Answer where response = ? and question = ?", new Object[] { response, question }));
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void closeResponse(Response response) {

		// mark as closed
		log.debug("closing response #" + response.getId());
		response.closeResponse();
		Assert.isTrue(response.isClosed(), "Response.closeResponse failed to set closed=true");

		// remove answers that are not in the history - ie, that (due to
		// navigation/branching) were once answered but ended up not being part
		// of the response
		// List<ResponseQuestionHistory> history = getQuestionHistory(response);
		List<Question> history = getQuestionsInHistory(response);
		List<Answer> toRemove = new ArrayList<Answer>();

		for (Answer answer : response.getAnswers()) {
			if (history.contains(answer.getQuestion())) {
				// this Answer's Question is in the response history somewhere
			} else {
				// remove from the list of answers
				toRemove.add(answer);
				delete(answer);
			}
		}

		// do the remove
		Assert.isTrue(toRemove.size() <= response.getAnswers().size(), "attempted to remove more answers than exist");
		response.removeAnswers(toRemove);

		// save the changes
		persist(response);

		// update any Invitations to RESPONDED
		// should only ever be one
		List<Invitation> list = find("from Invitation where response = ?", response);
		for (Invitation subject : list) {
			subject.setStatus(InvitationStatusType.RESPONDED);
			persist(subject);
		}
	}

	// ======================================================================

	@Override
	@Unsecured
	public List<ResponseQuestionHistory> getQuestionHistory(Response response) {
		return find("from ResponseQuestionHistory where id.response = ? order by id.order", response);
	}

	/**
	 * Return a list of the questions that are in the complete
	 * ResponseQuestionHistory.
	 * 
	 * @param response
	 *            {@link Response}
	 * @return List<Question>
	 */
	private List<Question> getQuestionsInHistory(Response response) {
		return find("select a.id.question from ResponseQuestionHistory a where a.id.response = ? order by a.id.order", response);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@Unsecured
	public void setLastShownQuestionHistory(Response response, Question question) {

		Assert.notNull(response);
		Assert.notNull(question);

		//
		addQuestionToHistory(response, question);

		// persist(response);
	}

	protected void addCalculatedQuestionChoiceHistory(Response response, ChoiceQuestion question) {

		Assert.notNull(response);
		Assert.notNull(question);

		// remove any existing historical records
		{
			List<ResponseChoiceHistory> toDelete = getQuestionChoiceHistory(response, question);
			log.debug("deleting " + toDelete.size() + " pre-existing Choice History entries");

			for (ResponseChoiceHistory history : toDelete) {
				getHibernateTemplate().delete(history);
			}
		}
		getHibernateTemplate().flush();

		// randomize (or not) depending on Question configuration
		List<Choice> choiceList = randomizeForQuestionChoiceHistory(question);

		// add all choices to a set of ResponseQuestionChoiceHistory objects
		int order = 0;
		for (Choice choice : choiceList) {
			//
			ResponseChoiceHistory history = new ResponseChoiceHistory(response, choice, order);

			//
			persist(history);

			//
			order++;
		}

		log.debug("added " + order + " Choice History entries");
	}

	/**
	 * Randomizes the Choice Question's list of Choices if the Question is
	 * flagged to require it. Otherwise returns question.getChoices().
	 * 
	 * @param question
	 * 
	 * @return List<Choice>
	 */
	protected List<Choice> randomizeForQuestionChoiceHistory(ChoiceQuestion question) {

		List<Choice> choices = question.getChoices();

		if (question.isRandomize()) {

			// copy all in
			List<Choice> retval = new ArrayList<Choice>();
			retval.addAll(choices);

			// randomize
			Collections.shuffle(retval);

			//
			return retval;
		} else {
			// verbatim
			return choices;
		}
	}

	@Override
	@Unsecured
	public List<ResponseChoiceHistory> getQuestionChoiceHistory(Response response, ChoiceQuestion question) {
		return find("from ResponseChoiceHistory where id.response = ? and id.choice.question = ? order by id.order",
		// order by id.order",
				new Object[] { response, question });
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@Unsecured
	public void truncateQuestionHistory(Response response, int index) {

		Assert.isTrue(index >= 0, "negative index not valid");

		List<ResponseQuestionHistory> history = getQuestionHistory(response);
		int length = history.size();

		Assert.isTrue(length > index, "requested to truncate to position out of range");

		// check if it's a request to truncate to the existing last question
		// ie, length = 4 and index = 3, means truncate to 4th item of 4
		if (length == (index + 1)) {
			return;
		}

		// ====================================
		// <DELETE if it's >= 2011>
		// This is the code that existed when #157
		// (http://redmine.itsonlyasurvey.com/redmine/issues/show/157)
		// was reported and fixed. It's kept here for paranoia.

		// where size=2 and index=0
		// (size-1) - index
		// (2-1) - 0 = 1

		// where size=4 and index=2
		// (size-1) - index
		// (4-1)= 3 - 2 = 1

		// where size=10 and index=4
		// (size-1) - index
		// (10 - 1) - 4 = 5
		// for(i=5; i < 10; i++) {
		// int reverseIndex = (length - 1) - index;
		//
		// BEFORE the for() loop:
		// index is zero-based
		// truncate(..., 4) with 10 existing (size 10, maxIndex = 9)
		// for(i = 4 .. 9)
		//
		// </DELETE if it's >= 2011>
		// ====================================

		// where size=4 and index=2
		// rI = 2+1 = 3
		// meaning, delete at index #3, which is item #4
		int reverseIndex = index + 1;

		// for(i = 1 .. 4)
		for (int i = reverseIndex; i < length; i++) {
			// remove(10-1)
			delete(history.get(i));
		}
	}

	@Override
	@Unsecured
	public boolean isHistoryAtStart(Response response) {
		return getQuestionHistory(response).size() < 2;
	}

	@Override
	@Unsecured
	public int getHighestHistoryPosition(Response response) {
		return getQuestionHistory(response).size() - 1;
	}

	@Override
	@Unsecured
	public void addQuestionToHistory(Response response, Question question) {
		int nextOrder = getNextQuestionHistoryOrder(response);
		ResponseQuestionHistory item = new ResponseQuestionHistory(response, question, nextOrder);
		persist(item);

		if (question.isChoiceQuestion()) {
			addCalculatedQuestionChoiceHistory(response, (ChoiceQuestion) question);
		}
	}

	protected int getNextQuestionHistoryOrder(final Response response) {
		ResponseQuestionHistory item = (ResponseQuestionHistory) execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria crit = session.createCriteria(ResponseQuestionHistory.class);
				crit.add(Restrictions.eq("id.response", response));
				crit.addOrder(Order.desc("id.order"));
				crit.setMaxResults(1);

				return crit.uniqueResult();
			}
		});

		//
		if (item == null) {
			return 0;
		} else {
			return item.getId().getOrder() + 1;
		}
	}

	@Override
	@Unsecured
	public Question getLastShownQuestion(Response response) {
		List<ResponseQuestionHistory> list = getQuestionHistory(response);
		Assert.notNull(list);
		int size = list.size();
		if (size > 0) {
			return list.get(size - 1).getId().getQuestion();
		} else {
			return null;
		}
	}

	// @Override
	// @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	// @Unsecured
	// public Question backLastShownQuestionHistory(Response response) {
	//
	// Assert.notNull(response);
	// Assert.notNull(response.getQuestionHistory());
	//
	// //
	// Question retval = null;
	//
	// int size = response.getQuestionHistory().size();
	//
	// // never go "back" past the first entry
	// if (size > 1) {
	// response.getQuestionHistory().remove(size - 1);
	// persist(response);
	// }
	//
	// retval = response.getLastShownQuestion();
	//
	// return retval;
	// }

	// ======================================================================

	@Override
	@Unsecured
	public boolean sendRespondentLink(Response response, String email) {

		Assert.notNull(response);
		Survey survey = response.getSurvey();
		Assert.notNull(survey);

		// front end is responsible for validating this
		Assert.isTrue(EmailValidator.getInstance().isValid(email), "invalid email address");

		boolean retval = false;

		// some extra defense
		String surveyTitle = survey.getDisplayTitle().replace("<", "&lt;");

		//
		String hostname = domainModelService.getPublicHostname();
		Assert.hasText(hostname);

		// link into the question taking stream: will pick up at end of history
		String link = "http://" + hostname + "/oas/html/res/q/" + response.getId() + ".html";

		//
		Locale locale = LocaleContextHolder.getLocale();
		String subject = messageSource.getMessage("response.saveAndReturnLater.emailSubject", new Object[] { surveyTitle },
				surveyTitle, locale);
		String message = messageSource.getMessage("response.saveAndReturnLater.emailTemplate", new Object[] { link }, link,
				locale);
		Assert.hasText(message, "no link email template");

		try {
			MimeMessage mail = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, "utf8");
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(message);

			javaMailSender.send(mail);

			// no exception from send(), things seem OK
			retval = true;

		} catch (Exception e) {
			// special case to catch all exceptions: user needs a pretty
			// response so we just log
			log.error("Error sending Respondent Link email: " + e.getMessage(), e);
			retval = false;
		}

		return retval;
	}
}

package com.oas.service.impl;

import java.util.Collection;
import java.util.Date;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.service.ResponseService;

/**
 * Service for interacting with surveys.
 * 
 * @author Jason Halliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ResponseServiceImpl extends AbstractServiceImpl implements ResponseService {

	@Override
	@Unsecured
	public Response findById(Long id) {
		Assert.notNull(id);
		// TODO SECURITY authorize by owner
		return get(Response.class, id);
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Response response) {
		Assert.notNull(response);
		persist(response);
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Response createResponse(Survey survey, SupportedLanguage supportedLanguage, String ipAddress) {
		Assert.notNull(survey);
		Response retval = new Response(survey, new Date(), supportedLanguage, ipAddress);
		persist(retval);
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

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	private void deleteAnswers(Response response, Question question) {
		// TODO this is inefficient
		getHibernateTemplate().deleteAll(find("from Answer where response = ? and question = ?", new Object[] { response, question }));
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void closeResponse(Response response) {
		log.debug("closing response #" + response.getId());
		response.setClosed(true);
		persist(response);
	}

	// ======================================================================

}

package com.oas.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.command.model.UploadAttachmentForm;
import com.oas.model.Answer;
import com.oas.model.Attachment;
import com.oas.model.AttachmentPayload;
import com.oas.model.Choice;
import com.oas.model.ObjectName;
import com.oas.model.ObjectResource;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.SurveyLanguage;
import com.oas.model.Attachment.AttachmentType;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.PageQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.model.templating.SurveyLogo.PositionType;
import com.oas.model.util.QuestionTypeCode;
import com.oas.model.util.QuestionTypeConstants;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;
import com.oas.util.Constants;

/**
 * Service for interacting with surveys.
 * 
 * @author xhalliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class SurveyServiceImpl extends AbstractServiceImpl implements SurveyService {

	/** Service for general operations on domain objects. */
	@Autowired
	private DomainModelService domainModelService;

	/** Service for language handling. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Survey survey) {
		Assert.notNull(survey);
		SecurityAssertions.assertOwnership(survey);

		// TODO consider a canChange check here, but ensure no publishSurvey
		// functionality uses it

		persist(survey);
	}

	@Override
	@Unsecured
	public boolean isChangeAllowed(Survey survey) {
		return survey.isChangeAllowed();
	}

	@Override
	@Unsecured
	public boolean isPaused(Survey survey) {
		return !survey.isPublished();
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setSurveyLanguages(Survey survey, IdListCommand command) {

		// security check
		SecurityAssertions.assertOwnership(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// sane input?
		Assert.isTrue(supportedLanguageService.isValidIdList(command), "invalid language data");

		Collection<SupportedLanguage> newList = supportedLanguageService.findByIdListCommand(command);
		Collection<SupportedLanguage> toAdd = supportedLanguageService.findByIdListCommand(command);
		Collection<SurveyLanguage> currentLanguages = survey.getSurveyLanguages();
		Collection<SurveyLanguage> toRemove = new ArrayList<SurveyLanguage>();

		for (SurveyLanguage surveyLanguage : currentLanguages) {

			SupportedLanguage supportedLanguage = surveyLanguage.getLanguage();

			if (!newList.contains(supportedLanguage)) {
				// this currently-set language is not in the new list: remove it
				// from survey
				toRemove.add(surveyLanguage);

				// delete the object from the database
				delete(surveyLanguage);
			}

			// this language is already set - remove it from list (if it's there
			// at all) of new items to add
			toAdd.remove(supportedLanguage);
		}

		// remove languages that are no longer set
		currentLanguages.removeAll(toRemove);

		// add languages that were not yet set
		for (SupportedLanguage supportedLanguage : toAdd) {

			SurveyLanguage surveyLanguage = new SurveyLanguage(survey, supportedLanguage);
			currentLanguages.add(surveyLanguage);
		}

		persist(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void applyTemplate(Survey survey, Template template) {

		// only owner can change this
		SecurityAssertions.assertOwnership(survey);

		SupportedLanguage templateLanguage = template.getSupportedLanguage();
		Assert.notNull(templateLanguage, "no language in template");

		Template subject = survey.getTemplates().get(templateLanguage);
		if (subject == null) {
			subject = new Template(survey, templateLanguage);
			survey.getTemplates().put(templateLanguage, subject);
		}

		//
		subject.setBeforeContent(template.getBeforeContent());
		subject.setAfterContent(template.getAfterContent());
		subject.setBaseUrl(template.getBaseUrl());
		subject.setImportedFromUrl(template.getImportedFromUrl());
		subject.setSupportedLanguage(template.getSupportedLanguage());
		subject.setTemplateType(template.getTemplateType());

		// save the template
		// persist(subject);

		// persist the survey
		persist(survey);
	};

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void clearTemplate(Survey survey, Template template) {

		// remove from survey
		survey.getTemplates().remove(template.getSupportedLanguage());

		// delete entirely
		delete(template);

		// save survey change
		persist(survey);
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public SurveyLogo attachLogo(Errors errors, Survey survey, SupportedLanguage language, SurveyLogo.PositionType position,
			String contentType, String altText, byte[] payload) {

		{
			Assert.notNull(errors);
			Assert.notNull(survey);
			Assert.notNull(language);
			Assert.notNull(position);
			Assert.hasText(contentType);

			Assert.notNull(payload);
			Assert.isTrue(payload.length > 0, "image size sanity check failed: no data");
			// sanity: payload < 100MB
			Assert.isTrue(payload.length < 104857600, "image size sanity check failed: too large");

			// should not be getting called if there are any existing errors
			Assert.isTrue(errors.hasErrors() == false, "not attempting to attach logo: errors already exist");

			// current user must own the survey
			SecurityAssertions.assertOwnership(survey);
		}

		boolean addToSurvey = false;

		//
		// the logo to attach
		//
		SurveyLogo retval = getLogoObject(survey, language, position);
		if (retval == null) {
			// create a new logo
			retval = new SurveyLogo(survey, language);
			retval.setPosition(position);
			addToSurvey = true;
		} // else: update existing logo

		// basic details
		retval.setUploadTime(new Date());
		retval.setContentType(contentType);
		retval.setSize(payload.length);
		retval.setAltText(altText.trim());
		// parses image data; sets width, height, and encoded content
		setLogoImageDetails(errors, retval, payload);

		//
		if (errors.hasErrors()) {
			return null;
		} else {
			// all good - relate and persist
			if (addToSurvey) {
				survey.getLogos().add(retval);
			}
			persist(survey);

			return retval;
		}
	}

	private boolean setLogoImageDetails(Errors errors, SurveyLogo logo, byte[] payload) {

		Assert.notNull(logo);
		Assert.notNull(payload);
		Assert.isTrue(payload.length > 0);

		try {
			// parse the image data
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(payload));
			Assert.notNull(image, "unable to process image data");

			int width = image.getWidth();
			int height = image.getHeight();

			// sanity
			Assert.isTrue(width > 0 && width < 5000, "width exceeds max");
			Assert.isTrue(height > 0 && height < 5000, "height exceeds max");

			// details
			logo.setWidth(width);
			logo.setHeight(height);

			// encode content
			logo.setPayload(new String(Base64.encodeBase64(payload)));

		} catch (IllegalArgumentException e) {

			// one of the assertions failed: not valid image data
			errors.reject("surveyService.attachLogo.error.processingImage.unknownImageType");
			return false;

		} catch (IOException e) {
			// not the type of exception that's supposed to occur
			log.error("exception parsing logo image: adding illegalArgument errorCode and returning false", e);
			errors.reject("illegalArgument");
			return false;
		}

		return true;
	}

	private SurveyLogo getLogoObject(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position) {
		//
		Assert.notNull(survey);
		Assert.notNull(language);
		Assert.notNull(position);

		// No security required: respondent code calls this method
		List<SurveyLogo> list = find("from SurveyLogo where survey = ? and supportedLanguage=? and position=?", new Object[] {
				survey, language, position });

		Assert.notNull(list);

		int nr = list.size();
		switch (nr) {
		case 0:
			return null;
		case 1:
			//
			SurveyLogo logo = list.get(0);
			//
			return logo;
		default:
			throw new IllegalArgumentException("expected zero or one objects (got " + nr + ")");
		}
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public byte[] getLogoData(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position) {
		//
		Assert.notNull(survey);
		Assert.notNull(language);
		Assert.notNull(position);

		// No security required: respondent code calls this method
		SurveyLogo logo = getLogoObject(survey, language, position);
		if (logo != null) {
			return Base64.decodeBase64(logo.getPayload().getBytes());
		}

		return null;
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Map<PositionType, SurveyLogo> getLogosForLanguage(Survey survey, SupportedLanguage language) {

		Map<PositionType, SurveyLogo> retval = new HashMap<PositionType, SurveyLogo>();
		List<SurveyLogo> list = find("from SurveyLogo where survey = ? and supportedLanguage=?",
				new Object[] { survey, language });

		for (SurveyLogo logo : list) {
			retval.put(logo.getPosition(), logo);
		}

		return retval;
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public SurveyLogo getLogo(Survey survey, SupportedLanguage language, PositionType position) {

		List<SurveyLogo> list = find("from SurveyLogo where survey = ? and supportedLanguage=? and position=?", new Object[] {
				survey, language, position });

		return unique(list);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void purgeLogo(Survey survey, SupportedLanguage language, PositionType position) {
		//
		Assert.notNull(survey);
		Assert.notNull(language);
		Assert.notNull(position);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		SurveyLogo logo = getLogoObject(survey, language, position);
		if (logo == null) {
			// shouldn't happen...
			log.warn("user attempted to delete logo that doesn't exist: Survey #" + survey.getId() + ", language: " + language
					+ ", position: " + position);

			// ...but not the end of the world
			return;
		}

		survey.getLogos().remove(logo);
		persist(survey);

		delete(logo);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void updateLogoAlt(Survey survey, SupportedLanguage language, PositionType position, String value) {
		//
		Assert.notNull(survey);
		Assert.notNull(language);
		Assert.notNull(position);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		SurveyLogo logo = getLogoObject(survey, language, position);
		if (logo == null) {
			String message = "attempted to update Survey Logo alt text for logo that doesn't exist";
			log.error(message + ": Survey #" + survey.getId() + ", language: " + language + ", position: " + position);
			throw new IllegalArgumentException(message);
		}

		if (StringUtils.hasText(value)) {
			value = value.trim();
		}

		logo.setAltText(value);
		persist(logo);
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void updateQuestionImage(Errors errors, Question question, UploadAttachmentForm command) {

		Assert.notNull(errors);
		Assert.isTrue(!errors.hasErrors(), "cannot process upload with previous errors");
		Assert.notNull(question);
		Assert.notNull(command);
		Assert.hasText(command.getLanguage());

		// security assertion
		SecurityAssertions.assertOwnership(question.getSurvey());

		//
		String altText = command.getAltText();
		MultipartFile file = command.getUpload();
		Assert.isTrue(file != null || StringUtils.hasText(altText));

		//
		SupportedLanguage language = supportedLanguageService.findByCode(command.getLanguage());
		Assert.notNull(language);

		//
		Attachment attachment = findQuestionImageAttachment(question);
		if (attachment == null) {
			// when there's no existing Attachment the user must upload an image
			// - it's not possible to have only altText
			Assert.isTrue(file != null, "cannot set altText without image data");

			attachment = new Attachment(question, AttachmentType.IMAGE, file.getContentType());
		}

		// process and add/update payload
		AttachmentPayload payload = processQuestionImageUpload(errors, attachment, language, file);
		if (!errors.hasErrors()) {
			attachment.getPayloads().put(language, payload);

			// save new or update existing
			domainModelService.attachUpload(question, attachment);
		} else {
			log.warn("Not uploading Question image attachment due to errors: " + errors);
		}

	}

	protected AttachmentPayload processQuestionImageUpload(Errors errors, Attachment attachment, SupportedLanguage language,
			MultipartFile file) {

		Assert.notNull(errors);
		Assert.notNull(attachment);
		Assert.notNull(language);
		Assert.notNull(file);
		Assert.hasText(file.getContentType());
		Assert.isTrue(file.getSize() < 1048576 * 10); // 10MB sanity check

		try {
			AttachmentPayload payload = new AttachmentPayload();
			payload.setUploadTime(new Date());

			// parse the image data
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
			Assert.notNull(image, "unable to process image data");

			int width = image.getWidth();
			int height = image.getHeight();

			// sanity
			Assert.isTrue(width > 0 && width < 5000, "width exceeds max");
			Assert.isTrue(height > 0 && height < 5000, "height exceeds max");

			// todo validate contentType, that it's a valid one and that it
			// matches Attachment (if not null)

			// encode content
			payload.setPayload(new String(Base64.encodeBase64(file.getBytes())));
			return payload;

		} catch (IllegalArgumentException e) {

			// one of the assertions failed: not valid image data
			log.error("exception parsing logo image: adding illegalArgument errorCode and returning false", e);
			errors.reject("surveyService.uploadQuestionImage.error.processingImage.unknownImageType");
			return null;

		} catch (IOException e) {
			// not the type of exception that's supposed to occur
			log.error("exception parsing logo image: adding illegalArgument errorCode and returning false", e);
			errors.reject("illegalArgument");
			return null;
		}

	}

	@Override
	@Unsecured
	public Attachment getQuestionImage(Question question) {
		//
		Assert.notNull(question);
		return findQuestionImageAttachment(question);
	}

	protected Attachment findQuestionImageAttachment(Question question) {

		Assert.notNull(question);

		List<Attachment> list = domainModelService.findAttachments(question, AttachmentType.IMAGE);
		switch (list.size()) {
		case 0:
			return null;
		case 1:
			return list.get(0);
		default:
			throw new IllegalArgumentException("multiple image attachments found");
		}
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteQuestionImage(Question question, SupportedLanguage language) {

		Assert.notNull(question);
		Assert.notNull(language);

		//
		SecurityAssertions.assertOwnership(question.getSurvey());

		Attachment attachment = findQuestionImageAttachment(question);
		if (attachment != null) {
			AttachmentPayload payload = attachment.getPayloads().get(language);
			if (payload != null) {
				attachment.getPayloads().remove(language);
				persist(attachment);
				return;
			}
		}

		log.warn("User #" + getCurrentUser().getId() + " attempted to remove null attachment or payload for Question #"
				+ question.getId());
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setQuestionImageToolTip(Question question, SupportedLanguage language, String value) {

		Assert.notNull(question);
		Assert.notNull(language);
		if (value != null) {
			Assert.isTrue(value.length() < 1024, "sanity check: Tool Tip too large");
		}

		//
		SecurityAssertions.assertOwnership(question.getSurvey());

		Attachment attachment = findQuestionImageAttachment(question);
		if (attachment != null) {
			AttachmentPayload payload = attachment.getPayloads().get(language);
			if (payload != null) {

				payload.setAltText(value);
				persist(attachment);
				return;
			}
		}
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Errors publishSurvey(Survey survey) {

		// MUST be owner
		SecurityAssertions.assertOwnership(survey);

		// MUST be paid for, etc.
		Errors errors = new BindException(survey, "surveyToPublish");

		// TODO fix autowiring of services and re-enable this
		// publishSurveyValidator.validate(survey, errors);

		// will NEVER publish without payment
		if (!survey.isPaidFor()) {
			errors.reject("publish.errors.paymentRequired");
		}

		if (errors.hasErrors()) {
			//
			log.warn("attempted to publish survey that failed ready-to-publish validation: id#" + survey.getId() + " by "
					+ getCurrentUser().getId());
		} else {
			// sanity: should NEVER reach this code if not paid for
			Assert.isTrue(survey.isPaidFor(), "not paid for");

			// at this point, survey data is valid and is paid for: do the deed
			survey.setPublished(true);
			persist(survey);
		}

		return errors;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void unpublishSurvey(Survey survey) {

		// MUST be owner
		// SecurityAssertions.assertOwnership(survey);

		// do the deed
		survey.setPublished(false);
		persist(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteSurvey(Survey survey) {

		log.info("user#" + getCurrentUser().getId() + " is deleting survey#" + survey.getId());

		// params
		Assert.notNull(survey);

		// allowed to change survey?
		// assertSurveyCanChange(survey);

		// security check
		SecurityAssertions.assertOwnership(survey);

		// delete(survey);
		domainModelService.deleteObject(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteResponseData(Survey survey) {

		// parameters
		Assert.notNull(survey);

		// security check
		SecurityAssertions.assertOwnership(survey);

		// for Hibernate callback
		final Long surveyId = survey.getId();

		// for logging
		final int responseCount = countResponses(survey);

		log.info("User#" + getCurrentUser().getId() + " is deleting response data for Survey#" + survey.getId() + " with "
				+ responseCount + " responses");

		if (hasNonDeletedResponses(survey)) {
			execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query query = session.createQuery("update Response set deleted=true where survey.id=?").setParameter(0,
							surveyId);
					query.executeUpdate();
					return null;
				}
			});

			log.info("flushing and clearing any remaining deletion of responses for Survey#" + survey.getId());
			getHibernateTemplate().flush();
			getHibernateTemplate().clear();

			int newCount = countResponses(survey);
			Assert.isTrue(newCount == 0, "unable to fully delete all response data (" + newCount + ")");
		}
	}

	/**
	 * Internally used method to clean up response data to individual questions
	 * - they are purged, not marked as deleted.
	 */
	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void purgeResponseData(Question question) {
		// TODO improve performance here

		{
			// this is manual to ensure the delete includes the parent
			// BaseObject,
			// which is not what happens in a bulk delete query
			int i = 0;
			Collection<Answer> list = find("from Answer where question=?", question);
			for (Answer answer : list) {

				getHibernateTemplate().delete(answer);
				i++;

				if (i % 20 == 0) {
					// flush and clear occasionally to conserve memory
					getHibernateTemplate().flush();
				}
			}
		}

		// flush any remaining
		getHibernateTemplate().flush();
	}

	/**
	 * Internally used method to clean up response data to individual choices -
	 * they are purged, not marked as deleted.
	 */
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	protected void purgeResponseData(Choice choice) {
		// TODO improve performance here

		// this is manual to ensure the delete includes the parent BaseObject,
		// which is not what happens in a bulk delete query
		Collection<ChoiceAnswer> list = find("from ChoiceAnswer where value=?", choice);
		int i = 0;
		for (ChoiceAnswer answer : list) {

			delete(answer);

			i++;

			// if (i % 20 == 0) {
			// // flush and clear occasionally to conserve memory
			// getHibernateTemplate().flush();
			// getHibernateTemplate().clear();
			// }
		}
		// getHibernateTemplate().flush();
		// getHibernateTemplate().clear();
	}

	@Override
	@ValidUser
	public Integer countResponses(Survey survey) {
		Integer retval = null;

		Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(Response.class);
		criteria.add(Restrictions.eq("survey", survey));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.setProjection(Projections.rowCount());

		List list = criteria.list();
		Assert.isTrue(list.size() == 1, "no data returned for count");

		retval = ((Integer) list.get(0));

		Assert.notNull(retval);
		return retval;
	}

	@Override
	@ValidUser
	public Integer countClosedResponses(Survey survey) {
		Integer retval = null;

		Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(Response.class);
		criteria.add(Restrictions.eq("survey", survey));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("closed", true));
		criteria.setProjection(Projections.rowCount());

		List list = criteria.list();
		Assert.isTrue(list.size() == 1, "no data returned for count");

		retval = ((Integer) list.get(0));

		Assert.notNull(retval);
		return retval;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteQuestion(Question question) {

		// params
		Assert.notNull(question);
		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// security check
		SecurityAssertions.assertOwnership(survey);

		// delete response data for this question
		purgeResponseData(question);
		getHibernateTemplate().flush();

		// remove from Survey
		question.getSurvey().getQuestions().remove(question);

		delete(question);

		// flush here to get rid of invalid references to deleted answers from
		// responses
		getHibernateTemplate().flush();

		// compact display order
		// NOTE reloads the object because deleteResponsData() will flush the
		// session
		Survey reloaded = get(Survey.class, survey.getId());
		ensureProperQuestionOrder(reloaded);
		persist(reloaded);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void cloneQuestion(Question question) {

		Assert.notNull(question);
		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// current user MUST own the survey
		SecurityAssertions.assertOwnership(survey);

		Question clone = null;

		// create instance of the appropriate type for the clone
		if (question.isTextQuestion()) {
			clone = new TextQuestion(question.getSurvey());
		} else if (question.isChoiceQuestion()) {
			clone = new ChoiceQuestion(question.getSurvey());
		} else if (question.isBooleanQuestion()) {
			clone = new BooleanQuestion(question.getSurvey());
		} else if (question.isScaleQuestion()) {
			clone = new ScaleQuestion(question.getSurvey());
		} else if (question.isPageQuestion()) {
			clone = new PageQuestion(question.getSurvey());
		}

		Assert.notNull(clone, "unknown question type");

		// set basic properties
		clone.setAllowOtherText(question.isAllowOtherText());
		clone.setRequired(question.isRequired());

		// the last question holds the key to the clone's display order
		Question lastQuestion = findLastQuestion(survey);
		clone.setDisplayOrder(lastQuestion.getDisplayOrder() + 1);

		// clone object names
		for (ObjectName name : question.getObjectNames()) {
			clone.addObjectName(name.getLanguage(), name.getValue());
		}

		// clone resources
		for (ObjectResource resource : question.getObjectResources()) {
			clone.addObjectResource(resource.getSupportedLanguage(), resource.getKey(), resource.getValue());
		}

		// get into the details
		if (question.isTextQuestion()) {

			// TextQuestion-specifics
			TextQuestion source = (TextQuestion) question;
			TextQuestion target = (TextQuestion) clone;

			//
			target.setFieldDisplayLength(source.getFieldDisplayLength());
			target.setNumRows(source.getNumRows());
			target.setMaximumLength(source.getMaximumLength());

		} else if (question.isChoiceQuestion()) {

			// source and target questions
			ChoiceQuestion source = (ChoiceQuestion) question;
			ChoiceQuestion target = (ChoiceQuestion) clone;

			// ChoiceQuestion-specifics
			target.setUnlimited(source.isUnlimited());
			target.setMaximumSum(source.getMaximumSum());
			target.setStyle(source.getStyle());

			// clone choices
			for (Choice sourceChoice : source.getChoices()) {
				Choice choice = new Choice(target, sourceChoice.getDisplayOrder());

				// clone object names
				for (ObjectName name : sourceChoice.getObjectNames()) {
					choice.addObjectName(name.getLanguage(), name.getValue());
				}

				// clone resources
				for (ObjectResource resource : sourceChoice.getObjectResources()) {
					choice.addObjectResource(resource.getSupportedLanguage(), resource.getKey(), resource.getValue());
				}

				target.addChoice(choice);
			}

		} else if (question.isScaleQuestion()) {

			// ScaleQuestion-specifics
			ScaleQuestion source = (ScaleQuestion) question;
			ScaleQuestion target = (ScaleQuestion) clone;

			target.setMinimum(source.getMinimum());
			target.setMaximum(source.getMaximum());

		} else if (question.isBooleanQuestion()) {

			// BooleanQuestion-specifics
			// none

		} else if (question.isPageQuestion()) {

			// PageQuestion-specifics
			PageQuestion source = (PageQuestion) question;
			PageQuestion target = (PageQuestion) clone;

			target.setShowBack(source.isShowBack());
			target.setShowForward(source.isShowForward());
		}

		// add it
		survey.addQuestion(clone);

		// compact display order
		ensureProperQuestionOrder(survey);

		// persist changes
		persist(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public boolean hasNonDeletedResponses(final Survey survey) {
		boolean retval;

		Collection<Object> list = find("select count(r) from Response r where r.survey = ? and r.deleted = false", survey);
		Assert.notNull(list, "query failed");
		Assert.isTrue(list.size() == 1, "expected 1 row");
		Long count = (Long) list.iterator().next();

		retval = (count > 0);

		return retval;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void confirmPayment(Survey survey) {

		Assert.notNull(survey);

		// MUST be owner
		SecurityAssertions.assertOwnership(survey);

		// calling on a paid-for survey is not valid
		Assert.isTrue(!survey.isPaidFor());

		log.info("survey #" + survey.getId() + " was paid for.");

		// change
		survey.setPaidFor(true);

		// persist
		persist(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Question addQuestion(final Survey survey, final CreateQuestionCommand command) {

		// input must be valid
		Assert.notNull(survey);
		Assert.notNull(command);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// create question of appropriate type and delegate
		Question retval = updateQuestion(survey, command, buildDefaultQuestionForCommand(survey, command));
		Assert.notNull(retval, "unable to create question");

		if (retval.isChoiceQuestion()) {
			// if this is a choice question, add the choices from the command
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) retval;
			addChoicesFromCommandForNewQuestion(choiceQuestion, command);

		}

		return retval;
	}

	private void addChoicesFromCommandForNewQuestion(ChoiceQuestion choiceQuestion, CreateQuestionCommand command) {
		//
		Map<String, SupportedLanguage> languageMap = supportedLanguageService.getSupportedLanguageMap();

		// add choices
		long displayOrder = 0;
		for (ChoiceCommand userChoice : command.getChoiceList()) {

			// caller MUST always fill out all commands that are in the list
			Assert.notNull(userChoice);
			Assert.notNull(userChoice.getMap());

			boolean anyNull = false;
			boolean allNull = true;

			for (String key : userChoice.getMap().keySet()) {
				String name = userChoice.getMap().get(key);
				if (name == null || "".equals(name)) {
					anyNull = true;
				} else {
					allNull = false;
				}
			}

			// TODO all this isNull business is dirty
			if (!allNull) {

				// invalid: validator needs to handle this
				Assert.isTrue(!anyNull, "one or more names were null");

				Choice persistentChoice = new Choice(choiceQuestion, displayOrder++);

				for (String key : userChoice.getMap().keySet()) {
					persistentChoice.addObjectName(languageMap.get(key), userChoice.getMap().get(key));
				}

				choiceQuestion.addChoice(persistentChoice);
			}
		}

		// re-persist to include local changes
		persist(choiceQuestion);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Question updateQuestion(Survey survey, final CreateQuestionCommand command, Question retval) {

		// input must be valid
		Assert.notNull(survey);
		Assert.notNull(command);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// current user must own survey
		SecurityAssertions.assertOwnership(survey);

		// addQuestion() delegates here, determine if this is a new question or
		// not
		boolean isNewQuestion = retval.getId() == null;

		//
		Map<String, SupportedLanguage> languageMap = supportedLanguageService.getSupportedLanguageMap();

		// common properties copied from command
		retval.setRequired(command.isRequired());
		retval.setAllowOtherText(command.isAllowOtherText());

		//
		if (retval.isTextQuestion()) {
			TextQuestion subject = (TextQuestion) retval;
			subject.setNumRows(command.getNumRows());
			subject.setMaximumLength(command.getMaximumLength());
			subject.setFieldDisplayLength(command.getFieldDisplayLength());
		}

		//
		if (retval.isScaleQuestion()) {
			ScaleQuestion subject = (ScaleQuestion) retval;
			// subject.setMinimum(command.getMinimum());
			// enforce minimum=1 constant
			subject.setMinimum(1L);
			subject.setMaximum(command.getMaximum());
		}

		if (retval.isPageQuestion()) {

			// add the i18n text for the page content
			PageQuestion pq = (PageQuestion) retval;

			ObjectTextCommand content = command.getPageContent();

			// add text
			for (String langCode : content.getMap().keySet()) {
				pq.addObjectResource(supportedLanguageService.findByCode(langCode), Constants.ObjectTextKeys.PAGE_CONTENT,
						content.getMap().get(langCode));
			}

		}

		//
		if (retval.isChoiceQuestion()) {
			// && command.getChoiceList() != null

			ChoiceQuestion choiceQuestion = (ChoiceQuestion) retval;
			choiceQuestion.setRandomize(command.isRandomizeChoices());

			// if user changes the type, update the related flags
			if (QuestionTypeCode.CHECKBOX.equals(command.getTypeCode())) {
				choiceQuestion.setUnlimited(true);
			}
			if (QuestionTypeCode.RADIO.equals(command.getTypeCode())) {
				choiceQuestion.setUnlimited(false);
			}
			if (QuestionTypeCode.SELECT.equals(command.getTypeCode())) {
				choiceQuestion.setUnlimited(false);
			}
			if (QuestionTypeCode.CONSTANT_SUM.equals(command.getTypeCode())) {
				choiceQuestion.setUnlimited(true);
				// choiceQuestion.setMaximumSum(100);
			}
		}

		// map i18n titles
		Map<String, String> names = command.getMap();
		for (String language : names.keySet()) {
			SupportedLanguage supportedLanguage = languageMap.get(language);
			Assert.notNull(supportedLanguage, "invalid language");

			String value = names.get(language);

			// must be a survey language; front-end must never allow user to
			// send an invalid value here, so this is invalid state
			// DELETE ME: this can happen when a user disables a language
			// Assert.state(surveyLanguages.contains(supportedLanguage),
			// "not a survey language");

			// set the name
			retval.setObjectName(supportedLanguage, value);
		}

		// display order
		if (isNewQuestion) {
			// TODO inefficient
			Question lastQuestion = findLastQuestion(survey);

			if (lastQuestion == null) {
				// first new question
				retval.setDisplayOrder(1L);
			} else {
				Long highestDisplayOrder = lastQuestion.getDisplayOrder();
				Assert.notNull(highestDisplayOrder, "invalid data: no highest display order");
				retval.setDisplayOrder(highestDisplayOrder + 1L);
			}
		}

		if (isNewQuestion) {
			// link the new question back to the survey
			survey.addQuestion(retval);
		}

		// persist the change
		persist(retval);

		//
		return retval;
	}

	private void moveQuestion(Survey survey, Question question, DIRECTION direction) {

		Assert.notNull(survey);
		Assert.notNull(question);
		Assert.notNull(direction);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		//
		List<Question> list = survey.getQuestions();
		int index = list.indexOf(question);
		int siblingIndex = -500;
		Assert.isTrue(index != -1, "question is not in survey");

		if (direction == DIRECTION.UP && index == 0) {
			// can't go up past first element
			return;
		}

		if (direction == DIRECTION.DOWN && (index + 1) == list.size()) {
			// can't go down past last element
			return;
		}

		Question sibling = null;
		if (direction == DIRECTION.UP) {
			siblingIndex = index - 1;
		}

		if (direction == DIRECTION.DOWN) {
			siblingIndex = index + 1;
		}

		sibling = list.get(siblingIndex);
		Assert.notNull(sibling, "no sibling: unhandled case");

		// re-order via displayOrder
		long newQuestionOrder = sibling.getDisplayOrder();
		long newSiblingOrder = question.getDisplayOrder();

		// re-order in the list
		survey.getQuestions().set(index, sibling);
		survey.getQuestions().set(siblingIndex, question);

		question.setDisplayOrder(newQuestionOrder);
		sibling.setDisplayOrder(newSiblingOrder);

		// compact display order
		ensureProperQuestionOrder(survey);

		// persist
		persist(survey);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void moveQuestionUp(Survey survey, Question question) {
		moveQuestion(survey, question, DIRECTION.UP);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void moveQuestionDown(Survey survey, Question question) {
		moveQuestion(survey, question, DIRECTION.DOWN);
	}

	@Override
	@Unsecured
	public Integer countQuestions(Survey survey) {
		Integer retval = null;

		Criteria criteria = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(Question.class);
		criteria.add(Restrictions.eq("survey", survey));
		criteria.setProjection(Projections.rowCount());

		List list = criteria.list();
		Assert.isTrue(list.size() == 1, "no data returned for count");

		retval = ((Integer) list.get(0));

		Assert.notNull(retval);
		return retval;
	}

	protected void ensureProperQuestionOrder(Survey survey) {

		long displayOrder = 0L;

		for (Question question : survey.getQuestions()) {
			Assert.isTrue(question.getDisplayOrder() >= displayOrder, "list out of order");
			question.setDisplayOrder(displayOrder++);
		}
	}

	protected Question buildDefaultQuestionForCommand(Survey survey, CreateQuestionCommand command) {
		//
		Question retval = null;

		if (QuestionTypeCode.BOOLEAN.equals(command.getTypeCode())) {
			//
			retval = new BooleanQuestion(survey);

		} else if (QuestionTypeCode.TEXT.equals(command.getTypeCode())) {
			//
			retval = new TextQuestion(survey);
			TextQuestion cast = (TextQuestion) retval;
			cast.setFieldDisplayLength(command.getFieldDisplayLength());
			cast.setMaximumLength(command.getMaximumLength());
			cast.setNumRows(1);

		} else if (QuestionTypeCode.ESSAY.equals(command.getTypeCode())) {
			//
			retval = new TextQuestion(survey);
			TextQuestion cast = (TextQuestion) retval;
			cast.setFieldDisplayLength(0);
			cast.setMaximumLength(command.getMaximumLength());
			cast.setNumRows(command.getNumRows());

		} else if (QuestionTypeCode.RADIO.equals(command.getTypeCode())) {
			//
			retval = new ChoiceQuestion(survey);
			((ChoiceQuestion) retval).setUnlimited(false);

		} else if (QuestionTypeCode.CHECKBOX.equals(command.getTypeCode())) {
			//
			retval = new ChoiceQuestion(survey);
			((ChoiceQuestion) retval).setUnlimited(true);

		} else if (QuestionTypeCode.SELECT.equals(command.getTypeCode())) {
			//
			retval = new ChoiceQuestion(survey);
			((ChoiceQuestion) retval).setUnlimited(false);
			((ChoiceQuestion) retval).setStyle(QuestionTypeConstants.STYLE_SELECT);

		} else if (QuestionTypeCode.SCALE.equals(command.getTypeCode())) {
			//
			retval = new ScaleQuestion(survey);
			// if (command.getMinimum() == null) {
			((ScaleQuestion) retval).setMinimum(1L);
			// } else {
			// ((ScaleQuestion) retval).setMinimum(command.getMinimum());
			// }
			((ScaleQuestion) retval).setMaximum(command.getMaximum());

		} else if (QuestionTypeCode.CONSTANT_SUM.equals(command.getTypeCode())) {
			//
			retval = new ChoiceQuestion(survey);
			((ChoiceQuestion) retval).setUnlimited(false);
			// NOTE hard-coded to 100 maximum
			((ChoiceQuestion) retval).setMaximumSum(100);
		} else if (command.isPageType()) {
			//
			retval = new PageQuestion(survey);

			//
			PageQuestion pq = (PageQuestion) retval;
			pq.setShowBack(command.isShowBack());
			pq.setShowForward(command.isShowForward());
		}

		//
		if (retval == null) {
			log.error("unable to build command for unknown question type: " + command.getTypeCode());
			Assert.notNull(retval, "unknown question type");
		}

		return retval;
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Choice addChoice(ChoiceQuestion question, NameObjectCommand command) {

		Assert.notNull(question);

		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		Choice choice = new Choice(question, getNextChoiceDisplayOrder(question));
		question.getChoices().add(choice);

		for (String languageCode : command.getMap().keySet()) {
			choice.addObjectName(supportedLanguageService.findByCode(languageCode), command.getMap().get(languageCode));
		}

		// save changes
		compactChoiceDisplayOrder(question);
		persist(question);

		return choice;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void addManyChoices(ChoiceQuestion question, List<ChoiceCommand> choiceList) {

		Assert.notNull(question);

		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// iterate over each possible addition
		for (NameObjectCommand command : choiceList) {

			// has text in ANY language
			if (command.hasText()) {
				Choice choice = new Choice(question, getNextChoiceDisplayOrder(question));
				question.getChoices().add(choice);

				for (String languageCode : command.getMap().keySet()) {
					String value = command.getMap().get(languageCode);

					// this method does not add blank values: whether or not
					// that is valid is up to validators and controllers
					if (StringUtils.hasText(value)) {
						choice.addObjectName(supportedLanguageService.findByCode(languageCode), value);
					}
				}
			}
		}

		// save changes
		compactChoiceDisplayOrder(question);
		persist(question);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void updateChoice(ChoiceQuestion question, Choice choice, NameObjectCommand command) {

		Assert.notNull(question);

		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// sanity
		Assert.isTrue(choice.getQuestion().getId().equals(question.getId()), "cannot change choice: not for same question");

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		for (String languageCode : command.getMap().keySet()) {
			choice.setObjectName(supportedLanguageService.findByCode(languageCode), command.getMap().get(languageCode));
		}

		// save changes
		compactChoiceDisplayOrder(question);
		persist(question);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void moveChoiceDown(Choice choice) {
		Assert.notNull(choice);
		ChoiceQuestion question = choice.getQuestion();
		Assert.notNull(question);

		moveChoice(question, choice, DIRECTION.DOWN);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void moveChoiceUp(Choice choice) {
		Assert.notNull(choice);
		ChoiceQuestion question = choice.getQuestion();
		Assert.notNull(question);

		moveChoice(question, choice, DIRECTION.UP);
	}

	private void moveChoice(ChoiceQuestion question, Choice choice, DIRECTION direction) {

		Assert.isTrue(direction == DIRECTION.UP || direction == DIRECTION.DOWN);
		Assert.notNull(choice);
		Assert.notNull(direction);
		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		List<Choice> choices = question.getChoices();

		int oldIndex = choices.indexOf(choice);
		int newIndex = -1;
		Assert.isTrue(oldIndex != -1, "choice is not in question");

		if (direction == DIRECTION.DOWN) {
			// if index is at end of list then nothing to do
			if (oldIndex == choices.size() - 1) {
				return;
			}

			// move down
			newIndex = oldIndex + 1;

		} else if (direction == DIRECTION.UP) {
			// if index is zero then already at top and nothing to do
			if (oldIndex == 0) {
				return;
			}

			// move up
			newIndex = oldIndex - 1;
		}

		// sanity/regression
		Assert.isTrue(newIndex != -1, "direction was not handled: no new index");
		Assert.isTrue(newIndex != oldIndex, "direction was not handled: new index same as old index");

		Choice otherChoice = choices.get(newIndex);
		Assert.notNull(otherChoice);

		Long tempOrder = choice.getDisplayOrder();
		choice.setDisplayOrder(otherChoice.getDisplayOrder());
		otherChoice.setDisplayOrder(tempOrder);

		choices.set(oldIndex, otherChoice);
		choices.set(newIndex, choice);

		// save changes
		persist(question);
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteChoice(Choice choice) {

		// parameters
		Assert.notNull(choice);
		ChoiceQuestion question = choice.getQuestion();
		Assert.notNull(question);
		Survey survey = question.getSurvey();
		Assert.notNull(survey);

		// allowed to change survey?
		assertSurveyCanChange(survey);

		// security check
		SecurityAssertions.assertOwnership(survey);

		// delete response data for this question
		purgeResponseData(choice);

		// remove from Survey and reorder
		question.getChoices().remove(choice);
		// compact display order
		compactChoiceDisplayOrder(question);

		delete(choice);

		// flush here to get rid of invalid references to deleted answers from
		// responses
		getHibernateTemplate().flush();

		persist(survey);

	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Choice cloneChoice(Choice choice) {
		ChoiceQuestion question = choice.getQuestion();

		Choice clone = new Choice(question, getNextChoiceDisplayOrder(question));
		for (ObjectName name : choice.getObjectNames()) {
			clone.addObjectName(name.getLanguage(), name.getValue());
		}

		// save clone
		question.addChoice(clone);
		compactChoiceDisplayOrder(question);

		// persist changes
		persist(clone);
		persist(question);

		return choice;
	}

	/**
	 * Ensure all displayOrder properties are from [0..n] where n is the size of
	 * the Choice list - 1.
	 * 
	 * @param question
	 */
	protected void compactChoiceDisplayOrder(ChoiceQuestion question) {
		// long displayOrder = 0L;

		for (int i = 0; i < question.getChoices().size(); i++) {
			// Assert.isTrue(choice.getDisplayOrder() >= displayOrder,
			// "list out of order");
			// choice.setDisplayOrder(displayOrder++);
			question.getChoices().get(i).setDisplayOrder(Long.valueOf(i));
		}
	}

	/**
	 * Get the next highest display order for a ChoiceQuestion's choices,
	 * according to choice.displayOrder.
	 * 
	 * @param question
	 * @return
	 */
	protected Long getNextChoiceDisplayOrder(ChoiceQuestion question) {
		Long highest = null;
		for (Choice choice : question.getChoices()) {
			if (highest == null || choice.getDisplayOrder() > highest) {
				highest = choice.getDisplayOrder();
			}
		}

		return highest;
	}

	// ======================================================================

	@Override
	@Unsecured
	public Survey findNonDeletedSurvey(Long id) {
		Assert.notNull(id, "no ID specified to find");
		Survey retval = get(Survey.class, id);
		if (retval != null) {
			if (retval.isDeleted()) {
				return null;
			}
		}
		return retval;
	}

	// ======================================================================

	@Override
	@Unsecured
	public Question findQuestionById(Long id) {
		Assert.notNull(id);
		return get(Question.class, id);
	}

	@Override
	@Unsecured
	public Question findFirstQuestion(Survey survey) {

		Assert.notNull(survey);

		// TODO this is inefficient: it loads ALL questions just to get the
		// first
		return survey.getQuestions().iterator().next();
	}

	/**
	 * Find the sequentially last question, i.e., without any branching rules
	 * taken into consideration.
	 * 
	 * @param question
	 *            Question
	 * @return Question or null
	 */
	@Override
	@Unsecured
	public Question findLastQuestion(Survey survey) {

		Assert.notNull(survey);

		Question retval = null;

		// TODO test that displayOrder is respected

		int size = survey.getQuestions().size();
		if (size > 0) {
			retval = survey.getQuestions().get(size - 1);
		}

		return retval;
	}

	/**
	 * Find the question that sequentially precedes the one passed, i.e.,
	 * without any branching rules taken into consideration.
	 * 
	 * @param question
	 *            Question
	 * @return Question or null
	 */
	@Override
	@Unsecured
	public Question findQuestionBefore(Question question) {

		int index = question.getSurvey().getQuestions().indexOf(question);
		if (index > 0) {
			return question.getSurvey().getQuestions().get(index - 1);
		} else {
			return null;
		}
	}

	/**
	 * Find the question that sequentially follows after the one passed, i.e.,
	 * without any branching rules taken into consideration.
	 * 
	 * @param question
	 *            Question
	 * @return Question or null
	 */
	@Override
	@Unsecured
	public Question findQuestionAfter(Question question) {

		int length = question.getSurvey().getQuestions().size();
		int index = question.getSurvey().getQuestions().indexOf(question);
		// if( 20 > 19 + 1)
		if (length > index + 1) {
			return question.getSurvey().getQuestions().get(index + 1);
		} else {
			return null;
		}
	}

	// ======================================================================

	/**
	 * Throw an IllegalArgumentException if the Survey is not currently allowed
	 * to change (isChangeAllowed()).
	 */
	private void assertSurveyCanChange(Survey survey) {
		Assert.isTrue(survey.isChangeAllowed(), "change is not allowed");
	}

}

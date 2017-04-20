package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.UploadAttachmentForm;
import com.oas.controller.AbstractOASController;
import com.oas.model.Attachment;
import com.oas.model.AttachmentPayload;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;

/**
 * Allows the user to manage image attachments to a Question.
 * 
 * @author xhalliday
 * @since March 24, 2009
 */
@Controller
public class QuestionImageController extends AbstractOASController {

	/** Generic domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	// ======================================================================

	/**
	 * Show the Attachments view for the object specified in the request.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView showAttachments(HttpServletRequest request) {

		// load question from URL
		Question subject = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = subject.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		Attachment attachment = null;
		String backUrl = "/html/db/mgt/ql/" + survey.getId() + ".html";
		List<Attachment> list = domainModelService.findAttachments(subject);
		Assert.isTrue(list.size() < 2, "multiple attachments not handled");
		if (list.size() > 0) {
			attachment = list.get(0);
		}

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("backUrl", backUrl);
		model.put("attachment", attachment);
		model.put("subject", subject);

		return new ModelAndView("/dashboard/manage/questionImages/questionImagesView", model);
	}

	// ======================================================================

	/**
	 * Add an Attachment form the object specified in the request.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/ul/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView addAttachmentForm(HttpServletRequest request, @RequestParam(value = "l") String languageCode) {
		return addAttachmentForm(request, languageCode, null);
	}

	public ModelAndView addAttachmentForm(HttpServletRequest request, @RequestParam(value = "l") String languageCode,
			Errors errors) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language, "unsupported language");

		//
		UploadAttachmentForm command = new UploadAttachmentForm(languageCode);
		if (errors == null) {
			errors = new BindException(command, "command");
		}

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("command", command);
		model.put("survey", survey);
		model.put("errors", errors);
		model.put("language", language);
		model.put("subject", question);

		return new ModelAndView("/dashboard/manage/questionImages/uploadQuestionImageForm", model);
	}

	@RequestMapping(value = "/db/mgt/qatt/ul/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView addAttachmentSubmit(HttpServletRequest request, UploadAttachmentForm command) {

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		//
		ModelAndView redirectOut = createRedirect(request, "/html/db/mgt/qatt/" + question.getId() + ".html");

		if (isCancel(request)) {
			return redirectOut;
		}

		if (command != null && command.getUpload() != null && command.getUpload().getSize() != 0) {

			//
			Errors errors = new BindException(command, "command");
			MultipartFile file = command.getUpload();

			String contentType = file.getContentType();

			boolean supportedFormat = false;
			if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
				supportedFormat = true;
			} else if ("image/gif".equals(contentType)) {
				supportedFormat = true;
			} else if ("image/png".equals(contentType)) {
				supportedFormat = true;
			}

			if (!supportedFormat) {
				//
				log.error("User attempted to upload question image of non-supported contentType (according to MultipartFile): "
						+ contentType);
				errors.reject("uploadLogos.error.generic");
			} else {
				//
				log.info("Processing attachment upload for survey #" + survey.getId() + ": " + file.getSize() + " bytes");
				surveyService.updateQuestionImage(errors, question, command);
			}

			// show errors if required
			if (errors.hasErrors()) {
				return addAttachmentForm(request, command.getLanguage(), errors);
			}

		} else {
			log.warn("USABILITY: User submitted upload attachment form with no file for survey #" + survey.getId());
		}

		return redirectOut;
	}

	// ======================================================================

	/**
	 * (Form) Delete an image for a particular language.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/rm/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView deleteImage(HttpServletRequest request, @RequestParam("l") String languageCode) {

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("question", question);
		model.put("languageName", language.getDisplayTitle());

		return new ModelAndView("/dashboard/manage/questionImages/deleteQuestionImageForm", model);
	}

	/**
	 * (Submit) Delete an image for a particular language.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/rm/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView deleteImageSubmit(HttpServletRequest request, @RequestParam("l") String languageCode) {

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		String redirectUrl = "/html/db/mgt/qatt/" + question.getId() + ".html";
		if (isCancel(request)) {
			return createRedirect(redirectUrl);
		}

		//
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);
		Assert.hasText(languageCode);

		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language);

		// do the deed
		surveyService.deleteQuestionImage(question, language);

		//
		return createRedirect(redirectUrl);
	}

	// ======================================================================

	/**
	 * (Form) Edit Tool Tip text for a particular language.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/tt/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView editToolTip(HttpServletRequest request, NameObjectCommand command, Errors errors,
			@RequestParam("l") String languageCode) {

		Assert.notNull(command);

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language);

		if (StringUtils.hasText(command.getMap().get(languageCode))) {
			// already has command data - being called from submit
		} else {
			// load value from DB
			String value = "";

			Attachment attachment = surveyService.getQuestionImage(question);
			if (attachment != null) {
				AttachmentPayload payload = attachment.getPayloads().get(language);
				if (payload != null) {
					value = payload.getAltText();
				}
			}

			// set command
			command.addName(languageCode, value);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("question", question);
		model.put("command", command);
		model.put("errors", errors);
		model.put("languageName", language.getDisplayTitle());
		// model.put("command", command);

		return new ModelAndView("/dashboard/manage/questionImages/editToolTip", model);
	}

	/**
	 * (Submit) Edit Tool Tip text for a particular language.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/qatt/tt/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView editToolTipSubmit(HttpServletRequest request, NameObjectCommand command, Errors errors,
			@RequestParam("l") String languageCode) {

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		String redirectUrl = "/html/db/mgt/qatt/" + question.getId() + ".html";
		if (isCancel(request)) {
			return createRedirect(redirectUrl);
		}

		//
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);
		Assert.hasText(languageCode);

		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language);

		// basic inline validation
		// TODO generic validator factory for these types of validations useful
		String value = command.getMap().get(languageCode);
		if (!StringUtils.hasText(value)) {
			errors.reject("editQuestionImageTooltip.validation.required");
		} else if (value.length() > 1024) {
			errors.reject("editQuestionImageTooltip.validation.maximumSize");
		}

		if (errors.hasErrors()) {
			// show the form
			return editToolTip(request, command, errors, languageCode);
		} else {
			// save the change
			surveyService.setQuestionImageToolTip(question, language, value);
		}

		return createRedirect(redirectUrl);
	}

	// ======================================================================

}

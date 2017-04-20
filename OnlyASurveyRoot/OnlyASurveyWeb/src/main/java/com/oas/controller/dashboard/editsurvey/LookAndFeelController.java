package com.oas.controller.dashboard.editsurvey;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.IdListCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.UploadLogoCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.SurveyTemplateOption;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.service.DomainModelService;
import com.oas.service.templating.integration.impl.Clf2TemplateIntegrationMethod;
import com.oas.util.RestfulIdUrlParser;

/**
 * Look & Feel for Surveys controller.
 * 
 * @author xhalliday
 * @since November 22, 2008
 */
@Controller
public class LookAndFeelController extends AbstractOASController {

	/** Maps to simple radios from form: Default. */
	protected static final int TYPE_DEFAULT = 1;

	/** Maps to simple radios from form: Upload logos. */
	protected static final int TYPE_UPLOAD_LOGOS = 2;

	/** Maps to simple radios from form: Magic Template Integration. */
	protected static final int TYPE_MAGIC = 99;

	/**
	 * Integration method for auto-magically integrating MTI-compliant
	 * templates.
	 */
	@Autowired
	private Clf2TemplateIntegrationMethod clf2TemplateIntegrationMethod;

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	// ======================================================================

	/**
	 * Show the Look & Feel tab view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/lnf/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView showTabView(HttpServletRequest request) {
		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", new IdListCommand(new Long[] { getTemplateOptionOrdinal(survey) }));

		// show view
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/lookAndFeel/lookAndFeelTab", model);
	}

	protected Long getTemplateOptionOrdinal(Survey survey) {
		if (SurveyTemplateOption.DEFAULT.equals(survey.getTemplateOption())) {
			return 1L;
		} else if (SurveyTemplateOption.UPLOADED_LOGOS.equals(survey.getTemplateOption())) {
			return 2L;
		} else if (SurveyTemplateOption.MAGIC.equals(survey.getTemplateOption())) {
			return 99L;
		} else {
			throw new IllegalArgumentException("invalid type code");
		}
	}

	// ======================================================================

	@RequestMapping(value = "/db/mgt/lnf/pvw/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView previewLookAndFeel(HttpServletRequest request, HttpServletResponse response) {

		Survey survey = getSurveyFromRestfulUrl(request);

		SupportedLanguage language = supportedLanguageService
				.findByCode(coalesce(request.getParameter("l"), getCurrentLanguage()));

		// apply whatever template is appropriate: do not change locale, since
		// the logged in user's language is important in the body
		applySurveyTemplateOption(request, response, survey, language, false);

		return new ModelAndView("/dashboard/manage/lookAndFeel/previewLookAndFeel", "survey", survey);
	}

	// ======================================================================

	/**
	 * Cancel
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = { "/db/mgt/lnf/*.html" }, params = { "_cancel" })
	@ValidUser
	public ModelAndView cancel(HttpServletRequest request) {
		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		return createRedirect("/html/db/mgt/" + survey.getId() + ".html");
	}

	// ======================================================================

	/**
	 * POST action for changing the Template Option of the Survey.
	 * 
	 */
	@RequestMapping(value = { "/db/mgt/lnf/chty/*.html" }, method = { RequestMethod.POST })
	@ValidUser
	public ModelAndView changeTemplateType(HttpServletRequest request, IdListCommand command) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		String redirectUrl = "/html/db/mgt/lnf/" + survey.getId() + ".html";

		// assign the new templating option
		List<Long> list = command.getIds();
		Assert.notNull(list);
		Assert.isTrue(list.size() == 1);

		int typeCode = list.get(0).intValue();
		switch (typeCode) {
		case TYPE_DEFAULT:
			//
			survey.setTemplateOption(SurveyTemplateOption.DEFAULT);
			surveyService.save(survey);
			break;
		case TYPE_UPLOAD_LOGOS:
			//
			// change type immediately : user cannot cancel this flow
			survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
			surveyService.save(survey);

			// redirect to the page where they import from URL
			redirectUrl = "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html";
			break;
		case TYPE_MAGIC:
			//
			// DO NOT change, as user can cancel this flow
			// survey.setTemplateOption(SurveyTemplateOption.MAGIC);
			// redirect to the page where they import from URL
			redirectUrl = "/html/db/mgt/lnf/mti/" + survey.getId() + ".html";
			break;
		default:
			throw new IllegalArgumentException("unrecognized type code:" + typeCode);
		}

		return createRedirect(redirectUrl);
	}

	// ======================================================================

	/**
	 * Show the Magic Template Integration form.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/lnf/mti/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView importTemplateForm(HttpServletRequest request) {
		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// command
		NameObjectCommand command = new NameObjectCommand();

		for (SupportedLanguage language : survey.getTemplates().keySet()) {
			Template template = survey.getTemplates().get(language);
			command.addName(language.getIso3Lang(), template.getImportedFromUrl());
		}

		// model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", command);

		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/lookAndFeel/MTI/importTemplateForm", model);
	}

	/**
	 * Show the Import Template form.
	 * 
	 * TODO this method is too complex
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/lnf/mti/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView importTemplateSubmit(HttpServletRequest request, NameObjectCommand command) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// cancel?
		if (isCancel(request)) {
			return createRedirect(getReturnTo(request, "/html/db/mgt/lnf/" + survey.getId() + ".html"));
		}

		Errors errors = new BindException(command, "command");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		String viewName = "/dashboard/manage/lookAndFeel/MTI/importTemplateForm";

		try {

			Map<String, String> cleanedUrlList = new HashMap<String, String>();
			Map<String, String> markupByLanguage = new HashMap<String, String>();
			Map<String, String> baseUrlByLanguage = new HashMap<String, String>();

			// for each language check if markup can be processed
			for (String isoCode : command.getMap().keySet()) {
				String url = command.getMap().get(isoCode);

				if (StringUtils.hasText(url)) {

					// get any markup
					log.info("Downloading from URL for Magic Template Integration: " + url);

					if (!(url.startsWith("http://") || url.startsWith("https://"))) {
						url = "http://" + url;
					}

					cleanedUrlList.put(isoCode, url);

					// base URL
					URL url2 = new URL(url);
					String baseUrl = url2.getProtocol() + "://" + url2.getHost();

					// load markup from remote
					String markup = domainModelService.getContentFromURL(url);

					// for processing later
					markupByLanguage.put(isoCode, markup);
					baseUrlByLanguage.put(isoCode, baseUrl);

					// check to see if it can be processed
					clf2TemplateIntegrationMethod.canProcess(markup, errors);
				}
			}

			if (!errors.hasErrors()) {
				// continue processing since all markup seems valid
				// for each language
				for (String isoCode : cleanedUrlList.keySet()) {

					String url = cleanedUrlList.get(isoCode);

					String markup = markupByLanguage.get(isoCode);
					String baseUrl = baseUrlByLanguage.get(isoCode);

					SupportedLanguage language = supportedLanguageService.findByCode(isoCode);

					if (StringUtils.hasText(markup)) {

						// process markup into a template
						Template template = clf2TemplateIntegrationMethod.processMarkup(baseUrl, url, markup);

						// override any detected language
						template.setSupportedLanguage(language);

						// apply the template and persist
						surveyService.applyTemplate(survey, template);

					} else {
						// remove existing: clearing the text box (or not
						// providing one) indicates not wanting a template for
						// that language
						Template existing = survey.getTemplates().get(language);
						if (existing != null) {
							surveyService.clearTemplate(survey, existing);
						}
					}
				}

				// ensure type is set
				survey.setTemplateOption(SurveyTemplateOption.MAGIC);
				surveyService.save(survey);

				return createRedirect("/html/db/mgt/lnf/" + survey.getId() + ".html");
			}

		} catch (Exception e) {
			// TODO this is pretty simplistic
			log.error("exception while trying to integrate a MTI template", e);
			errors.reject("mti.importTemplate.error.generic");
		}

		model.put("errors", errors);
		model.put("command", command);
		return new ModelAndView(viewName, model);
	}

	// ======================================================================

	protected SupportedLanguage getLanguageFromURI(HttpServletRequest request) {
		// language
		String languageCode = RestfulIdUrlParser.parseLastPathPart(request.getRequestURI(), 1);
		Assert.hasText(languageCode, "no language code");

		SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
		Assert.notNull(language);
		return language;
	}

	protected SurveyLogo.PositionType getPositionFromURI(HttpServletRequest request) {
		// position
		String position = RestfulIdUrlParser.parseLastPathPart(request.getRequestURI(), 2);
		Assert.hasText(position, "no position code");
		Assert.isTrue("l".equals(position) || "r".equals(position));

		return SurveyLogo.PositionType.fromLetter(position.charAt(0));
	}

	// ======================================================================

	/**
	 * Main view for showing the Logos attached to a Survey.
	 * 
	 */

	@RequestMapping(value = "/db/mgt/lnf/lgos/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView surveyLogosView(HttpServletRequest request) {
		return doSurveyLogosView(request, null);
	}

	private ModelAndView doSurveyLogosView(HttpServletRequest request, Errors errors) {

		Survey survey = getSurveyFromRestfulUrl(request);

		// iso3 code => survey logo
		Map<String, SurveyLogo> leftLogos = new HashMap<String, SurveyLogo>();
		Map<String, SurveyLogo> rightLogos = new HashMap<String, SurveyLogo>();

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("survey", survey);
		model.put("errors", errors);
		model.put("leftLogos", leftLogos);
		model.put("rightLogos", rightLogos);

		for (SupportedLanguage language : survey.getSupportedLanguages()) {

			String languageCode = language.getIso3Lang();

			// load for language
			Map<SurveyLogo.PositionType, SurveyLogo> logoList = surveyService.getLogosForLanguage(survey, language);

			for (SurveyLogo.PositionType position : logoList.keySet()) {
				//
				if (SurveyLogo.PositionType.isLeft(position)) {
					// left-side logos
					leftLogos.put(languageCode, logoList.get(position));

				} else if (SurveyLogo.PositionType.isRight(position)) {
					// right-side logos
					rightLogos.put(languageCode, logoList.get(position));

				} else {
					// unknown - error
					final String message = "unknown position value";
					log.error(message + ": " + position);
					throw new IllegalArgumentException(message);
				}
			}
		}

		return new ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/surveyLogosView", model);
	}

	@RequestMapping(value = "/db/mgt/lnf/uplg/*/*/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView uploadLogosForm(HttpServletRequest request) {
		return doUploadLogosForm(request, null);
	}

	private ModelAndView doUploadLogosForm(HttpServletRequest request, Errors errors) {

		// load and validate authorization
		Survey survey = getSurveyFromRestfulUrl(request);

		//
		Map<String, Object> model = new HashMap<String, Object>();

		// language
		SupportedLanguage language = getLanguageFromURI(request);
		Assert.isTrue(survey.getSupportedLanguages().contains(language), "not a survey language");

		// position
		SurveyLogo.PositionType position = getPositionFromURI(request);

		if (SurveyLogo.PositionType.isLeft(position)) {
			model.put("pageTitle", "uploadLogos.uploadImage.left.pageTitle");
		} else {
			model.put("pageTitle", "uploadLogos.uploadImage.right.pageTitle");
		}

		model.put("survey", survey);
		model.put("errors", errors);
		model.put("language", language);
		model.put("position", position);

		return new ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/uploadLogosForm", model);
	}

	/**
	 * Process an Upload Logo command.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/lnf/uplg/*/*/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView uploadLogosSubmit(HttpServletRequest request, UploadLogoCommand command, Errors errors) {

		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html");
		}

		Assert.notNull(command);

		// no cancel at this point - must have an uploaded file
		MultipartFile upload = command.getUpload();
		if (upload == null || upload.isEmpty()) {
			// no file
			errors.reject("uploadLogos.error.noFileUploaded");

		} else {

			// language
			SupportedLanguage language = getLanguageFromURI(request);
			Assert.isTrue(survey.getSupportedLanguages().contains(language), "not a survey language");

			// position
			SurveyLogo.PositionType position = getPositionFromURI(request);

			String altText = command.getAltText();
			if (StringUtils.hasText(altText)) {
				// basic validation
				altText = altText.trim();
				if (altText.length() > 255) {
					// silently trim
					// TODO revisit
					altText = altText.substring(0, 255);
				}
			}

			try {
				log.info("Attaching logo to Survey #" + survey.getId() + ": " + position + "/" + language);
				doAttachLogo(errors, survey, upload, language, position, altText);
				log.info("Done attaching logo to Survey #" + survey.getId() + ": " + position + "/" + language);

			} catch (IOException e) {
				log.error("IOException processing logo attachments", e);
				errors.reject("uploadLogos.error.generic");
			} catch (RuntimeException e) {
				log.error("RuntimeException processing logo attachments", e);
				errors.reject("uploadLogos.error.generic");
			}
		}

		// if NO ERRORS then:
		if (errors.hasErrors()) {
			return doUploadLogosForm(request, errors);
		}

		// survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
		// surveyService.save(survey);

		return createRedirect(request, "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html");
	}

	/**
	 * Show the confirmation form for deleting a logo.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/lnf/rmlg/*/*/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView deleteLogoForm(HttpServletRequest request) {

		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html");
		}

		// language
		SupportedLanguage language = getLanguageFromURI(request);
		Assert.isTrue(survey.getSupportedLanguages().contains(language), "not a survey language");

		// position
		SurveyLogo.PositionType position = getPositionFromURI(request);
		Assert.isTrue(SurveyLogo.PositionType.isLeft(position) || SurveyLogo.PositionType.isRight(position));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);

		// return new
		// ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/deleteLogo");
		return new ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/deleteLogo", model);
	}

	/**
	 * Delete a logo.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/lnf/rmlg/*/*/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView deleteLogoSubmit(HttpServletRequest request) {

		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html");
		}

		// language
		SupportedLanguage language = getLanguageFromURI(request);
		Assert.isTrue(survey.getSupportedLanguages().contains(language), "not a survey language");

		// position
		SurveyLogo.PositionType position = getPositionFromURI(request);
		Assert.isTrue(SurveyLogo.PositionType.isLeft(position) || SurveyLogo.PositionType.isRight(position));

		if (true) {
			surveyService.purgeLogo(survey, language, position);
		}

		return createRedirect(request, "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html");
	}

	/**
	 * Invoke the Survey Service to attach a logo.
	 * 
	 * @param survey
	 * @param file
	 * @param position
	 *            LEFT or RIGHT
	 * @throws IOException
	 */
	private void doAttachLogo(Errors errors, Survey survey, MultipartFile file, SupportedLanguage language,
			SurveyLogo.PositionType position, String altText) throws IOException {

		// ignore empty uploads
		if (file != null && file.getSize() > 0) {

			// to detect weirdness where end users can upload to our machines
			log.info("User #" + SecurityUtil.getCurrentUser().getId() + " is uploading " + file.getSize()
					+ " byte file for logo (position " + position + ") for survey #" + survey.getId());

			// ensure it's a supported type

			String contentType = file.getContentType();

			boolean supportedFormat = false;
			if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
				supportedFormat = true;
			} else if ("image/gif".equals(contentType)) {
				supportedFormat = true;
			} else if ("image/png".equals(contentType)) {
				supportedFormat = true;
			}

			Assert.isTrue(supportedFormat, "unsupported format");

			// byte array payload
			byte[] payload = file.getBytes();

			// attach
			surveyService.attachLogo(errors, survey, language, position, contentType, altText, payload);
		}
	}

	// ======================================================================
	@RequestMapping(value = "/db/mgt/lnf/lgos/tt/*/*/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView editToolTip(HttpServletRequest request, NameObjectCommand command, Errors errors) {

		Assert.notNull(command);

		//
		Survey survey = getSurveyFromRestfulUrl(request);

		SupportedLanguage language = getLanguageFromURI(request);
		Assert.isTrue(survey.getSupportedLanguages().contains(language));
		String languageCode = language.getIso3Lang();

		SurveyLogo.PositionType position = getPositionFromURI(request);

		if (StringUtils.hasText(command.getMap().get(languageCode))) {
			// already has command data - being called from submit
		} else {
			// load value from DB
			String value = "";

			SurveyLogo logo = surveyService.getLogo(survey, language, position);
			if (logo != null) {
				value = logo.getAltText().trim();
			}

			// set command
			command.addName(languageCode, value);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", command);
		model.put("errors", errors);
		model.put("languageName", language.getDisplayTitle());
		model.put("languageCode", language.getIso3Lang());

		return new ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/editLogoToolTip", model);
	}

	/**
	 * (Submit) Edit Tool Tip text for a particular language.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/lnf/lgos/tt/*/*/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView editToolTipSubmit(HttpServletRequest request, NameObjectCommand command, Errors errors) {

		//
		Survey survey = getSurveyFromRestfulUrl(request);
		String redirectUrl = "/html/db/mgt/lnf/lgos/" + survey.getId() + ".html";

		if (isCancel(request)) {
			return createRedirect(redirectUrl);
		}

		// language
		SupportedLanguage language = getLanguageFromURI(request);
		Assert.isTrue(survey.getSupportedLanguages().contains(language), "not a survey language");
		String languageCode = language.getIso3Lang();

		// position
		SurveyLogo.PositionType position = getPositionFromURI(request);
		Assert.isTrue(SurveyLogo.PositionType.isLeft(position) || SurveyLogo.PositionType.isRight(position));

		// basic inline validation
		// TODO generic validator factory for these types of validations useful
		String value = command.getMap().get(languageCode);
		if (!StringUtils.hasText(value)) {
			errors.reject("surveyLogos.toolTip.validation.required");
		} else if (value.length() > 1024) {
			errors.reject("surveyLogos.toolTip.validation.maximumSize");
		}

		if (errors.hasErrors()) {
			// show the form
			return editToolTip(request, command, errors);
		} else {
			// save the change
			surveyService.updateLogoAlt(survey, language, position, value);
		}

		return createRedirect(redirectUrl);
	}

}

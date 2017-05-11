package com.oas.controller.dashboard.editsurvey;

import java.io.IOException;
import java.net.URL;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.IdListCommand;
import com.oas.command.model.LogoUploadCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.SurveyTemplateOption;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.service.DomainModelService;
import com.oas.service.templating.integration.impl.Clf2TemplateIntegrationMethod;

/**
 * Look & Feel for Surveys controller.
 * 
 * @author xhalliday
 * @since November 22, 2008
 */
@Controller
public class LookAndFeelController extends AbstractOASController {

	/** Maps to simple radios from form: Default. */
	private static final int TYPE_DEFAULT = 1;

	/** Maps to simple radios from form: Upload logos. */
	private static final int TYPE_UPLOAD_LOGOS = 2;

	/** Maps to simple radios from form: Magic Template Integration. */
	private static final int TYPE_MAGIC = 99;

	/** For handling file uploads. */
	@Autowired
	private MultipartResolver multipartResolver;

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
			// DO NOT change, as user can cancel this flow
			// survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
			// redirect to the page where they import from URL
			redirectUrl = "/html/db/mgt/lnf/uplg/" + survey.getId() + ".html";
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

	@RequestMapping(value = "/db/mgt/lnf/uplg/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView uploadLogosForm(HttpServletRequest request) {
		return doUploadLogosForm(request, null);
	}

	private ModelAndView doUploadLogosForm(HttpServletRequest request, Errors errors) {

		Survey survey = getSurveyFromRestfulUrl(request);
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("survey", survey);
		model.put("errors", errors);

		return new ModelAndView("/dashboard/manage/lookAndFeel/uploadLogos/uploadLogosForm", model);
	}

	/**
	 * Process an Upload Logo or Delete Logo command.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/lnf/uplg/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView uploadLogosSubmit(HttpServletRequest request, LogoUploadCommand command, Errors errors) {

		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/lnf/" + survey.getId() + ".html");
		}

		Assert.notNull(command);

		try {
			for (SupportedLanguage language : survey.getSupportedLanguages()) {
				String languageCode = language.getIso3Lang();

				String leftAlt = command.getLalt().get(languageCode);
				String rightAlt = command.getRalt().get(languageCode);

				doAttachLogo(errors, survey, command.getLlgo().get(languageCode), language, SurveyLogo.PositionType.LEFT, leftAlt);
				doAttachLogo(errors, survey, command.getRlgo().get(languageCode), language, SurveyLogo.PositionType.RIGHT,
						rightAlt);
			}
		} catch (IOException e) {
			log.error("IOException processing logo attachments", e);
			errors.reject("uploadLogos.error.generic");
		} catch (RuntimeException e) {
			log.error("RuntimeException processing logo attachments", e);
			errors.reject("uploadLogos.error.generic");
		}

		// if NO ERRORS then:
		if (errors.hasErrors()) {
			return doUploadLogosForm(request, errors);
		}

		survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
		surveyService.save(survey);

		return createRedirect(request, "/html/db/mgt/lnf/" + survey.getId() + ".html");
	}

	/**
	 * TODO this is here as a prototype stub; it MUST be in a service!
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

	@RequestMapping(value = "/db/mgt/lnf/pvw/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView previewLookAndFeel(HttpServletRequest request) {

		Survey survey = getSurveyFromRestfulUrl(request);

		// apply whatever template is appropriate
		applySurveyTemplateOption(request, survey);

		return new ModelAndView("/dashboard/manage/lookAndFeel/previewLookAndFeel", "survey", survey);
	}

	// ======================================================================

}

package com.oas.controller;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.controller.AbstractAnnotatedSaneController;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.security.SecurityUtil;

import com.oas.model.BaseObject;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.SurveyTemplateOption;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;
import com.oas.security.SecurityAssertions;
import com.oas.service.ResponseService;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;
import com.oas.service.templating.integration.impl.Clf2TemplateIntegrationMethod;
import com.oas.util.Constants;
import com.oas.util.RestfulIdUrlParser;
import com.opensymphony.module.sitemesh.RequestConstants;

/**
 * Parent for all controllers in OAS.
 * 
 * TODO move survey template application (ie, transformation) to a service, or
 * create a parent for those controllers that call it.
 * 
 * @author xhalliday
 * 
 */
abstract public class AbstractOASController extends AbstractAnnotatedSaneController {

	/**
	 * Log.
	 */
	protected Logger log = Logger.getLogger(getClass());

	/**
	 * Generic data access service.
	 */
	@Autowired
	@Qualifier("dataAccessObject")
	protected DataAccessObject dataAccessObject;

	/**
	 * LocaleResolver for changing locale programmatically.
	 */
	@Autowired
	protected LocaleResolver localeResolver;

	/**
	 * Service for interacting with the survey domain.
	 */
	@Autowired
	protected SurveyService surveyService;

	/**
	 * Service for interacting with the response domain.
	 */
	@Autowired
	protected ResponseService responseService;

	/**
	 * Service for dealing with the SupportedLanguage property of various
	 * objects.
	 */
	@Autowired
	protected SupportedLanguageService supportedLanguageService;

	/**
	 * CLF2 template integration method.
	 * 
	 * TODO refactor: template integration needs to be handled by a service
	 */
	@Autowired
	protected Clf2TemplateIntegrationMethod magicTemplateIntegration;

	// ======================================================================

	protected Long getRestfulId(HttpServletRequest request) {
		//
		Long retval = RestfulIdUrlParser.parseTrailingId(request.getRequestURI());
		return retval;
	}

	/**
	 * Get a path from a URL, beginning after the last slash.
	 * 
	 * @param request
	 * @param everythingAfter
	 * @return
	 */
	protected String getRestfulPath(HttpServletRequest request) {
		// String pathInfo = request.getPathInfo();
		String retval = RestfulIdUrlParser.parseLastPathPart(request.getRequestURI(), true);
		return retval;
	}

	/**
	 * Load a Survey based on a restful URL, eg., /prefix/123.html, and assert
	 * ownership of it.
	 * 
	 * @param request
	 * @return
	 */
	protected Survey getSurveyFromRestfulUrl(HttpServletRequest request) {
		return getSurveyFromRestfulUrl(request, true);
	}

	/**
	 * Load a Survey based on a restful URL, eg., /prefix/123.html, optionally
	 * asserting ownership of it.
	 * 
	 * @param request
	 * @return
	 */
	protected Survey getSurveyFromRestfulUrl(HttpServletRequest request, boolean assertOwnership) {

		if (assertOwnership) {
			requireSecureContext();
		}

		// get the survey ID from the URL
		Long id = RestfulIdUrlParser.parseTrailingId(request.getRequestURI());
		Assert.notNull(id, "ID missing");

		// load the survey
		Survey survey = surveyService.findNonDeletedSurvey(id);
		Assert.notNull(survey, "no such survey");

		if (assertOwnership) {
			// assert ownership
			SecurityAssertions.assertOwnership(survey);
		}

		return survey;
	}

	protected Response getResponseFromRestfulUrl(HttpServletRequest request) {
		return getEntityFromRestfulUrl(Response.class, request, true);
	}

	protected BaseObject getBaseObjectFromRestfulUrl(HttpServletRequest request) {

		Long id = getRestfulId(request);
		Assert.notNull(id);

		BaseObject retval = dataAccessObject.get(BaseObject.class, id);
		Assert.notNull(retval);

		// the object is required
		Assert.notNull(retval);
		return retval;
	}

	protected <C> C getEntityFromRestfulUrl(Class<C> clazz, HttpServletRequest request) {
		return getEntityFromRestfulUrl(clazz, request, true);
	}

	protected <C> C getEntityFromRestfulUrl(Class<C> clazz, HttpServletRequest request, boolean mustExist) {

		Long id = getRestfulId(request);
		Assert.notNull(id);

		Object loadedObject = dataAccessObject.get(clazz, id);
		if (loadedObject == null && !mustExist) {
			return null;

		}
		Assert.notNull(loadedObject);
		Assert.isAssignable(clazz, loadedObject.getClass());

		@SuppressWarnings("unchecked")
		C retval = (C) loadedObject;

		// the object is required
		if (mustExist) {
			Assert.notNull(retval);
		}
		return retval;
	}

	// ======================================================================

	protected Survey getSurveyFromModel(Map<String, Object> model) {
		return (Survey) model.get("survey");
	}

	// ======================================================================

	/**
	 * Set the decorator to be used to the parameter.
	 * 
	 * @param request
	 */
	protected void applyLayout(HttpServletRequest request, String layoutName) {
		request.setAttribute(RequestConstants.DECORATOR, layoutName);
	}

	protected void applySurveyTemplateOption(HttpServletRequest request, Survey survey) {
		// TODO take this from request to support preview in any language
		SupportedLanguage language = supportedLanguageService
				.findByCode(coalesce(request.getParameter("l"), getCurrentLanguage()));

		applySurveyTemplateOption(request, survey, language);
	}

	/**
	 * Apply whatever the appropriate decorator is given the Survey's
	 * configuration.
	 * 
	 * @param request
	 * @param survey
	 * @param language
	 */
	protected void applySurveyTemplateOption(HttpServletRequest request, Survey survey, SupportedLanguage language) {
		//
		request.setAttribute("templateLanguage", language.getIso3Lang());

		//
		if (survey.getTemplateOption().equals(SurveyTemplateOption.DEFAULT)) {
			//
			applyDefaultSurveyResponseLayout(request);

		} else if (survey.getTemplateOption().equals(SurveyTemplateOption.UPLOADED_LOGOS)) {
			//
			applyLogos(request, survey, language);
			applyDefaultSurveyResponseLayout(request);

		} else if (survey.getTemplateOption().equals(SurveyTemplateOption.MAGIC)) {
			//
			Template template = survey.getTemplates().get(language);
			if (template != null) {
				applyMagicTemplateIntegration(request, template);
			} else {
				// no template for this language - fallback to default
				applyDefaultSurveyResponseLayout(request);
			}

		} else {
			throw new IllegalArgumentException("unknown template option");
		}

	}

	/**
	 * Apply Magic Template Integration.
	 * 
	 * @param request
	 * @param template
	 */
	protected void applyMagicTemplateIntegration(HttpServletRequest request, Template template) {

		Assert.notNull(request);
		Assert.notNull(template, "no Template for Survey");

		// apply the appropriate decorator
		applyLayout(request, "magicIntegrationTemplate");

		String beforeContent = magicTemplateIntegration.runtimeBeforeContent(request, template, template.getBeforeContent());
		String afterContent = magicTemplateIntegration.runtimeAfterContent(request, template, template.getAfterContent());

		// access to the template
		request.setAttribute("template", template);
		request.setAttribute("beforeContent", beforeContent);
		request.setAttribute("afterContent", afterContent);
	}

	/**
	 * Set the decorator to be used to "surveyResponseDecorator".
	 * 
	 * @param request
	 */
	protected void applyDefaultSurveyResponseLayout(HttpServletRequest request) {
		applyLayout(request, Constants.SURVEY_RESPONSE_DECORATOR_NAME);
	}

	/**
	 * Apply any logo data to the request, if they exist.
	 * 
	 * @param request
	 * @param survey
	 */
	protected void applyLogos(HttpServletRequest request, Survey survey, SupportedLanguage language) {

		Assert.notNull(request);
		Assert.notNull(survey);
		Assert.notNull(language);

		Map<SurveyLogo.PositionType, SurveyLogo> map = surveyService.getLogosForLanguage(survey, language);

		SurveyLogo left = map.get(SurveyLogo.PositionType.LEFT);
		SurveyLogo right = map.get(SurveyLogo.PositionType.RIGHT);

		if (left != null) {
			request.setAttribute("leftLogoExists", true);
			request.setAttribute("leftLogoAlt", left.getAltText());
			request.setAttribute("leftLogoExtension", getExtensionForContentType(left.getContentType()));
		}

		if (right != null) {
			request.setAttribute("rightLogoExists", true);
			request.setAttribute("rightLogoAlt", right.getAltText());
			request.setAttribute("rightLogoExtension", getExtensionForContentType(right.getContentType()));
		}

	}

	protected String getExtensionForContentType(String contentType) {

		// quasi-reasonable default
		String retval = "gif";

		if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
			retval = "jpg";
		} else if ("image/gif".equals(contentType)) {
			retval = "gif";
		} else if ("image/png".equals(contentType)) {
			retval = "png";
		}

		return retval;
	}

	/**
	 * Set the decorator to be used to "wideLayout".
	 * 
	 * @param request
	 */
	protected void applyWideLayout(HttpServletRequest request) {
		applyLayout(request, Constants.WIDE_LAYOUT_DECORATOR_NAME);
	}

	// ======================================================================

	protected ModelAndView getDefaultHome() {
		return new ModelAndView(new RedirectView(Constants.DEFAULT_HOME, true));
	}

	/**
	 * Get a default redirect out, using either ?returnTo/?rTo or
	 * Constants.DEFAULT_HOME.
	 * 
	 * 
	 * @param request
	 * @return
	 */
	protected ModelAndView createRedirect(HttpServletRequest request) {
		return createRedirect(getReturnTo(request, Constants.DEFAULT_HOME));
	}

	protected ModelAndView createRedirect(HttpServletRequest request, String defaultUrl) {
		return createRedirect(getReturnTo(request, coalesce(defaultUrl, Constants.DEFAULT_HOME)));
	}

	protected ModelAndView createRedirect(String url) {
		return new ModelAndView(new RedirectView(url, true));
	}

	protected String getReturnTo(HttpServletRequest request, String defaultUrl) {
		String redirectUrl = defaultUrl;
		String returnTo = coalesce(request.getParameter("returnTo"), request.getParameter("rTo"));

		if (StringUtils.hasText(returnTo)) {
			// ignore anything that seems to have a protocol, as DiD for
			// outsiders crafting URLs (XSS/CSRF).
			if (returnTo.indexOf(":") == -1) {
				redirectUrl = returnTo;
			} else {
				String message = "getReturnTo got a returnTo parameter that was absolute: contains the colon character from ";
				if (SecurityUtil.isSecureContext()) {
					message += "user#" + SecurityUtil.getCurrentUser().getId();
				} else {
					message += "anonymous user.";
				}
				message += " Ignored.";
				log.warn(message);
			}
		} else {
			// none was specified: a default MUST be present
			Assert.notNull(defaultUrl, "no default URL");
		}

		return redirectUrl;
	}

	/**
	 * Sets the current user's locale via the localeResolver. Only pass in
	 * "eng", "fra", or other supported ISO3 codes.
	 * 
	 * @param request
	 * @param response
	 * @param language
	 */
	protected void changeLocale(HttpServletRequest request, HttpServletResponse response, String language) {

		Assert.isTrue(language.indexOf("_") == -1, "non-iso3-code given");
		Assert.notNull(localeResolver, "No LocaleResolver found: not in a DispatcherServlet request?");

		LocaleEditor localeEditor = new LocaleEditor();
		localeEditor.setAsText(language.substring(0, 2) + "_CA");
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
	}

	// ======================================================================

	/**
	 * Determine the absolute URL prefix to use, taking reverse proxies into
	 * consideration.
	 * 
	 * @return String http://absolute-url.com/prefix
	 */
	protected String determineAbsoluteUrlPrefix(HttpServletRequest request) {
		String retval = "";

		String proxyingFor = request.getHeader(Constants.X_FORWARDED_HOST);

		// if behind a reverse proxy that sets X-Forwarded-Host header
		if (StringUtils.hasText(proxyingFor)) {
			// upstream's responsibility to enforce SSL security
			// if (request.isSecure()) {
			// retval = "https://";
			// } else {
			retval = "http://";
			// }

			retval += proxyingFor;
		} else {

			if (request.isSecure()) {
				retval = "https://";
			} else {
				retval = "http://";
			}

			retval += request.getServerName();

			// a custom port if necessary
			if (request.getServerPort() != 80) {
				retval += ":" + request.getServerPort();
			}
		}

		// context path
		retval += request.getContextPath();

		return retval;
	}

	// ======================================================================

	public String coalesce(String string1, String string2) {
		if (StringUtils.hasText(string1)) {
			return string1;
		}

		return string2;
	}

	public String getCurrentLanguage() {
		return LocaleContextHolder.getLocale().getISO3Language();
	}

	public SupportedLanguage getCurrentSupportedLanguage() {
		return supportedLanguageService.findByCode(LocaleContextHolder.getLocale().getISO3Language());
	}

	// ======================================================================

	/**
	 * This is overridden here so that controller tests can inspect the
	 * reference data. Otherwise the method is not visible.
	 */
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		return super.referenceData(request);
	}

	protected boolean isSave(HttpServletRequest request) {
		return (request.getParameter("_save") != null || request.getParameter("_evas") != null);
	}

	//
	// protected boolean isFinish(HttpServletRequest request) {
	// return (request.getParameter("_finish") != null);
	// }
	//
	// protected boolean isBack(HttpServletRequest request) {
	// return (request.getParameter("_back") != null);
	// }

	protected boolean isCancel(HttpServletRequest request) {
		return (request.getParameter("_cancel") != null || request.getParameter("_lecnac") != null);
	}

	// ======================================================================

	/**
	 * Determine the page number used in a DisplayTag table.
	 * 
	 * @param request
	 * @param tableId
	 * @return
	 */
	protected int getDisplayTagPageNumber(HttpServletRequest request, String tableId) {

		String paramName = new ParamEncoder(tableId).encodeParameterName(TableTagParameters.PARAMETER_PAGE);
		String param = request.getParameter(paramName);
		if (StringUtils.hasText(param)) {
			return Integer.parseInt(param) - 1;
		} else {
			return 0;
		}
	}
}

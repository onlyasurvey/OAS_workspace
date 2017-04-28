package com.oas.service.templating.integration.impl;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;
import com.oas.service.SupportedLanguageService;

/**
 * A template integration method for CLF2 compliant markup.
 * 
 * @author xhalliday
 * @since December 6, 2008
 */
@Component
public class Clf2TemplateIntegrationMethod extends AbstractTemplateIntegrationMethod {

	/** Service for dealing with supported languages. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	/** Strings that indicate the beginning of content in CLF2-templated markup. */
	private static final String[] CONTENT_BEGINS = { "<!-- OAS01 -->", "<!-- CONTENT BEGINS | DEBUT DU CONTENU -->",
			"<!-- DEBUT DU CONTENU | CONTENT BEGINS -->", "<!-- CONTENT BEGINS | CONTENU COMMENCE -->" };

	/** Strings that indicate the end of content in CLF2-templated markup. */
	private static final String[] CONTENT_ENDS = { "<!-- OAS02 -->", "<!-- CONTENT ENDS | FIN DU CONTENU -->",
			"<!-- FIN DU CONTENU | CONTENT ENDS -->", "<!-- CONTENT ENDS | CONTENU TERMINE -->" };

	/** Compiled pattern for matching language. */
	private Pattern linkToEnglishPattern;

	/** Compiled pattern for matching language. */
	private Pattern linkToFrenchPattern;

	/** Regular expressing for detecting an English language toggle link. */
	private String linkToEnglishRegex = ".*>.*English.*</a>.*";

	/** Regular expressing for detecting a French language toggle link. */
	private String linkToFrenchRegex = ".*>.*Fran.*ais.*</a>.*";

	// ======================================================================

	/**
	 * Strings that indicate the start of a language toggle in CLF2-templated
	 * markup.
	 */
	protected static final String[] LANGUAGE_TOGGLE_START = { "<!-- OAS11 -->",
			"<!-- FRENCH LINK BEGINS | DEBUT DU LIEN FRANCAIS -->", "<!-- FRENCH LINK BEGINS | LIEN FRANCAIS COMMENCE -->",
			"<!-- ENGLISH LINK BEGINS | LIEN ANGLAIS COMMENCE -->", "<!-- DEBUT DU LIEN ANGLAIS | ENGLISH LINK BEGINS -->" };

	/**
	 * Strings that indicate the end of a language toggle in CLF2-templated
	 * markup.
	 */
	protected static final String[] LANGUAGE_TOGGLE_END = { "<!-- OAS12 -->", "<!-- FRENCH LINK ENDS | FIN DU LIEN FRANCAIS -->",
			"<!-- FRENCH LINK ENDS | LIEN FRANCAIS TERMINE -->", "<!-- ENGLISH LINK ENDS | LIEN ANGLAIS TERMINE -->",
			"<!-- FIN DU LIEN ANGLAIS | ENGLISH LINK ENDS -->" };

	// ======================================================================

	public Clf2TemplateIntegrationMethod() {
		initializeRegularExpressions();
	}

	protected void initializeRegularExpressions() {
		// linkToEnglishPattern = Pattern.compile(linkToEnglishRegex);
		// linkToFrenchPattern = Pattern.compile(linkToFrenchRegex);
		linkToEnglishPattern = Pattern.compile(linkToEnglishRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		linkToFrenchPattern = Pattern.compile(linkToFrenchRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void canProcess(String markup, Errors errors) {
		int contentBegins = determineContentBeginIndex(markup);
		int contentEnds = determineContentEndIndex(markup);

		if (contentBegins == -1) {
			errors.reject("mti.tim.error.noStartingComment");
		}

		if (contentEnds == -1) {
			errors.reject("mti.tim.error.noEndingComment");
		}
	}

	@Override
	public Template processMarkup(String baseUrl, String importUrl, String markup) {
		Template retval = null;
		int contentBegins = determineContentBeginIndex(markup);
		int contentEnds = determineContentEndIndex(markup);

		Assert.isTrue(contentBegins != -1 && contentEnds != -1 && contentBegins != contentEnds,
				"invalid input: missing CLF2 comments");

		// <div class="center">

		String beforeContent = markup.substring(0, contentBegins);
		String afterContent = markup.substring(contentEnds);

		// standard stuff for CLF2, some site's look depends on it
		// beforeContent += "<div class=\"center\">";
		// afterContent = "</div>" + afterContent;

		//
		Assert.isTrue(baseUrl.indexOf("://") != -1, "no protocol on base URL");
		Assert.isTrue(importUrl.indexOf("://") != -1, "no protocol on import URL");

		//
		retval = new Template();
		retval.setBaseUrl(baseUrl);
		retval.setImportedFromUrl(importUrl);
		retval.setBeforeContent(beforeContent);
		retval.setAfterContent(afterContent);
		retval.setTemplateType(determineTemplateTypeCode(markup));

		// convert relative URLs to absolute URLs
		convertRelativeUrls(retval);

		// attempt to default the language stored with the template data
		determineTemplateLanguage(retval, markup);

		//
		return retval;
	}

	// ======================================================================

	@Override
	public String runtimeBeforeContent(HttpServletRequest request, Template template, String beforeContent) {

		String retval = beforeContent;

		String contextPath = request.getContextPath();

		// replace the title
		String siteTitle = getMessageSourceAccessor().getMessage("jsp.default.title");
		retval = retval.replaceFirst("(<title>(.*)</title>)", "<title>" + template.getSurvey().getDisplayTitle() + " - "
				+ siteTitle + "</title>");

		// CLF2 language toggle

		String otherLanguageCode = getOtherLanguageCode(template);
		String otherLanguageLabel = getOtherLanguageLabel(template);

		// String rTo =
		// request.getRequestURI().substring(contextPath.length());
		String rTo = "/html" + request.getPathInfo();

		if (StringUtils.hasText(request.getQueryString())) {

			// encoded for XSS prevention
			// try {
			String queryString = request.getQueryString();
			Assert.isTrue(queryString.indexOf("\"") == -1 && queryString.indexOf("'") == -1, "illegal return to query string");

			//
			rTo += "?" + queryString;
			// } catch (UnsupportedEncodingException e) {
			// throw new RuntimeException(e);
			// }
		}

		String replacement = "<a href='" + contextPath + "/html/" + otherLanguageCode + ".html?rTo=" + rTo + "'>"
				+ otherLanguageLabel + "</a>";

		int start = determineLanguageToggleBeginIndex(retval);
		int end = determineLanguageToggleEndIndex(retval);

		if (start != -1 && end != -1) {

			String content = retval.substring(start, end);

			String regex = "(<a.*>.*</a>)";
			String newContent = content.replaceFirst(regex, replacement);
			retval = retval.substring(0, start) + newContent + retval.substring(end);
		}

		// inject our CSS
		String responseCss = contextPath + "/incl/css/respond-styles.css";
		retval = retval.replace("</head>", "<link rel='stylesheet' type='text/css' href='" + responseCss + "'/></head>");

		return retval;
	}

	/**
	 * Get the "other" language code - works only for English and French users,
	 * otherwise defaults to English.
	 * 
	 * TODO this will need to change when our UI supports a third language
	 * 
	 * @return ISO3 language code
	 */
	private String getOtherLanguageCode(Template template) {
		// Locale locale = LocaleContextHolder.getLocale();
		if ("eng".equals(template.getSupportedLanguage().getIso3Lang())) {
			// English user
			return "fra";
		} else {
			return "eng";
		}
	}

	/**
	 * Get the "other" language label - works only for English and French users,
	 * otherwise defaults to English.
	 * 
	 * TODO this will need to change when our UI supports a third language
	 * 
	 * @return ISO3 language code
	 */
	private String getOtherLanguageLabel(Template template) {
		// Locale locale = LocaleContextHolder.getLocale();
		if ("eng".equals(template.getSupportedLanguage().getIso3Lang())) {
			// English user
			return getMessageSourceAccessor().getMessage("french", Locale.CANADA_FRENCH);
		} else {
			return getMessageSourceAccessor().getMessage("english", Locale.CANADA);
		}
	}

	/**
	 * Where is any one of the beginComment values?
	 * 
	 * @param markup
	 * @return
	 */
	protected int determineLanguageToggleBeginIndex(String markup) {
		int retval = -1;

		for (String match : LANGUAGE_TOGGLE_START) {
			retval = markup.indexOf(match);
			if (retval != -1) {
				// add the matching comment to the index
				retval += match.length();
				break;
			}
		}

		return retval;
	}

	/**
	 * Where is any one of the endComment values?
	 * 
	 * @param markup
	 * @return
	 */
	protected int determineLanguageToggleEndIndex(String markup) {
		int retval = -1;

		for (String match : LANGUAGE_TOGGLE_END) {
			retval = markup.lastIndexOf(match);
			if (retval != -1) {
				// NOTE: DO NOT add the matching comment to the index
				break;
			}
		}

		return retval;
	}

	// ======================================================================

	/**
	 * Where is any one of the beginComment values?
	 * 
	 * @param markup
	 * @return
	 */
	protected TemplateType determineTemplateTypeCode(String markup) {
		int retval = -1;

		for (String match : CONTENT_BEGINS) {
			retval = markup.indexOf(match);
			if (retval != -1) {
				// TODO more generic when we have >2 types
				String comment = markup.substring(retval, retval + match.length());
				if (comment.contains("OAS")) {
					return TemplateType.OAS_COMMENTS;
				} else {
					return TemplateType.CLF2_COMMENTS;
				}
			}
		}

		return TemplateType.NONE;
	}

	/**
	 * Where is any one of the beginComment values?
	 * 
	 * @param markup
	 * @return
	 */
	protected int determineContentBeginIndex(String markup) {
		int retval = -1;

		for (String match : CONTENT_BEGINS) {
			retval = markup.indexOf(match);
			if (retval != -1) {
				// add the matching comment to the index
				retval += match.length();
				break;
			}
		}

		return retval;
	}

	/**
	 * Where is any one of the endComment values?
	 * 
	 * @param markup
	 * @return
	 */
	protected int determineContentEndIndex(String markup) {
		int retval = -1;

		for (String match : CONTENT_ENDS) {
			retval = markup.lastIndexOf(match);
			if (retval != -1) {
				// NOTE: DO NOT add the matching comment to the index
				break;
			}
		}

		return retval;
	}

	/**
	 * CLF2 templated sites always allow language toggling, therefore the
	 * language presented in this template is "the other" official language.
	 * 
	 * @param retval
	 * @param markup
	 */
	protected void determineTemplateLanguage(Template retval, String markup) {

		Assert.notNull(retval);
		// notNull instead of hasText
		Assert.notNull(markup);

		if (hasLinkToFrench(markup)) {
			retval.setSupportedLanguage(supportedLanguageService.findByCode("eng"));
			return;
		}

		if (hasLinkToEnglish(markup)) {
			retval.setSupportedLanguage(supportedLanguageService.findByCode("fra"));
			return;
		}

		// otherwise, unknown language
	}

	protected boolean hasLinkToEnglish(String markup) {
		return linkToEnglishPattern.matcher(markup).matches();
		// return markup.matches(linkToEnglishRegex);
		// return Pattern.matches(linkToEnglishRegex, markup);
	}

	protected boolean hasLinkToFrench(String markup) {
		return linkToFrenchPattern.matcher(markup).matches();
		// return markup.matches(linkToFrenchRegex);
		// return Pattern.matches(linkToFrenchRegex, markup);
	}
}

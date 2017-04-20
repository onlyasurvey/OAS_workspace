package com.oas.controller.survey;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.oas.model.Attachment;
import com.oas.model.AttachmentPayload;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.templating.SurveyLogo;
import com.oas.service.DomainModelService;

/**
 * Handles binary streams related to {@link Survey}s - logos, attachments.
 * 
 * @author xhalliday
 * @since Refactored into this class from SurveyController September 6, 2009
 */
@Controller
public class SurveyBinariesController extends AbstractPublicFacingResponseController {

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	// ======================================================================

	/**
	 * Send a Survey Logo image to the user.
	 * 
	 * @param request
	 *            The HTTP request
	 * @param p
	 *            The "p" parameter, which is either "l"eft or "r"ight
	 * @param output
	 *            The output stream to send the image
	 * @throws IOException
	 */
	@RequestMapping(value = "/srvy/lg/*", params = { "p" })
	public void sendLogoImage(HttpServletRequest request, HttpServletResponse response, String p) throws IOException {

		// do not assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, false);

		// this breaks Preview - review later, maybe check owner?
		// Errors errors = new BindException(survey, "cmd");
		// respondabilityValidator.validate(survey, errors);
		//
		// if (errors.hasErrors()) {
		// // this is an invalid argument
		// // TODO send error message instead
		// throw new IllegalArgumentException("not respondable");
		// }

		SurveyLogo.PositionType position = null;
		if ("l".equals(p)) {
			position = SurveyLogo.PositionType.LEFT;
		} else if ("r".equals(p)) {
			position = SurveyLogo.PositionType.RIGHT;
		}

		// TODO send error image instead
		Assert.notNull(position, "invalid request");

		//

		SupportedLanguage language = getCurrentSupportedLanguage();
		String overrideLanguage = request.getParameter("l");
		if (StringUtils.hasText(overrideLanguage)) {
			language = supportedLanguageService.findByCode(overrideLanguage);
			Assert.notNull(language, "invalid override language");
		}

		//
		// caching-headers
		//
		// it is up to the caller to append a parameter indicating the
		// version - ie, a last modified date. This code simply sets a
		// far-future-expires header with the expectation that the front-end
		// (ie image element) does something to the URL to make it unique to
		// uploadTime
		//
		domainModelService.addFarFutureExpiresHeader(response);

		try {
			byte[] payload = surveyService.getLogoData(survey, language, position);

			OutputStream output = response.getOutputStream();
			output.write(payload);
			output.flush();

		} catch (IOException e) {
			log.error("IOException streaming " + position + " logo for survey#" + survey.getId() + " to user", e);
			throw e;
		}
	}

	// ======================================================================

	/**
	 * Sends a Question Attachment to a user.
	 * 
	 * @param request
	 *            The HTTP request
	 * @param output
	 *            The output stream to send the image
	 * @throws IOException
	 */
	@RequestMapping(value = "/srvy/att/*")
	public void sendAttachment(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// , OutputStream output
		// must exist
		Question question = getEntityFromRestfulUrl(Question.class, request);

		//
		SupportedLanguage language = getCurrentSupportedLanguage();
		String overrideLanguage = request.getParameter("l");
		if (StringUtils.hasText(overrideLanguage)) {
			language = supportedLanguageService.findByCode(overrideLanguage);
			Assert.notNull(language, "invalid override language");
		}

		Attachment attachment = surveyService.getQuestionImage(question);
		// TODO replace with error image
		Assert.notNull(attachment);

		AttachmentPayload payload = attachment.getPayloads().get(language);
		// TODO default to any other language then to built-in error image
		if (payload == null) {
			if (attachment.getPayloads().size() > 0) {
				SupportedLanguage firstFoundLanguage = attachment.getPayloads().keySet().iterator().next();
				payload = attachment.getPayloads().get(firstFoundLanguage);
			}
		}
		Assert.notNull(payload);

		// content-type
		response.setContentType(attachment.getContentType());

		//
		// caching-headers
		//
		// it is up to the caller to append a parameter indicating the
		// version - ie, a last modified date. This code simply sets a
		// far-future-expires header with the expectation that the front-end
		// (ie image element) does something to the URL to make it unique to
		// uploadTime
		//
		domainModelService.addFarFutureExpiresHeader(response);

		try {
			byte[] bytes = Base64.decodeBase64(payload.getPayload().getBytes());

			log.info("Sending attachment #" + attachment.getId() + "/" + language + " (" + attachment.getContentType() + ", "
					+ bytes.length + " bytes)to user");

			//
			OutputStream output = response.getOutputStream();
			output.write(bytes);
			output.flush();

		} catch (IOException e) {
			log
					.error("IOException streaming image attachment for question#" + question.getId() + "/"
							+ language.getIso3Lang(), e);
			throw e;
		}
	}

	// ======================================================================
}

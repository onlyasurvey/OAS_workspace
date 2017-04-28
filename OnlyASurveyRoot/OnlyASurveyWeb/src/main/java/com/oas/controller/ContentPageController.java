package com.oas.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.oas.service.DomainModelService;

/**
 * Generic controller for static content pages.
 * 
 * @author xhalliday
 * @since October 18, 2008
 */
@Controller
public class ContentPageController extends AbstractOASController {

	/** Default 404 error. */
	private static final byte[] notFound = "<h1>404</h1>".getBytes();

	/** For loading public content pages. */
	@Autowired
	private DomainModelService domainModelService;

	/**
	 * DELETEME
	 */
	@RequestMapping("/tac.html")
	public ModelAndView termsAndConditions() {
		return new ModelAndView("/content/termsAndConditions");
	}

	/**
	 * DELETEME
	 */
	@RequestMapping("/priv.html")
	public ModelAndView privacyPolicy() {
		return new ModelAndView("/content/privacyPolicy");
	}

	/**
	 * Load a page from the domain model service based on the last part of the
	 * path in the URI.
	 * 
	 * Mapped path includes "*.*" to force there to be an extension, since there
	 * should always be and the results otherwise could be unpredictable.
	 * 
	 * @param request
	 * @param outputStream
	 *            To stream non-HTML content
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/cms/**.*", method = RequestMethod.GET)
	public ModelAndView showPage(HttpServletRequest request, OutputStream outputStream) {

		ModelMap model = new ModelMap();

		// determine which page (everything after /cms/)
		String pageName = getRestfulPath(request);

		//

		byte[] content = null;

		// TODO this blanket try/catch is pretty minimalistic
		try {
			// load content page
			content = domainModelService.getPublicContent(pageName);

			if (content != null) {

				// if extension is HTML then wrap it in our L&F
				// if extension is not HTML then stream it to the client
				// (images, etc)
				if (!"html".equals(FilenameUtils.getExtension(pageName))) {
					//
					outputStream.write(content);

					// return null (no view), or if on exception then log it
					// flows to the view below
					return null;
				}
			}
		} catch (RuntimeException e) {
			// wrapped exceptions
			log.error("404 not found (RuntimeException)" + FilenameUtils.normalize(pageName), e);
			// content defaults to 404 error message above
		} catch (IOException e) {
			log.error("404 not found (IOException)" + FilenameUtils.normalize(pageName), e);
			// content defaults to 404 error message above
		}

		if (content == null) {
			content = notFound;
		}

		// logic falls through to here if the URL ends in ".html" if on IO
		// exception
		// convert to string explicitly since view uses it in an expression
		model.addAttribute("publicPageContent", new String(content));

		//
		applyWideLayout(request);
		return new ModelAndView("/content/wrapPage", model);
	}
}

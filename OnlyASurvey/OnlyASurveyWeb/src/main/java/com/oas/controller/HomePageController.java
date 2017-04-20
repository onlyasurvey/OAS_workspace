package com.oas.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.oas.service.DomainModelService;

/**
 * Shows the Home Page (/oas.html).
 */
@Controller
public class HomePageController extends AbstractOASController {

	/** General domain service. */
	@Autowired
	private DomainModelService domainModelService;

	/** Site-relative path where the Public Site Content is located. */
	private final static String PUBLIC_PAGE_PATH = "public-home-page.html";

	/**
	 * Show the Home Page.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/oas.html")
	protected ModelAndView homePage(HttpServletRequest request) {

		// load the actual page content using Public Content

		// show view
		applyWideLayout(request);
		return new ModelAndView("/homePage", "pageContent", loadHomePageContent());
	}

	/**
	 * Load /homepage from the Public Site Content repo
	 * 
	 * @return Content or an empty string
	 */
	private String loadHomePageContent() {
		// it comes in as byte[]
		byte[] bytes = domainModelService.getPublicContent(PUBLIC_PAGE_PATH);
		String content = "";
		if (bytes == null) {
			log.warn("Unable to load " + PUBLIC_PAGE_PATH + " from PublicSiteContent repo");
		} else {
			content = new String(bytes);
		}
		return content;
	}
}

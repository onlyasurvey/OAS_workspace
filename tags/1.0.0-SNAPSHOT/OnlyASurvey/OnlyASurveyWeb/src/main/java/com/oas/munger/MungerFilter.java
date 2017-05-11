package com.oas.munger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class MungerFilter implements Filter {

	/**
	 * Log.
	 */
	protected Logger log = Logger.getLogger(getClass());

	/** Stores context path so that tags can access it. */
	private static String contextPath;

	/** Stores servlet path so that tags can access it. */
	private static String servletPath;

	/**
	 * Allows the site owner or developer to disable the munger for all
	 * requests.
	 */
	private static boolean disableMungerCompletely = true;

	// ======================================================================

	public MungerFilter() {
		super();
		disableMungerCompletely = false;
	}

	// ======================================================================

	public static String getContextPath() {
		return contextPath;
	}

	public static void setContextPath(String path) {
		contextPath = path;
	}

	public static String getServletPath() {
		return servletPath;
	}

	public static void setServletPath(String path) {
		servletPath = path;
	}

	// ======================================================================

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		// initialize this variable exactly once
		if (contextPath == null) {
			synchronized (this) {
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				// contextPath = httpRequest.getContextPath();
				setContextPath(httpRequest.getContextPath());
			}

			log.info("Setting MungerFilter.contextPath to " + contextPath);
		}

		// initialize this variable exactly once
		if (servletPath == null) {
			synchronized (this) {
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				// servletPath = httpRequest.getServletPath();
				setServletPath(httpRequest.getServletPath());
			}

			log.info("Setting MungerFilter.servletPath to " + servletPath);
		}

		if (StringUtils.hasText(request.getParameter("MUNGER_DISABLED"))) {
			setDisabled("true".equals(request.getParameter("MUNGER_DISABLED")));
		}

		MungerRequestWrapper wrappedRequest = new MungerRequestWrapper((HttpServletRequest) request);
		MungerResponseWrapper wrappedResponse = new MungerResponseWrapper((HttpServletResponse) response);

		chain.doFilter(wrappedRequest, wrappedResponse);
	}

	// ======================================================================

	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	// ======================================================================

	public static boolean excludeFromEncoding(String path) {
		return isDisabled() || (StringUtils.hasText(path) && (path.startsWith("/incl/") || path.startsWith("j_")));
	}

	// ======================================================================

	public static void setDisabled(boolean disabled) {
		disableMungerCompletely = disabled;
	}

	public static boolean isDisabled() {
		return disableMungerCompletely;
	}
}

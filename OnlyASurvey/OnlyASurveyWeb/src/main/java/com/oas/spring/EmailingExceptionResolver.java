package com.oas.spring;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Map;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import ca.inforealm.core.security.SecurityUtil;

/**
 * Extension to SimpleMappingExceptionResolver that sends an email.
 * 
 * @author xhalliday
 * @since 2009-10-17
 */
public class EmailingExceptionResolver extends SimpleMappingExceptionResolver {

	/** Destination email address. */
	private String mailTo;

	/** In case configuration is missing. */
	private static final String FALLBACK_EMAIL_ADDRESS = "production-errors@onlyasurvey.com";

	/** Mail sender. */
	@Autowired
	private JavaMailSender javaMailSender;

	/**
	 * Resolve (handle) the exception.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param response
	 *            {@link HttpServletResponse}
	 * @param handler
	 *            Object
	 * @param ex
	 *            {@link Exception}
	 */
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		// invoke parent functionality
		ModelAndView retval = super.doResolveException(request, response, handler, ex);

		// send an email
		sendEmail(ex, request);

		//
		return retval;
	}

	/**
	 * Sends an email with the exception.
	 * 
	 * @param ex
	 *            {@link Exception}
	 * @param request
	 *            {@link HttpServletRequest}
	 */
	private void sendEmail(Exception ex, HttpServletRequest request) {
		logger.error("Sending mail for user exception", ex);
		if (ex == null) {
			logger.error("sendMail received a null Exception");
			return;
		}

		MimeMessage mail = javaMailSender.createMimeMessage();
		try {
			mail.setFrom(new InternetAddress("production-errors@onlyasurvey.com"));

			if (!StringUtils.hasText(mailTo)) {
				mailTo = FALLBACK_EMAIL_ADDRESS;
			}

			mail.addRecipient(RecipientType.TO, new InternetAddress(mailTo));
			mail.setSubject("Production exception: " + ex.getMessage());

			// to capture the stack trace
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PrintStream stream = new PrintStream(output);
			// PrintWriter writer = new PrintWriter(output);
			// ex.printStackTrace(writer);
			ex.printStackTrace(stream);

			// the text
			String messageText = "Production error occurred: " + ex.getMessage() + "\n\nHost: " + request.getServerName()
					+ "\nURI: " + request.getRequestURI() + "\n\n";

			if (SecurityUtil.isSecureContext()) {
				// a user is logged in
				messageText += "User: " + SecurityUtil.getCurrentUser().getDisplayTitle() + "\n\n";
			}

			messageText += dumpAttributes(request);

			messageText += output.toString();

			mail.setText(messageText);

			//
			javaMailSender.send(mail);

		} catch (Exception e) {
			// catching Exception is nasty, but this function is business
			// critical and mail sending is secondary: cannot allow a failure
			// here to cause a stacktrace to be shown on a user's screen
			if (logger.isDebugEnabled()) {
				// this is typically very verbose and useless: the mail server
				// is down
				// logger.error("Unable to send email: ", e);
				logger.error("Unable to send email: " + e.getMessage());
			}

		}
	}

	private String dumpAttributes(HttpServletRequest request) {
		String retval = "";

		@SuppressWarnings("unchecked")
		Map<String, Object> parameterMap = (Map<String, Object>) request.getParameterMap();

		//
		retval += "request parameter map:\n";
		for (String key : parameterMap.keySet()) {
			retval += "  " + key + " = " + request.getParameter(key) + "\n";
		}
		retval += "\n\n";

		//
		{
			retval += "request attribute map:\n";
			@SuppressWarnings("unchecked")
			Enumeration<String> attributeNames = request.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String key = attributeNames.nextElement();
				retval += "  " + key + " = " + request.getAttribute(key) + "\n";
			}
			retval += "\n\n";
		}

		//
		{
			retval += "session attribute map:\n";
			@SuppressWarnings("unchecked")
			Enumeration<String> attributeNames = request.getSession().getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String key = attributeNames.nextElement();
				retval += "  " + key + " = " + request.getSession().getAttribute(key) + "\n";
			}
			retval += "\n\n";
		}

		return retval;
	}

	/**
	 * Accessor.
	 * 
	 * @param mailTo
	 *            the mailTo to set
	 */
	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}
}

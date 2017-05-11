package com.oas.web.tag;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.springframework.util.StringUtils;

/**
 * <input type='submit' name='_lecnac' class='button' value='message:cancel'/>
 * 
 * NOTE: lecnac is "cancel" reversed, used in AbstractOASController.isCancel().
 * 
 * @author xhalliday
 * @since February 13, 2008
 */
public class CancelButtonTag extends TagSupport {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -3121695590913405133L;

	/** The message code to use for the button label. */
	private String code = "cancel";

	/** (Optional) Message code for a title attribute. */
	private String title = null;

	// ======================================================================

	public CancelButtonTag() {
		super();
	}

	// ======================================================================

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int doEndTag() throws JspException {

		try {
			pageContext.getOut().print(generateTagMarkup());
		} catch (IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}

	protected String generateTagMarkup() {

		String message = "BUG";
		String titleAttribute = "";

		// copied from MesageSupport
		LocalizationContext locCtxt = null;
		Tag t = findAncestorWithClass(this, BundleSupport.class);
		if (t != null) {
			// use resource bundle from parent <bundle> tag
			BundleSupport parent = (BundleSupport) t;
			locCtxt = parent.getLocalizationContext();
		} else {
			locCtxt = BundleSupport.getLocalizationContext(pageContext);
		}

		// TODO this is overly complex
		if (locCtxt == null) {
			message = "???" + getCode() + "???";
		} else {
			ResourceBundle bundle = locCtxt.getResourceBundle();
			if (bundle != null) {
				message = bundle.getString(getCode());

				if (StringUtils.hasText(getTitle())) {
					String titleValue = bundle.getString(getTitle());
					if (StringUtils.hasText(titleValue)) {
						titleAttribute = "title=\"" + titleValue + "\"";
					} else {
						titleAttribute = "title=\"???" + getTitle() + "???\"";

					}
				}
			}
		}

		return "<input type='submit' name='_lecnac' class='button' value=\"" + message + "\" " + titleAttribute + "/>";
	}

}

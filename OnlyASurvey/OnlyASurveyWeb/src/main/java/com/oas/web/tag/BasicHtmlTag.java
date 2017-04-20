package com.oas.web.tag;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.tag.common.core.NullAttributeException;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.util.Assert;

/**
 * Tag that allows a white-list of HTML tags to be output, and otherwise
 * operates the same as the core out tag. This code is not particularly pretty
 * because much of it's content is copied in from OutTag and OutSupport - little
 * effort has been put into prettiness.
 * 
 * @author xhalliday
 * @since May 17, 2009
 */
public class BasicHtmlTag extends BodyTagSupport {

	/** AntiSamy config file to use. */
	private static final String POLICY_FILE_LOCATION_SHORT = "/config/antisamy-usertext.xml";

	/** Static cached copy. */
	private static final AntiSamy antiSamy;

	// TODO threadlocal cache for antiSamy?
	static {
		URL url = BasicHtmlTag.class.getResource(POLICY_FILE_LOCATION_SHORT);
		Assert.notNull(url, "unable to find HTML cleaning policy resource");
		String POLICY_FILE_LOCATION_ABSOLUTE = url.getFile();

		// according to
		// https://lists.owasp.org/pipermail/owasp-antisamy/2009-March/000137.html
		// the Policy object is threadsafe if you don't call a
		// particular method, so here we use a cached copy

		try {
			Policy policy = Policy.getInstance(POLICY_FILE_LOCATION_ABSOLUTE);
			antiSamy = new AntiSamy(policy);
		} catch (PolicyException e) {
			throw new RuntimeException(e);
		}

	}
	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -1979262912362248487L;

	// *********************************************************************
	// Internal state

	protected Object value; // tag attribute
	protected String def; // tag attribute
	protected boolean escapeXml; // tag attribute

	/** Non-space body needed? */
	private boolean needBody;

	// /** List of blessed tags - i.e., those that do not get escaped. */
	// private List<String> blessedTags;
	//
	// /** List of blessed entities - i.e., those that do not get escaped. */
	// private List<String> blessedEntities;
	//
	// /** How long is the longest blessed tag? Used for read-aheads. */
	// private int longestBlessedTag = -1;
	//
	// /** How long is the longest blessed entity? Used for read-aheads. */
	// private int longestBlessedEntity = -1;

	// *********************************************************************
	// 'Private' state (implementation details)

	private String value_; // stores EL-based property
	private String default_; // stores EL-based property
	private String escapeXml_; // stores EL-based property

	// ======================================================================

	/** Default constructor. */
	public BasicHtmlTag() {
		// initialiseBlessedTagList();
		// initialiseBlessedEntityList();
	}

	/**
	 * Initialize the list of blessed tags.
	 */
	// private void initialiseBlessedTagList() {
	//
	// // http://redmine.itsonlyasurvey.com/issues/show/72
	// blessedTags = new ArrayList<String>();
	//
	// addBlessedTagPair("div");
	// addBlessedTagPair("p");
	// addBlessedTagPair("strong");
	// addBlessedTagPair("em");
	// addBlessedTagPair("ul");
	// addBlessedTagPair("ol");
	// addBlessedTagPair("dl");
	// addBlessedTagPair("dd");
	// addBlessedTagPair("li");
	// addBlessedTagPair("dt");
	//
	// // list.add("<a>");
	// // list.add("</a>");
	//
	// addBlessedLiteralTag("<br/>");
	// addBlessedLiteralTag("<br />");
	//
	// addBlessedLiteralTag("<span style=\"text-decoration: italic;\">");
	// addBlessedLiteralTag("<span style=\"font-weight: normal;\">");
	// addBlessedLiteralTag("<span style=\"font-weight: bold;\">");
	// addBlessedLiteralTag("<span style=\"text-decoration: line-through;\">");
	// addBlessedLiteralTag("<span style=\"text-decoration: underline;\">");
	// addBlessedLiteralTag("<span style=\"text-decoration: none;\">");
	// addBlessedLiteralTag("</span>");
	//
	// // set the longest tag length for tags
	// for (String tag : blessedTags) {
	// if (tag.length() > longestBlessedTag) {
	// longestBlessedTag = tag.length();
	// }
	// }
	// }

	/**
	 * Initialize the list of blessed entities.
	 */
	// private void initialiseBlessedEntityList() {
	//
	// // http://redmine.itsonlyasurvey.com/issues/show/72
	// blessedEntities = new ArrayList<String>();
	//
	// addBlessedEntity("&nbsp;");
	//
	// // set the longest tag length for entities
	// for (String entity : blessedEntities) {
	// if (entity.length() > longestBlessedEntity) {
	// longestBlessedEntity = entity.length();
	// }
	// }
	// }
	//
	// private void addBlessedLiteralTag(String tagName) {
	// blessedTags.add(tagName);
	// }
	//
	// private void addBlessedTagPair(String tagName) {
	// blessedTags.add("<" + tagName + ">");
	// blessedTags.add("</" + tagName + ">");
	// }
	//
	// private void addBlessedEntity(String entity) {
	// blessedEntities.add(entity);
	// }

	// evaluates expression and chains to parent
	@Override
	public int doStartTag() throws JspException {

		// evaluate any expressions we were passed, once per invocation
		evaluateExpressions();

		needBody = false; // reset state related to 'default'
		this.bodyContent = null; // clean-up body (just in case container is
		// pooling tag handlers)

		try {

			// print value if available; otherwise, try 'default'
			if (value != null) {
				output(pageContext, escapeXml, value);
				return SKIP_BODY;
			} else {
				// if we don't have a 'default' attribute, just go to the body
				if (def == null) {
					needBody = true;
					return EVAL_BODY_BUFFERED;
				}

				// if we do have 'default', print it
				if (def != null) {
					// some programmer-supplied "good default"
					output(pageContext, escapeXml, def);
				}

				return SKIP_BODY;
			}
		} catch (IOException ex) {
			throw new JspException(ex.toString(), ex);
		}
	}

	/**
	 * Output the content of the tag, generally using AntiSamy to clean
	 * user-supplied input.
	 * 
	 * @param pageContext
	 *            {@link PageContext}
	 * @param escapeXml
	 *            Whether or not to clean input using AntiSamy
	 * @param obj
	 *            Source object, either a reader or a string.
	 */
	public void output(PageContext pageContext, boolean escapeXml, Object obj) throws IOException {
		JspWriter writer = pageContext.getOut();

		if (!escapeXml) {
			// write chars as is
			if (obj instanceof Reader) {
				Reader reader = (Reader) obj;
				char[] buf = new char[4096];
				int count;
				while ((count = reader.read(buf, 0, 4096)) != -1) {
					writer.write(buf, 0, count);
				}
			} else {
				writer.write(obj.toString());
			}
		} else {
			//
			// clean user-supplied input
			//
			String dirtyInput = null;

			if (obj instanceof Reader) {

				StringBuffer buffer = new StringBuffer();

				Reader reader = (Reader) obj;
				char[] buf = new char[4096];
				int count;
				while ((count = reader.read(buf, 0, 4096)) != -1) {
					// stuff the buffer
					buffer.append(buf, 0, count);
				}

				// write the entire buffer
				dirtyInput = buffer.toString();
			} else {
				//
				dirtyInput = obj.toString();
			}

			// AntiSamy for cleaning user input
			try {

				// according to
				// https://lists.owasp.org/pipermail/owasp-antisamy/2009-March/000137.html
				// the Policy object is threadsafe if you don't call a
				// particular method, so here we use a cached copy

				// TODO threadlocal cache for antiSamy?
				CleanResults cr = antiSamy.scan(dirtyInput);

				//
				writer.write(cr.getCleanHTML());

			} catch (ScanException ex) {
				throw new IOException(ex.toString(), ex);
			} catch (PolicyException ex) {
				throw new IOException(ex.toString(), ex);
			}
		}
	}

	// ======================================================================

	/**
	 * Prints the body if necessary; reports errors. May not strictly be
	 * required here since the parent also has it, but we must define the
	 * needBody due to it's use in doStartTag so it's copied here for
	 * completeness, since setting this.needBody may not set the parent's copy.
	 * 
	 * Dirty C&P job.
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (!needBody)
				return EVAL_PAGE; // nothing more to do

			// trim and print out the body
			if (bodyContent != null && bodyContent.getString() != null)
				output(pageContext, escapeXml, bodyContent.getString().trim());
			return EVAL_PAGE;
		} catch (IOException ex) {
			throw new JspException(ex.toString(), ex);
		}
	}

	/**
	 * Evaluates expressions as necessary.
	 * 
	 * @throws JspException
	 *             JSP exception
	 */
	private void evaluateExpressions() throws JspException {
		try {
			value = ExpressionUtil.evalNotNull("out", "value", value_, Object.class, this, pageContext);
		} catch (NullAttributeException ex) {
			// explicitly allow 'null' for value
			value = null;
		}
		try {
			def = (String) ExpressionUtil.evalNotNull("out", "default", default_, String.class, this, pageContext);
		} catch (NullAttributeException ex) {
			// explicitly allow 'null' for def
			def = null;
		}
		escapeXml = true;
		Boolean escape = ((Boolean) ExpressionUtil.evalNotNull("out", "escapeXml", escapeXml_, Boolean.class, this, pageContext));
		if (escape != null)
			escapeXml = escape.booleanValue();
	}

	public void setValue(String value_) {
		this.value_ = value_;
	}

	public void setDefault(String default_) {
		this.default_ = default_;
	}

	public void setEscapeXml(String escapeXml_) {
		this.escapeXml_ = escapeXml_;
	}
}

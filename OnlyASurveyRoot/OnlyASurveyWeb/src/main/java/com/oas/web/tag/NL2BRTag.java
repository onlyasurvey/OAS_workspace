package com.oas.web.tag;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.tag.common.core.NullAttributeException;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.springframework.util.Assert;

/**
 * Replaces newlines with &lt;br/&gt;, derives from OutSupport to behave like
 * &lt;c:out/&gt; re: escaping tags.
 * 
 * @author xhalliday
 */
public class NL2BRTag extends BodyTagSupport {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -1979333912362231287L;

	protected Object value; // tag attribute
	protected String def; // tag attribute

	/** Non-space body needed? */
	private boolean needBody;

	// *********************************************************************
	// 'Private' state (implementation details)

	private String value_; // stores EL-based property

	// ======================================================================

	/** Default constructor. */
	public NL2BRTag() {
	}

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
				output(pageContext, value);
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
					output(pageContext, def);
				}

				return SKIP_BODY;
			}
		} catch (IOException ex) {
			throw new JspException(ex.toString(), ex);
		}
	}

	/**
	 * Output the content of the tag, replacing newlines and escaping tags.
	 * 
	 * @param pageContext
	 *            {@link PageContext}
	 * @param escapeXml
	 *            Whether or not to clean input using AntiSamy
	 * @param obj
	 *            Source object, either a reader or a string.
	 */
	protected void output(PageContext pageContext, Object obj) throws IOException {
		JspWriter jspWriter = pageContext.getOut();

		CharArrayWriter bufferWriter = new CharArrayWriter();
		CharArrayWriter out = new CharArrayWriter();
		// ByteArrayOutputStream writer = new ByteArrayOutputStream();
		// StringWriter writer = new StringWriter();

		// stuff the object's contents into buffer
		if (obj instanceof Reader) {
			Reader reader = (Reader) obj;

			char[] buf = new char[4096];
			while (reader.read(buf, 0, 4096) != -1) {
				bufferWriter.write(buf);
			}
		} else {
			bufferWriter.write(obj.toString());
		}

		// escaping
		char[] buffer = bufferWriter.toCharArray();
		Assert.notNull(buffer);

		int length = buffer.length;
		for (int i = 0; i < length; i++) {
			char c = buffer[i];

			switch (c) {
			case '&':
				out.write("&amp;");
				break;
			case '<':
				out.write("&lt;");
				break;
			case '>':
				out.write("&gt;");
				break;
			case '"':
				out.write("&#034;");
				break;
			case '\'':
				out.write("&#039;");
				break;
			case '\n':
				out.write("<br/>");
				break;
			default:
				out.write(c);
				break;
			}
		}

		// flush it out
		jspWriter.write(out.toCharArray());
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
				output(pageContext, bodyContent.getString().trim());
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
	}

	public void setValue(String value_) {
		this.value_ = value_;
	}
}

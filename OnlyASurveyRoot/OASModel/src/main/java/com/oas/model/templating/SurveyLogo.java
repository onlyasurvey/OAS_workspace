package com.oas.model.templating;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.oas.model.BaseObject;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

/**
 * Logo image attachment for Surveys.
 * 
 * @author xhalliday
 * @since February 28, 2009
 */
@Entity
@Table(schema = "oas", name = "survey_logo")
public class SurveyLogo extends BaseObject {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -1049908395690413860L;

	// ======================================================================

	/** Position of a logo - left, right. */
	public enum PositionType {
		LEFT, RIGHT;

		/** Is the passed position type "LEFT"? */
		public static final boolean isLeft(PositionType type) {
			return LEFT.equals(type);
		}

		/** Is the passed position type "RIGHT"? */
		public static final boolean isRight(PositionType type) {
			return RIGHT.equals(type);
		}

		/**
		 * Returns LEFT or RIGHT if the input is "l" or "r", respectively.
		 * 
		 * @param letter
		 *            "l" or "r" characters are valid input.
		 * @return PositionType
		 */
		public static PositionType fromLetter(char letter) {
			switch (letter) {
			case 'l':
				return LEFT;
			case 'r':
				return RIGHT;
			default:
				return null;
			}
		}
	}

	// ======================================================================

	/** The Survey this logo is for. */
	@ManyToOne
	@JoinColumn(name = "survey_id")
	private Survey survey;

	/** Language this logo is in. */
	@ManyToOne
	@JoinColumn(name = "language_id")
	private SupportedLanguage supportedLanguage;

	/** Position for this logo - left, right. */
	@Enumerated(EnumType.STRING)
	@Column(name = "position")
	private PositionType position;

	/** Widthof the image in pixels. */
	@Column(name = "width")
	private int width;

	/** Height of the image in pixels. */
	@Column(name = "height")
	private int height;

	/** Size in bytes of the decoded payload. */
	@Column(name = "size")
	private int size;

	/** Content Type. */
	@Column(name = "content_type")
	private String contentType;

	/** Text used for alt/title attributes. */
	@Column(name = "alt_text", length = 255)
	private String altText;

	/** The image data, base64-encoded; lazy-loaded. */
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "payload")
	private String payload;

	/** Timestamp when the image was uploaded, for caching. */
	@Column(name = "upload_time")
	private Date uploadTime;

	// ======================================================================

	public SurveyLogo() {
	}

	public SurveyLogo(Survey survey, SupportedLanguage supportedLanguage) {
		setSurvey(survey);
		setSupportedLanguage(supportedLanguage);
	}

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @param survey
	 *            the survey to set
	 */
	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	/**
	 * @return the position
	 */
	public PositionType getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(PositionType position) {
		this.position = position;
	}

	/**
	 * @return the supportedLanguage
	 */
	public SupportedLanguage getSupportedLanguage() {
		return supportedLanguage;
	}

	/**
	 * @param supportedLanguage
	 *            the supportedLanguage to set
	 */
	public void setSupportedLanguage(SupportedLanguage supportedLanguage) {
		this.supportedLanguage = supportedLanguage;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * @return the uploadtime
	 */
	public Date getUploadTime() {
		return uploadTime;
	}

	/**
	 * @param uploadtime
	 *            the uploadtime to set
	 */
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	/**
	 * @return the altText
	 */
	public String getAltText() {
		return altText;
	}

	/**
	 * @param altText
	 *            the altText to set
	 */
	public void setAltText(String altText) {
		this.altText = altText;
	}
}

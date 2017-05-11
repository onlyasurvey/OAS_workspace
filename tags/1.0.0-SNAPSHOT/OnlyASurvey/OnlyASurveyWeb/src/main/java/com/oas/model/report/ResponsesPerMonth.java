package com.oas.model.report;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.util.Assert;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.Survey;

/**
 * View that provides the number of responses to a survey per date.
 * 
 * @author xhalliday
 * @since September 27, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_responses_per_month")
public class ResponsesPerMonth extends AbstractResourceModel implements Comparable {

	@EmbeddedId
	private SurveyDateCompositeID id;

	@Column(name = "response_count", updatable = false, insertable = false)
	private Long count;

	// ======================================================================
	public ResponsesPerMonth() {
		super();
	}

	public ResponsesPerMonth(Survey survey, Date date, Long count) {
		super();

		setId(new SurveyDateCompositeID(survey, date));
		setCount(count);
	}

	// ======================================================================
	@Override
	public int compareTo(Object o) {
		// must be the correct type
		Assert.isAssignable(ResponsesPerMonth.class, o.getClass());

		ResponsesPerMonth other = (ResponsesPerMonth) o;
		return getId().getDate().compareTo(other.getId().getDate());
	}

	// ======================================================================

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @return the id
	 */
	public SurveyDateCompositeID getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(SurveyDateCompositeID id) {
		this.id = id;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(Long count) {
		this.count = count;
	}
}

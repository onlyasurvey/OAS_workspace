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
@Table(schema = "oas", name = "vw_responses_per_day")
public class ResponsesPerDay extends AbstractResourceModel implements Comparable {

	@EmbeddedId
	private SurveyDateCompositeID id;

	@Column(name = "response_count", updatable = false, insertable = false)
	private Long count;

	// ======================================================================
	public ResponsesPerDay() {
		super();
	}

	public ResponsesPerDay(Survey survey, Date date, Long count) {
		super();

		setId(new SurveyDateCompositeID(survey, date));
		setCount(count);
	}

	// ======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = prime * result + ((id == null) ? 0 : id.hashCode());
	// return result;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (getClass() != obj.getClass())
	// return false;
	// final ResponsesPerDay other = (ResponsesPerDay) obj;
	// if (id == null) {
	// if (other.id != null)
	// return false;
	// } else if (!id.equals(other.id))
	// return false;
	// return true;
	// }
	// ======================================================================
	@Override
	public int compareTo(Object o) {
		// must be the correct type
		Assert.isAssignable(ResponsesPerDay.class, o.getClass());

		ResponsesPerDay other = (ResponsesPerDay) o;
		return getId().getDate().compareTo(other.getId().getDate());
	}

	// ======================================================================

	// /**
	// * @return the survey
	// */
	// public Survey getSurvey() {
	// return survey;
	// }
	//
	// /**
	// * @return the date
	// */
	// public Date getDate() {
	// return date;
	// }

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

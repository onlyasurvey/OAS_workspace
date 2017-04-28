package com.oas.model;

import java.io.Serializable;

/**
 * Indicates that a given domain object is survey-related, meaning you can get
 * and set the related Survey object.
 * 
 * @author xhalliday
 * @since September 15, 2008
 */
public interface SurveyRelatedData extends Serializable {

	public Survey getSurvey();
}

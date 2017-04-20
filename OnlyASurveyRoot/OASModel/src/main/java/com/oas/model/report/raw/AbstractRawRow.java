package com.oas.model.report.raw;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@SuppressWarnings("serial")
abstract public class AbstractRawRow implements Serializable {

	/** Response creation date. */
	@Column(name = "date_created")
	private Date dateCreated;

	/** Response completion (close) date. */
	@Column(name = "date_closed")
	private Date dateClosed;

	// ======================================================================

	public AbstractRawRow() {
	}

	/**
	 * Return the appropriate composite key PK.
	 * 
	 * @return the id
	 */
	abstract public AbstractRawRowId getId();

	// ======================================================================

	/**
	 * @return the date
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the date
	 */
	public Date getDateClosed() {
		return dateClosed;
	}

}

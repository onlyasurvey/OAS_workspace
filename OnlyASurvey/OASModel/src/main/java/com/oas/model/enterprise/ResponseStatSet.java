package com.oas.model.enterprise;

import javax.persistence.Embeddable;

import org.springframework.util.Assert;

/**
 * A set of stats used by {@link QuickStats}.
 * 
 * @author xhalliday
 * @since 2009-10-13
 */
@Embeddable
public class ResponseStatSet {

	/** Today. */
	private Long today;

	/** Last 7 days. */
	private Long lastWeek;

	/** Month-to-date. */
	private Long thisMonth;

	/** Last 90 days. */
	private Long lastQuarter;

	/** Total. */
	private Long total;

	// ==================================================================

	/**
	 * Default constructor.
	 */
	public ResponseStatSet() {
	}

	/**
	 * Complex constructor.
	 * 
	 * @param today
	 *            Count today
	 * @param lastWeek
	 *            Count in the last week (7 days)
	 * @param thisMonth
	 *            Count this month
	 * @param lastQuarter
	 *            Count in the last 90 days
	 * @param total
	 *            Total
	 */
	public ResponseStatSet(Long today, Long lastWeek, Long thisMonth, Long lastQuarter, Long total) {
		this.today = today;
		this.lastWeek = lastWeek;
		this.thisMonth = thisMonth;
		this.lastQuarter = lastQuarter;
		this.total = total;
	}

	// ==================================================================

	/**
	 * {@inheritDoc}
	 * 
	 * @todo This is a really cheap implementation because it depends on
	 *       .toString().
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return toString().equals(obj.toString());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @todo This is a really cheap implementation because it depends on
	 *       .toString().
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return "statSet[total=" + getTotal() + ",today=" + getToday() + ",last-week=" + getLastWeek() + ",month-to-date="
				+ getThisMonth() + ",last-quarter=" + getLastQuarter() + "]";
	}

	// ==================================================================

	public ResponseStatSet subtract(ResponseStatSet other) {
		Assert.notNull(other);
		ResponseStatSet retval = new ResponseStatSet(//
				getToday() - other.getToday()//
				, getLastWeek() - other.getLastWeek()//
				, getThisMonth() - other.getThisMonth()//
				, getLastQuarter() - other.getLastQuarter()//
				, getTotal() - other.getTotal()//
		);
		return retval;
	}

	// ==================================================================

	/**
	 * Accessor.
	 * 
	 * @return the today
	 */
	public Long getToday() {
		return today;
	}

	/**
	 * Accessor.
	 * 
	 * @return the thisMonth
	 */
	public Long getThisMonth() {
		return thisMonth;
	}

	/**
	 * Accessor.
	 * 
	 * @return the total
	 */
	public Long getTotal() {
		return total;
	}

	/**
	 * Accessor.
	 * 
	 * @return the lastQuarter
	 */
	public Long getLastQuarter() {
		return lastQuarter;
	}

	/**
	 * Accessor.
	 * 
	 * @return the lastWeek
	 */
	public Long getLastWeek() {
		return lastWeek;
	}

}

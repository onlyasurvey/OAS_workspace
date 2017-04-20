package com.oas.model.enterprise;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.util.Assert;

/**
 * Enterprise stats-at-a-glance (comprised of multiple {@link ResponseStatSet}
 * s), backed by the oas.enterprise_quick_stats view.
 * 
 * @author xhalliday
 * @since 2009-10-13
 */
@Entity
@Table(schema = "oas", name = "enterprise_quick_stats")
public class QuickStats {

	/** Date as of which these numbers are current. */
	@Id
	@Column(name = "as_of")
	private Date asOf;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "total", column = @Column(name = "total")),
			@AttributeOverride(name = "lastWeek", column = @Column(name = "total_last_week")),
			@AttributeOverride(name = "thisMonth", column = @Column(name = "total_this_month")),
			@AttributeOverride(name = "lastQuarter", column = @Column(name = "total_last_quarter")),
			@AttributeOverride(name = "today", column = @Column(name = "total_today")) })
	private ResponseStatSet total;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "total", column = @Column(name = "closed_total")),
			@AttributeOverride(name = "lastWeek", column = @Column(name = "closed_last_week")),
			@AttributeOverride(name = "thisMonth", column = @Column(name = "closed_this_month")),
			@AttributeOverride(name = "lastQuarter", column = @Column(name = "closed_last_quarter")),
			@AttributeOverride(name = "today", column = @Column(name = "closed_today")) })
	private ResponseStatSet closed;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "total", column = @Column(name = "deleted_total")),
			@AttributeOverride(name = "lastWeek", column = @Column(name = "deleted_last_week")),
			@AttributeOverride(name = "thisMonth", column = @Column(name = "deleted_this_month")),
			@AttributeOverride(name = "lastQuarter", column = @Column(name = "deleted_last_quarter")),
			@AttributeOverride(name = "today", column = @Column(name = "deleted_today")) })
	private ResponseStatSet deleted;

	// ======================================================================

	public QuickStats() {
	}

	// ======================================================================

	@Override
	public String toString() {
		return "[enterprise-quick-stats[ total=" + total + ", closed=" + closed + ", deleted=" + deleted + "]";
	}

	// ======================================================================

	public ResponseStatSet getPartial() {

		Assert.notNull(total);
		Assert.notNull(closed);
		Assert.notNull(deleted);

		// TODO recalculated for each call
		return total.subtract(closed).subtract(deleted);
	}

	// ======================================================================

	/**
	 * Accessor.
	 * 
	 * @return the asOf
	 */
	public Date getAsOf() {
		return asOf;
	}

	/**
	 * Accessor.
	 * 
	 * @return the total
	 */
	public ResponseStatSet getTotal() {
		return total;
	}

	/**
	 * Accessor.
	 * 
	 * @return the closed
	 */
	public ResponseStatSet getClosed() {
		return closed;
	}

	/**
	 * Accessor.
	 * 
	 * @return the deleted
	 */
	public ResponseStatSet getDeleted() {
		return deleted;
	}

	// ======================================================================

}

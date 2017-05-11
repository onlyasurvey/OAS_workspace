package com.oas.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ca.inforealm.core.model.UserAccount;

/**
 * Global parent type that defines all objects in this system.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "account_owner")
public class AccountOwner extends UserAccount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3290907887813676673L;

	/** First Name. */
	private String firstname;

	/** Last Name. */
	private String lastname;

	/** Organization / Company. */
	private String organization;

	/** Primary contact telephone number. */
	private String telephone;

	/** How did they learn about us? */
	@Column(name = "learned_about")
	private String learnedAbout;

	/** IP address used when the account was created. */
	@Column(name = "ip_on_join")
	private String ipOnJoin;

	/** Government account? */
	@Column(name = "is_government")
	private boolean government;

	/** Send newsletter to this Account Owner? */
	@Column(name = "newsletter_flag")
	private boolean newsletterFlag;

	/** Last time the account logged in. */
	@Column(name = "last_login")
	private Date lastLogin;

	/** User's preferred language. */
	@OneToOne
	@JoinColumn(name = "language_id")
	private SupportedLanguage language;

	// ======================================================================

	public AccountOwner() {
		super();
	}

	public AccountOwner(Long id) {
		super(id);
	}

	// ======================================================================

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname
	 *            the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname
	 *            the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the telephone
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * @param telephone
	 *            the telephone to set
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * @return the learnedAbout
	 */
	public String getLearnedAbout() {
		return learnedAbout;
	}

	/**
	 * @param learnedAbout
	 *            the learnedAbout to set
	 */
	public void setLearnedAbout(String learnedAbout) {
		this.learnedAbout = learnedAbout;
	}

	/**
	 * @return the ipOnJoin
	 */
	public String getIpOnJoin() {
		return ipOnJoin;
	}

	/**
	 * @param ipOnJoin
	 *            the ipOnJoin to set
	 */
	public void setIpOnJoin(String ipOnJoin) {
		this.ipOnJoin = ipOnJoin;
	}

	/**
	 * @return the isGovernmentOfCanada
	 */
	public boolean isGovernment() {
		return government;
	}

	/**
	 * @param isGovernment
	 *            the isGovernmentOfCanada to set
	 */
	public void setGovernment(boolean isGovernment) {
		this.government = isGovernment;
	}

	/**
	 * @return the lastLogin
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin
	 *            the lastLogin to set
	 */
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * @return the language
	 */
	public SupportedLanguage getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

	/**
	 * @return the newsletterFlag
	 */
	public boolean isNewsletterFlag() {
		return newsletterFlag;
	}

	/**
	 * @param newsletterFlag
	 *            the newsletterFlag to set
	 */
	public void setNewsletterFlag(boolean newsletterFlag) {
		this.newsletterFlag = newsletterFlag;
	}

}

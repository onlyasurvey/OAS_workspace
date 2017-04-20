package com.oas.command.model;

/**
 * Command used by people creating accounts.
 * 
 * @author xhalliday
 * @since October 4, 2008
 */
public class SignupCommand {

	/** Login username. */
	private String username;

	/** Password, and Password Confirmation. */
	private String password[] = new String[] { "", "" };

	/** Email, and Email Confirmation. */
	private String email[] = new String[] { "", "" };

	/** First Name. */
	private String firstname;
	/** Last Name. */
	private String lastname;

	/** Organization / Company. */
	private String organization;

	/** Primary contact telephone number. */
	private String telephone;

	/** How did they learn about us? */
	private String learnedAbout;

	/** Government account? */
	private boolean government;

	/** Receive newsletter? */
	private boolean news;

	/** User's preferred language. */
	// private Long languageId;
	// ======================================================================
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String[] getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String[] password) {
		this.password = password;
	}

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
	 * @return the email
	 */
	public String[] getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String[] email) {
		this.email = email;
	}

	/**
	 * @return the government
	 */
	public boolean isGovernment() {
		return government;
	}

	/**
	 * @param government
	 *            the government to set
	 */
	public void setGovernment(boolean government) {
		this.government = government;
	}

	/**
	 * @return the news
	 */
	public boolean isNews() {
		return news;
	}

	/**
	 * @param news
	 *            the news to set
	 */
	public void setNews(boolean news) {
		this.news = news;
	}

	// /**
	// * @return the languageId
	// */
	// public Long getLanguageId() {
	// return languageId;
	// }
	//
	// /**
	// * @param languageId
	// * the languageId to set
	// */
	// public void setLanguageId(Long languageId) {
	// this.languageId = languageId;
	// }

}

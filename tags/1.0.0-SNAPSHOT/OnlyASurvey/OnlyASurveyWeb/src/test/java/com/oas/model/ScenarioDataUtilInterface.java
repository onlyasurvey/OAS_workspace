package com.oas.model;

import java.util.Calendar;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.service.AbstractServiceInterface;

public interface ScenarioDataUtilInterface extends AbstractServiceInterface, DataAccessObject {

	/**
	 * Create a typical scenario, having one (unpublished) survey, with each of
	 * the question types and a few responses.
	 * 
	 * @param owner
	 * @return
	 */
	public Survey createTypicalScenario1(Actor owner);

	/**
	 * Like createTypicalScenario1(Actor) but with a persist flag.
	 * 
	 * @param owner
	 * @param persist
	 * @return
	 */
	public Survey createTypicalScenario1(Actor owner, boolean persist);

	/**
	 * Like createTypicalScenario1(Actor, persist) but with a published flag.
	 * 
	 * @param owner
	 * @param persist
	 * @param published
	 * @return
	 */
	public Survey createTypicalScenario1(Actor owner, boolean persist, boolean published);

	/**
	 * Adds a single, default response. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 * 
	 * @param survey
	 */
	public Response addDefaultResponse(Survey survey);

	/**
	 * Adds the default scenario rules. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 * 
	 * @param survey
	 */
	void addDefaultRules(Survey survey);

	public void addHistoricalResponseData(Survey survey);

	public Response firstResponse(Survey survey);

	/**
	 * Create responseCount responses, including a subset of answer data.
	 * 
	 * @param survey
	 *            The Survey to add data to.
	 * @param responseCount
	 *            How many responses to add.
	 */
	void createResponseData(Survey survey, int responseCount);

	public Survey createMonthlyReportTestSurvey(Actor owner);

	public Survey createMonthlyReportTestSurvey(Actor owner, Calendar cal1, Calendar cal2, Calendar cal3, Calendar cal4);
}
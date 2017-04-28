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
	Survey createTypicalScenario1(Actor owner);

	/**
	 * Like createTypicalScenario1(Actor) but with a persist flag.
	 * 
	 * @param owner
	 * @param persist
	 * @return
	 */
	Survey createTypicalScenario1(Actor owner, boolean persist);

	/**
	 * Like createTypicalScenario1(Actor, persist) but with a published flag.
	 * 
	 * @param owner
	 * @param persist
	 * @param published
	 * @return
	 */
	Survey createTypicalScenario1(Actor owner, boolean persist, boolean published);

	/**
	 * Adds a single, default response. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 * 
	 * @param survey
	 */
	Response addDefaultScenario1Response(Survey survey);

	/**
	 * Same as addDefaultResponse(Survey) but makes adding question history
	 * optional.
	 * 
	 * @param survey
	 * @return
	 */
	Response addDefaultScenario1Response(Survey survey, boolean addQuestionHistory);

	/**
	 * Adds the default scenario rules. Called by createTypicalScenario1 if
	 * persist=true, otherwise it's up to the developer to call this manually if
	 * needed.
	 * 
	 * @param survey
	 */
	void addDefaultScenario1Rules(Survey survey);

	/**
	 * Create a very simple scenario. Consists of a one-TextQuestion Survey and
	 * no responses, in a Published state.
	 * 
	 * @param owner
	 *            AccountOwner
	 * @return Survey
	 */
	Survey createScenario2(Actor owner);

	/**
	 * Add historical response data to a survey, being responses 1 and 2 days
	 * ago, and 1 and 2 months ago.
	 */
	void addHistoricalResponseData(Survey survey);

	/**
	 * Generic response data creating method. Does not depend on a particular
	 * scenario, but does assume that a question of each of the following types
	 * exist: Text, Scale, Choice.
	 * 
	 * @param survey
	 *            The Survey to add data to.
	 * @param responseCount
	 *            How many responses to add.
	 */
	void createResponseData(Survey survey, int responseCount);

	Survey createMonthlyReportTestSurvey(Actor owner);

	/**
	 * Create monthly report data.
	 * 
	 * @param owner
	 *            {@link Actor}
	 * @param cal1
	 *            Date for 1st response
	 * @param cal2
	 *            Date for 2nd response
	 * @param cal3
	 *            Date for 3rd response
	 * @param cal4
	 *            Date for 4th response
	 * @return {@link Survey}
	 */
	Survey createMonthlyReportTestSurvey(Actor owner, Calendar cal1, Calendar cal2, Calendar cal3, Calendar cal4);

	/**
	 * Get the first Response for the passed Survey.
	 * 
	 * @param survey
	 *            Survey to inspect
	 * @return Response
	 */
	Response firstResponse(Survey survey);

}

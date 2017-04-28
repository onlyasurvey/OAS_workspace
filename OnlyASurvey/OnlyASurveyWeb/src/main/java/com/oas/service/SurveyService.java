package com.oas.service;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.UploadAttachmentForm;
import com.oas.model.Attachment;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;

/**
 * Primary service for managing Surveys and their related data.
 * 
 * @author xhalliday
 */
public interface SurveyService extends AbstractServiceInterface {

	enum DIRECTION {
		UP, DOWN
	};

	// ======================================================================

	/**
	 * Save a survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void save(Survey survey);

	// ======================================================================

	/**
	 * Mark a Survey as Published, allowing responses to be added to it. Asserts
	 * ownership of the survey and that it is paid for.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	Errors publishSurvey(Survey survey);

	/**
	 * Mark the Survey as Draft, disallowing responses but allowing edits.
	 * Asserts ownership of the survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void unpublishSurvey(Survey survey);

	// ======================================================================

	/**
	 * Applies the given template to the survey. Any existing template already
	 * associated with the Survey will have it's contents overwritten. Any ID in
	 * the specified Template is ignored.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param template
	 */
	void applyTemplate(Survey survey, Template template);

	/**
	 * Clear the specified template from the Survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void clearTemplate(Survey survey, Template template);

	// ======================================================================

	/**
	 * Attach a logo image to a survey.
	 * 
	 * @param errors
	 *            Errors attaching (e.g., wrong format) will be reported here
	 * @param survey
	 *            The Survey to attach to; updates the object and persists it
	 * @param language
	 *            Supported language to attach to
	 * @param position
	 *            SurveyLogo.LogoPositionType The position to assign
	 * @param contentType
	 *            Content-Type of the attachment
	 * @param altText
	 *            alt/title attribute text for this language
	 * @param payload
	 *            Byte image data
	 * @return SurveyLogo
	 * 
	 */
	SurveyLogo attachLogo(Errors errors, Survey survey, SupportedLanguage language, SurveyLogo.PositionType position,
			String contentType, String altText, byte[] payload);

	/**
	 * Return a map of survey logos, keyed on their position.
	 * 
	 * @param survey
	 *            Target Survey
	 * @param language
	 *            Language to load logos for
	 * @param position
	 *            The LEFT/RIGHT position to load
	 * @return
	 */
	byte[] getLogoData(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position);

	/**
	 * Return all logos for the given Survey and language, keyed by position.
	 * 
	 * @param survey
	 * @param language
	 * @return
	 */
	Map<SurveyLogo.PositionType, SurveyLogo> getLogosForLanguage(Survey survey, SupportedLanguage language);

	/**
	 * Find a specific Survey Logo.
	 * 
	 * @param survey
	 * @param language
	 * @param position
	 * @return
	 */
	SurveyLogo getLogo(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position);

	/**
	 * Update an existing Survey Logo's alt text.
	 * 
	 * @param survey
	 * @param language
	 * @param position
	 * @param value
	 */
	void updateLogoAlt(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position, String value);

	/**
	 * Purge a logo by it's position and language.
	 * 
	 * @param survey
	 * @param language
	 * @param position
	 */
	void purgeLogo(Survey survey, SupportedLanguage language, SurveyLogo.PositionType position);

	// ======================================================================

	/**
	 * Update an image attached to a Question. NOTE assumes there's only ever 1
	 * image Attachment - not multi-attachment friendly.
	 * 
	 * @todo Review multiple-attachment strategy - filename attribute?
	 *       type/usage enum?
	 */
	void updateQuestionImage(Errors errors, Question question, UploadAttachmentForm command);

	/**
	 * Retrieve the image attached to a Question. NOTE assumes there's only ever
	 * 1 image Attachment - not multi-attachment friendly.
	 * 
	 * @param question
	 * @return Attachment or null if none
	 */
	Attachment getQuestionImage(Question question);

	/**
	 * Delete a Question Image attachment for a particular language.
	 * 
	 * @param question
	 * @param language
	 * 
	 * @return
	 */
	void deleteQuestionImage(Question question, SupportedLanguage language);

	/**
	 * Set the Tool Tip text for a Question image.
	 * 
	 * @param question
	 * @param language
	 * @param value
	 */
	void setQuestionImageToolTip(Question question, SupportedLanguage language, String value);

	// ======================================================================

	/**
	 * Delete (set is_deleted=true) the Survey. Asserts ownership of the Survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void deleteSurvey(Survey survey);

	/**
	 * Delete (set is_deleted=true) all Responses to a Survey. Asserts ownership
	 * of the Survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void deleteResponseData(Survey survey);

	/**
	 * Purge all Answers to a Question. Asserts ownership of the Survey.
	 * 
	 * @param question
	 */
	void purgeResponseData(Question question);

	/**
	 * Determine if the survey has any response data that is not marked as
	 * deleted.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @return
	 */
	boolean hasNonDeletedResponses(Survey survey);

	// ======================================================================

	/**
	 * Count how many responses have been submitted to a Survey that have not
	 * been deleted (is_deleted=false)
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @return Integer
	 */
	Integer countResponses(Survey survey);

	/**
	 * Count how many <strong>closed</strong> responses have been submitted to a
	 * Survey that have not been deleted (is_deleted=false and is_closed=true)
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @return Integer
	 */
	Integer countClosedResponses(Survey survey);

	// ======================================================================

	/**
	 * Determine whether or not any changes are permitted to the Survey.
	 */
	boolean isChangeAllowed(Survey survey);

	// ======================================================================

	/**
	 * Determine whether or not the Survey is paused.
	 */
	boolean isPaused(Survey survey);

	// ======================================================================

	/**
	 * Use the specified list of language IDs to set the supported languages on
	 * the survey.
	 * 
	 * @param survey
	 *            Survey to affect
	 * @param command
	 *            IDs of languages to now support
	 */
	void setSurveyLanguages(Survey survey, IdListCommand command);

	// ======================================================================

	/**
	 * Add a question to the survey, from the given command object, including
	 * any choices.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param command
	 * @return
	 */
	Question addQuestion(Survey survey, CreateQuestionCommand command);

	/**
	 * Update the Question from the given command object. DOES NOT update
	 * choices.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param command
	 *            Command data
	 * @param question
	 *            The question to update
	 * @return
	 */
	Question updateQuestion(Survey survey, CreateQuestionCommand command, Question question);

	/**
	 * Move a question up by 1 relative to it's siblings.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param question
	 * @param direction
	 */
	void moveQuestionUp(Survey survey, Question question);

	/**
	 * Move a question down by 1 relative to it's siblings.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param question
	 * @param direction
	 */
	void moveQuestionDown(Survey survey, Question question);

	/**
	 * Delete the question. If response data exists then this method throws an
	 * IllegalArgumentException.
	 * 
	 * @param question
	 */
	void deleteQuestion(Question question);

	/**
	 * Clone an existing question, placing it at the end of current list of
	 * questions.
	 * 
	 * @param question
	 *            The question to clone
	 */
	void cloneQuestion(Question question);

	/**
	 * Determine how many questions belong to a survey.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @return
	 */
	Integer countQuestions(Survey survey);

	// ======================================================================

	/**
	 * Add a Choice to a Question.
	 */
	Choice addChoice(ChoiceQuestion question, NameObjectCommand command);

	/**
	 * Add many Choices to a Question.
	 * 
	 * @param question
	 * @param questionCommand
	 */
	void addManyChoices(ChoiceQuestion question, List<ChoiceCommand> choiceList);

	/**
	 * Update an existing Choice in a Question.
	 */
	void updateChoice(ChoiceQuestion question, Choice choice, NameObjectCommand command);

	/**
	 * Move a choice up by 1 relative to it's siblings.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param choice
	 * @param direction
	 */
	void moveChoiceUp(Choice choice);

	/**
	 * Move a choice down by 1 relative to it's siblings.
	 * 
	 * @param survey
	 *            The Survey to affect
	 * @param choice
	 * @param direction
	 */
	void moveChoiceDown(Choice choice);

	/**
	 * Delete a Choice and any related Answer data.
	 * 
	 * @param choice
	 */
	void deleteChoice(Choice choice);

	/**
	 * Clone the given choice onto the tail of hte list of choices for it's
	 * question.
	 * 
	 * @param choice
	 * @return Choice (clone)
	 */
	Choice cloneChoice(Choice choice);

	// ======================================================================

	/**
	 * Mark a Survey as Paid For. Does no validation.
	 * 
	 * @param survey
	 *            The Survey to affect
	 */
	void confirmPayment(Survey survey);

	// ======================================================================

	/**
	 * Find a Running Survey by it's primary key. Filters out deleted Surveys.
	 * 
	 * @param id
	 *            Survey ID to load
	 * @return Survey
	 */
	Survey findNonDeletedSurvey(Long id);

	// ======================================================================

	Question findQuestionById(Long id);

	Question findFirstQuestion(Survey survey);

	Question findLastQuestion(Survey survey);

	Question findQuestionBefore(Question question);

	Question findQuestionAfter(Question question);

}

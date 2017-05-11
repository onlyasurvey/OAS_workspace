package com.oas.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.model.AbstractResourceModel;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.report.ResponsesPerDay;
import com.oas.model.report.ResponsesPerLanguage;
import com.oas.model.report.ResponsesPerMonth;
import com.oas.model.report.calendar.CalendarReport;
import com.oas.model.report.calendar.DailyReport;
import com.oas.model.report.calendar.MonthlyReport;
import com.oas.model.report.calendar.breakdown.ChoiceCalendarBreakdown;
import com.oas.model.report.calendar.breakdown.ScaleCalendarBreakdown;
import com.oas.model.report.calendar.breakdown.TextCalendarBreakdown;
import com.oas.model.report.calendar.value.TextCalendarValue;
import com.oas.service.ReportingService;

/**
 * Service for interacting with surveys.
 * 
 * @author Jason Halliday
 * @since September 6, 2008
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ReportingServiceImpl extends AbstractServiceImpl implements ReportingService {

	private <C extends AbstractResourceModel> List<C> doQuery(final Class<C> clazz, final Survey survey, final Date startDate, final int maxResults) {

		@SuppressWarnings("unchecked")
		List<C> list = (List<C>) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				// TODO revisit using getSimpleName here, it's dirty
				Query query = session.createQuery("from " + clazz.getSimpleName() + " where id.survey = ? and id.date >= ? order by id.date desc")
						.setParameter(0, survey).setParameter(1, startDate).setMaxResults(maxResults);

				return query.list();
			}
		});

		return list;
	}

	@SuppressWarnings("unchecked")
	private <C extends AbstractResourceModel> Map<Long, C> getPerDateMap(Class<C> clazz, int calendarField, int numEntries, Survey survey, Calendar calendar) {
		Map<Long, C> map = new HashMap<Long, C>(numEntries);

		for (int i = 0; i < numEntries; i++) {
			Date thisDate = calendar.getTime();

			// instantiate using the Constructor(Survey,Date,Long) form
			C item = null;
			try {
				item = (C) BeanUtils.instantiateClass(clazz.getConstructor(new Class[] { Survey.class, Date.class, Long.class }), new Object[] { survey,
						thisDate, 0L });
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

			Assert.notNull(item, "unable to populate object");

			// 
			map.put(thisDate.getTime(), item);
			calendar.add(calendarField, 1);
		}

		return map;
	}

	/**
	 * TODO revisit this method, it's way too big.
	 */
	@Override
	@ValidUser
	public Collection<ResponsesPerDay> getResponseRatePerDay(final Survey survey, final int numDays) {

		Assert.notNull(survey);
		Assert.isTrue(numDays > 0);

		List<ResponsesPerDay> retval = new ArrayList<ResponsesPerDay>(numDays);

		// get calendar started numDays ago
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH, -numDays + 1);

		final Date startDate = new Date(calendar.getTimeInMillis());

		// execute the query
		List<ResponsesPerDay> list = doQuery(ResponsesPerDay.class, survey, startDate, numDays);

		// fill in all dates, so everything is ordered nicely for the caller
		Map<Long, ResponsesPerDay> map = getPerDateMap(ResponsesPerDay.class, Calendar.DAY_OF_MONTH, numDays, survey, calendar);

		int i = 0;
		for (ResponsesPerDay item : list) {
			// get the pre-initialized record
			ResponsesPerDay existingItem = map.get(item.getId().getDate().getTime());
			Assert.notNull(existingItem, "no matching date record: " + i);

			// update the record
			existingItem.setCount(item.getCount());

			i++;
		}

		int numAdded = 0;
		for (ResponsesPerDay rpd : map.values()) {
			retval.add(rpd);
			numAdded++;
		}

		// sort
		Collections.sort(retval);

		Assert.isTrue(numAdded == retval.size(), numAdded + " != " + retval.size());

		return retval;
	}

	@Override
	@ValidUser
	public Collection<ResponsesPerMonth> getResponseRatePerMonth(final Survey survey, final int numMonths) {

		Assert.notNull(survey);
		Assert.isTrue(numMonths > 0);

		// get calendar started numDays ago
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MONTH, -numMonths + 1);

		final Date startDate = new Date(calendar.getTimeInMillis());

		List<ResponsesPerMonth> retval = new ArrayList<ResponsesPerMonth>(numMonths);

		// execute the query
		List<ResponsesPerMonth> list = doQuery(ResponsesPerMonth.class, survey, startDate, numMonths);

		// fill in all dates, so everything is ordered nicely for the caller
		Map<Long, ResponsesPerMonth> map = getPerDateMap(ResponsesPerMonth.class, Calendar.MONTH, numMonths, survey, calendar);

		int i = 0;
		for (ResponsesPerMonth item : list) {
			// get the pre-initialized record
			Date itemDate = item.getId().getDate();
			ResponsesPerMonth existingItem = map.get(itemDate.getTime());
			Assert.notNull(existingItem, "no matching date record: " + i + " for date " + itemDate.toString());

			// update the record
			existingItem.setCount(item.getCount());

			i++;
		}

		int numAdded = 0;
		for (ResponsesPerMonth rpd : map.values()) {
			retval.add(rpd);
			numAdded++;
		}

		// sort
		Collections.sort(retval);

		Assert.isTrue(numAdded == retval.size(), numAdded + " != " + retval.size());

		return retval;
	}

	@Override
	@ValidUser
	public Map<SupportedLanguage, Integer> getResponsesPerLanguage(Survey survey) {
		Map<SupportedLanguage, Integer> retval = new HashMap<SupportedLanguage, Integer>();
		Map<SupportedLanguage, Long> perLanguageCount = new HashMap<SupportedLanguage, Long>();
		Collection<ResponsesPerLanguage> list = find("from ResponsesPerLanguage where id.survey = ?", survey);

		long totalCount = 0;
		for (ResponsesPerLanguage rpl : list) {
			totalCount += rpl.getCount();
			perLanguageCount.put(rpl.getId().getSupportedLanguage(), rpl.getCount());
		}

		for (ResponsesPerLanguage rpl : list) {
			SupportedLanguage supportedLanguage = rpl.getId().getSupportedLanguage();
			Long langCount = perLanguageCount.get(rpl.getId().getSupportedLanguage());

			Float percentage = new Float(((float) langCount / (float) totalCount) * 100);
			retval.put(supportedLanguage, percentage.intValue());
		}

		return retval;
	}

	// ======================================================================
	// Daily Report
	// ======================================================================

	@Override
	@ValidUser
	public DailyReport getDailyReport(Survey survey) {

		DailyReport report = new DailyReport(survey);

		// load report data
		Collection<ChoiceCalendarBreakdown> choiceList = find("from ChoiceDailyBreakdown where survey = ?", report.getSurvey());
		Collection<TextCalendarBreakdown> textBreakdown = find("from TextDailyBreakdown where survey = ?", report.getSurvey());
		Collection<ScaleCalendarBreakdown> scaleBreakdown = find("from ScaleDailyBreakdown where survey = ?", report.getSurvey());

		// add as appropriate
		addChoiceBreakdownToCalendarReport(report, choiceList);
		addTextBreakdownToCalendarReport(report, textBreakdown);
		addScaleBreakdownToCalendarReport(report, scaleBreakdown);

		// set zeros for any cell with no data
		zeroMissingData(report, survey);

		return report;
	}

	// ======================================================================
	// Monthly Report
	// ======================================================================

	@Override
	@ValidUser
	public MonthlyReport getMonthlyReport(Survey survey) {

		MonthlyReport report = new MonthlyReport(survey);

		// load report data
		Collection<ChoiceCalendarBreakdown> choiceList = find("from ChoiceMonthlyBreakdown where survey = ?", report.getSurvey());
		Collection<TextCalendarBreakdown> textBreakdown = find("from TextMonthlyBreakdown where survey = ?", report.getSurvey());
		Collection<ScaleCalendarBreakdown> scaleBreakdown = find("from ScaleMonthlyBreakdown where survey = ?", report.getSurvey());

		// add as appropriate
		addChoiceBreakdownToCalendarReport(report, choiceList);
		addTextBreakdownToCalendarReport(report, textBreakdown);
		addScaleBreakdownToCalendarReport(report, scaleBreakdown);

		// set zeros for any cell with no data
		zeroMissingData(report, survey);

		return report;
	}

	/**
	 * Add text breakdown to a CalendarReport.
	 * 
	 * @param report
	 * @param question
	 */
	private void addTextBreakdownToCalendarReport(CalendarReport report, Collection<TextCalendarBreakdown> textBreakdown) {

		// all questions can have text answers - the "other" value
		for (TextCalendarBreakdown breakdown : textBreakdown) {

			// the month in question
			Date date = breakdown.getId().getDate();

			// ensure month is recorded
			report.addDate(date);

			// get the map for this question, initializing if it's null
			Map<Date, Long> map = report.getTextAnswerCounts().get(breakdown.getId().getQuestion());
			if (map == null) {
				map = new HashMap<Date, Long>();
				report.getTextAnswerCounts().put(breakdown.getId().getQuestion(), map);
			}

			map.put(date, breakdown.getResponseCount());
		}
	}

	/**
	 * Add choice breakdown to a CalendarReport.
	 * 
	 * @param report
	 * @param question
	 */
	private void addChoiceBreakdownToCalendarReport(CalendarReport report, Collection<ChoiceCalendarBreakdown> list) {

		// ChoiceQuestion source = (ChoiceQuestion) question;
		for (ChoiceCalendarBreakdown breakdown : list) {

			//
			Choice choice = breakdown.getId().getChoice();

			// date from the breakdown will already be truncated to appropriate
			// precision
			Date date = breakdown.getId().getDate();

			// ensure date is recorded
			report.addDate(date);

			//
			Map<Date, ChoiceCalendarBreakdown> map = report.getChoiceAnswers().get(choice);
			if (map == null) {
				// initialize new data set
				map = new HashMap<Date, ChoiceCalendarBreakdown>();

				// stuff into report
				report.getChoiceAnswers().put(choice, map);
			}
			Assert.isNull(map.get(date), "sanity check");
			map.put(date, breakdown);
		}
	}

	/**
	 * Add scale breakdown to a CalendarReport.
	 * 
	 * @param report
	 * @param textBreakdown
	 */
	private void addScaleBreakdownToCalendarReport(CalendarReport report, Collection<ScaleCalendarBreakdown> scaleBreakdown) {

		Map<Question, Map<Date, Map<Long, Long>>> byQuestion = report.getScaleAnswerCounts();
		Assert.notNull(byQuestion);

		for (ScaleCalendarBreakdown breakdown : scaleBreakdown) {
			Question question = breakdown.getId().getQuestion();
			Date date = breakdown.getId().getDate();
			Long answerValue = breakdown.getId().getAnswerValue();
			Long count = breakdown.getCount();

			Assert.notNull(question);
			Assert.notNull(date);
			Assert.notNull(answerValue);
			Assert.notNull(count);

			report.addDate(date);

			// String zDebug = question.getId() + " on " + date + ": " +
			// answerValue + " = " + count;

			Map<Date, Map<Long, Long>> byDate = byQuestion.get(question);
			if (byDate == null) {
				byDate = new HashMap<Date, Map<Long, Long>>();
				byQuestion.put(question, byDate);
			}

			Map<Long, Long> byAnswer = byDate.get(date);
			if (byAnswer == null) {
				byAnswer = new HashMap<Long, Long>();
				byDate.put(date, byAnswer);
			}

			Assert.isNull(byAnswer.get(answerValue), "sanity check: failed by date and answer");
			byAnswer.put(answerValue, count);
		}

	}

	@Override
	@ValidUser
	public Map<Question, Collection<TextAnswer>> getTextAnswers(Survey survey) {
		Map<Question, Collection<TextAnswer>> retval = new HashMap<Question, Collection<TextAnswer>>();

		Collection<TextAnswer> valueList = find("from TextAnswer where response.survey = ? AND response.deleted = false order by response.created", survey);

		for (TextAnswer value : valueList) {
			Collection<TextAnswer> collection = retval.get(value.getQuestion());
			if (collection == null) {
				// first item for this question
				collection = new ArrayList<TextAnswer>();
				retval.put(value.getQuestion(), collection);
			}

			// add in the value
			collection.add(value);
		}

		return retval;
	}

	/**
	 * Put zeros where any data is missing (eg., no responses in a month or for
	 * a choice).
	 * 
	 * @param report
	 * @param survey
	 */
	private void zeroMissingData(CalendarReport report, Survey survey) {
		for (Question question : survey.getQuestions()) {

			if (question.isTextQuestion() || question.isAllowOtherText()) {
				//
				Map<Date, Long> map = report.getTextAnswerCounts().get(question);
				if (map == null) {
					map = new HashMap<Date, Long>();
					report.getTextAnswerCounts().put(question, map);
				}

				//
				for (Date date : report.getDateList()) {

					Long count = map.get(date);
					if (count == null) {
						count = 0L;

						//
						map.put(date, count);
					}
				}

				// next question: this MUST NOT happen unless question really is
				// only a text quesiton, since isOtherText applies to non-text
				// questions!
				if (question.isTextQuestion()) {
					continue;
				}
			}

			if (question.isChoiceQuestion()) {
				//
				ChoiceQuestion cq = (ChoiceQuestion) question;

				for (Choice choice : cq.getChoices()) {

					//
					Map<Date, ChoiceCalendarBreakdown> map = report.getChoiceAnswers().get(choice);
					if (map == null) {
						map = new HashMap<Date, ChoiceCalendarBreakdown>();
						report.getChoiceAnswers().put(choice, map);
					}

					//
					for (Date date : report.getDateList()) {

						ChoiceCalendarBreakdown breakdown = map.get(date);
						if (breakdown == null) {
							breakdown = report.newChoiceCalendarBreakdown(survey, question, choice, date, 0L);

							//
							map.put(date, breakdown);
						}
					}
				}

				// next question
				continue;
			}

			if (question.isScaleQuestion()) {
				//
				ScaleQuestion cq = (ScaleQuestion) question;

				//
				Map<Date, Map<Long, Long>> map = report.getScaleAnswerCounts().get(cq);
				if (map == null) {
					map = new HashMap<Date, Map<Long, Long>>();
					report.getScaleAnswerCounts().put(cq, map);
				}

				for (Long answerValue : cq.getPossibleValues()) {

					//
					for (Date date : report.getDateList()) {

						Map<Long, Long> breakdown = map.get(date);
						if (breakdown == null) {
							breakdown = new HashMap<Long, Long>();

							//
							map.put(date, breakdown);
						}

						//
						if (breakdown.containsKey(answerValue)) {
							// already has a value
						} else {
							breakdown.put(answerValue, 0L);
						}
					}
				}

				// next question
				continue;
			}

			throw new IllegalArgumentException("unknown question type for #" + question.getId());

		}
	}

	// ======================================================================
	// Text Responses
	// ======================================================================

	@Override
	@ValidUser
	public Collection<TextCalendarValue> getTextResponsesPerDay(Question question, Date date) {

		Object[] args = new Object[] { question, DateUtils.truncate(date, Calendar.DAY_OF_MONTH) };
		Collection<TextCalendarValue> retval = find("from TextDailyValue where id.question = ? and id.month = ? order by id.response.created", args);
		return retval;
	}

	@Override
	@ValidUser
	public Collection<TextCalendarValue> getTextResponsesPerMonth(Question question, Date date) {

		Object[] args = new Object[] { question, DateUtils.truncate(date, Calendar.MONTH) };
		Collection<TextCalendarValue> retval = find("from TextMonthlyValue where id.question = ? and id.month = ? order by id.response.created", args);
		return retval;
	}
}

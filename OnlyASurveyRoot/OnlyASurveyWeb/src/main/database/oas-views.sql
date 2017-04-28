-- ---------------------------- PER LANGUAGE ------------------------------

CREATE OR REPLACE VIEW oas.vw_responses_per_language AS
SELECT
	s.id AS survey_id,
	sl.id AS language_id,
	COUNT( sl ) AS response_count
FROM oas.response r
JOIN oas.base_object base_r ON ( base_r.id = r.id )
JOIN oas.survey s ON ( r.survey_id = s.id )
JOIN oas.supported_language sl ON ( sl.id = r.language_id )
WHERE
	 base_r.is_deleted = FALSE
	 AND
	 r.is_closed = TRUE
GROUP BY
	s.id,
	sl.id
;




-- ---------------------------- SUMMARY ------------------------------

CREATE OR REPLACE VIEW oas.vw_survey_summaries AS
	SELECT
			s.id			AS survey_id,
			COUNT(base_r)	AS response_count
	FROM	oas.survey s
	JOIN oas.base_object base_s ON ( s.id = base_s.id AND base_s.is_deleted = FALSE)
	LEFT OUTER JOIN oas.response r ON ( r.survey_id = s.id AND r.is_closed = TRUE )
	LEFT OUTER JOIN oas.base_object base_r ON ( base_r.id = r.id AND base_r.is_deleted = FALSE )

	GROUP BY
		s.id
	;


-- ---------------------------- DAILY ------------------------------

CREATE OR REPLACE VIEW oas.vw_responses_per_day AS

SELECT
	DISTINCT date_created AS target_date,
	survey_id,
	SUM(response_count) AS response_count
FROM (
	SELECT
				r.survey_id,
			 	base_r.date_created::DATE,
				COUNT(base_r) AS response_count
	FROM	oas.response r
	JOIN	oas.base_object base_r ON ( base_r.id = r.id )
	WHERE
		base_r.is_deleted = FALSE
		AND
		r.is_closed = TRUE
	GROUP BY
		date_created, survey_id
	
	ORDER BY
		base_r.date_created ASC
) AS r
GROUP BY
	date_created, survey_id
	;


CREATE OR REPLACE VIEW oas.vw_choice_daily_breakdown AS
	SELECT
		DISTINCT r.target_date,
		r.survey_id,
		r.question_id,
		r.choice_id,
		SUM(r.response_count) AS response_count
	
	FROM
	(
		SELECT
			DATE_TRUNC('day', base_r.date_created::date )::date AS target_date,
			r.survey_id,
			a.question_id,
			c.id AS choice_id,
			COUNT(  ca.id ) AS response_count
		
		FROM oas.choice_answer ca
		JOIN oas.answer a ON ( a.id = ca.id )
		JOIN oas.choice c ON ( c.id = ca.answer_value )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		WHERE
			r.is_closed = TRUE

		GROUP BY
			target_date, r.survey_id, a.question_id, c.id
	) AS r
	GROUP BY
		
		target_date
		, survey_id
		, question_id
		, choice_id
	ORDER BY
		target_date, choice_id
;

CREATE OR REPLACE VIEW oas.vw_scale_daily_breakdown AS
	SELECT
		DISTINCT r.target_date,
		r.survey_id,
		r.question_id,
		r.answer_value,
		SUM(r.response_count) AS response_count
	
	FROM
	(
		SELECT
			DATE_TRUNC('day', base_r.date_created::date )::date AS target_date,
			r.survey_id,
			a.question_id,
			sa.answer_value,
			COUNT(  sa.id ) AS response_count
		
		FROM oas.scale_answer sa
		JOIN oas.answer a ON ( a.id = sa.id )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		WHERE
			r.is_closed = TRUE
		GROUP BY
			target_date, r.survey_id, a.question_id, sa.answer_value
	) AS r
	GROUP BY		
		target_date
		, survey_id
		, question_id
		, answer_value
	ORDER BY
		target_date, answer_value
;

CREATE OR REPLACE VIEW oas.vw_text_daily_breakdown AS
	SELECT
		DISTINCT a.target_date,
		survey_id,
		question_id,
		SUM(response_count) AS response_count
	FROM
	(
		SELECT
			DATE_TRUNC('day', base_r.date_created::date )::date AS target_date,
			s.id AS survey_id,
			q.id AS question_id,
			COUNT(ta.id) AS response_count
		FROM oas.text_answer ta
		JOIN oas.answer a ON ( a.id = ta.id )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		JOIN oas.question q ON ( q.id = a.question_id )
		JOIN oas.survey s ON ( s.id = q.survey_id )
		JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
		WHERE
			r.is_closed = TRUE
		GROUP BY
			base_r.date_created,
			s.id,
			q.id
	) a
	GROUP BY
		target_date
		, survey_id
		, question_id
;

CREATE OR REPLACE VIEW oas.vw_text_daily_values AS
	SELECT
		DATE_TRUNC('day', base_r.date_created::date )::date AS target_date,
		s.id AS survey_id,
		q.id AS question_id,
		r.id AS response_id,
		ta.answer_value
	FROM oas.text_answer ta
	JOIN oas.answer a ON ( a.id = ta.id )
	JOIN oas.response r ON ( r.id = a.response_id )
	JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
	JOIN oas.question q ON ( q.id = a.question_id )
	JOIN oas.survey s ON ( s.id = q.survey_id )
	JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
	WHERE
		r.is_closed = TRUE
;


-- ---------------------------- MONTHLY ------------------------------

	

CREATE OR REPLACE VIEW oas.vw_responses_per_month AS
SELECT
	DISTINCT target_date
,
	 survey_id
, 
 	SUM(response_count) AS response_count

FROM 
(
	SELECT
		r.survey_id,
		DATE_TRUNC('month', r.target_date)::date  AS target_date,
		SUM(r.response_count) AS response_count
	FROM oas.vw_responses_per_day r
--	WHERE
--		r.is_closed = TRUE
	GROUP BY
		target_date, survey_id
	ORDER BY
		survey_id, target_date
	) AS r
GROUP BY target_date, survey_id
;

CREATE OR REPLACE VIEW oas.vw_choice_monthly_breakdown AS
	SELECT
		DISTINCT r.target_date,
		r.survey_id,
		r.question_id,
		r.choice_id,
		SUM(r.response_count) AS response_count
	
	FROM
	(
		SELECT
			DATE_TRUNC('month', base_r.date_created::date )::date AS target_date,
			r.survey_id,
			a.question_id,
			c.id AS choice_id,
			COUNT(  ca.id ) AS response_count
		
		FROM oas.choice_answer ca
		JOIN oas.answer a ON ( a.id = ca.id )
		JOIN oas.choice c ON ( c.id = ca.answer_value )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		WHERE
			r.is_closed = TRUE
		GROUP BY
			target_date, r.survey_id, a.question_id, c.id
	) AS r
	GROUP BY
		
		target_date
		, survey_id
		, question_id
		, choice_id
	ORDER BY
		target_date, choice_id
;


CREATE OR REPLACE VIEW oas.vw_scale_monthly_breakdown AS
	SELECT
		DISTINCT r.target_date,
		r.survey_id,
		r.question_id,
		r.answer_value,
		SUM(r.response_count) AS response_count
	
	FROM
	(
		SELECT
			DATE_TRUNC('month', base_r.date_created::date )::date AS target_date,
			r.survey_id,
			a.question_id,
			sa.answer_value,
			COUNT(  sa.id ) AS response_count
		
		FROM oas.scale_answer sa
		JOIN oas.answer a ON ( a.id = sa.id )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		WHERE
			r.is_closed = TRUE
		GROUP BY
			target_date, r.survey_id, a.question_id, sa.answer_value
	) AS r
	GROUP BY
		
		target_date
		, survey_id
		, question_id
		, answer_value
	ORDER BY
		target_date, answer_value
;


CREATE OR REPLACE VIEW oas.vw_text_monthly_breakdown AS
	SELECT
		DISTINCT a.target_date,
		survey_id,
		question_id,
		SUM(response_count) AS response_count
	FROM
	(
		SELECT
			DATE_TRUNC('month', base_r.date_created::date )::date AS target_date,
			s.id AS survey_id,
			q.id AS question_id,
			COUNT(ta.id) AS response_count
		FROM oas.text_answer ta
		JOIN oas.answer a ON ( a.id = ta.id )
		JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
		JOIN oas.response r ON ( r.id = base_r.id )
		JOIN oas.question q ON ( q.id = a.question_id )
		JOIN oas.survey s ON ( s.id = q.survey_id )
		JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
		WHERE
			r.is_closed = TRUE
		GROUP BY
			base_r.date_created,
			s.id,
			q.id
	) a
	GROUP BY
		target_date
		, survey_id
		, question_id
;

CREATE OR REPLACE VIEW oas.vw_text_monthly_values AS
	SELECT
		DATE_TRUNC('month', base_r.date_created::date )::date AS target_date,
		s.id AS survey_id,
		q.id AS question_id,
		--r.id AS response_id,
		base_r.id AS response_id,
		ta.answer_value
	FROM oas.text_answer ta
	JOIN oas.answer a ON ( a.id = ta.id )
	JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
	JOIN oas.response r ON ( r.id = base_r.id )
	JOIN oas.question q ON ( q.id = a.question_id )
	JOIN oas.survey s ON ( s.id = q.survey_id )
	JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
	WHERE
		r.is_closed = TRUE
;



-- ----------------------------------------------------------------------
-- ---------------------------- RAW EXPORT ------------------------------
-- ----------------------------------------------------------------------

CREATE OR REPLACE VIEW oas.vw_raw_text AS
	SELECT
		s.id AS survey_id,
		q.id AS question_id,
		--r.id AS response_id,
		base_r.id AS response_id,
		base_r.date_created,
		r.date_closed,
		ta.answer_value
	FROM oas.text_answer ta
	JOIN oas.answer a ON ( a.id = ta.id )
	JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
	JOIN oas.response r ON ( r.id = base_r.id )
	JOIN oas.question q ON ( q.id = a.question_id )
	JOIN oas.survey s ON ( s.id = q.survey_id )
	JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
	WHERE
		r.is_closed = TRUE

;



CREATE OR REPLACE VIEW oas.vw_raw_scale AS
	SELECT
		r.survey_id,
		a.question_id,
		r.id AS response_id,
		base_r.date_created,
		r.date_closed,
		sa.answer_value
	
	FROM oas.scale_answer sa
	JOIN oas.answer a ON ( a.id = sa.id )
	JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
	JOIN oas.response r ON ( r.id = base_r.id )
	WHERE
		r.is_closed = TRUE

;

--DROP VIEW oas.vw_raw_choice;
CREATE OR REPLACE VIEW oas.vw_raw_choice AS
	SELECT
		r.survey_id,
		a.question_id,
		r.id AS response_id,
		base_r.date_created,
		r.date_closed,
		c.id AS choice_id,
		ca.sum_value
	
	FROM oas.choice_answer ca
	JOIN oas.answer a ON ( a.id = ca.id )
	JOIN oas.choice c ON ( c.id = ca.answer_value )
	JOIN oas.base_object base_r ON ( base_r.id = a.response_id AND base_r.is_deleted = FALSE )
	JOIN oas.response r ON ( r.id = base_r.id )
	WHERE
		r.is_closed = TRUE
;

-- ---------------------------- ENTERPRISE QUICK STATS ------------------------------

CREATE OR REPLACE VIEW oas.enterprise_quick_stats AS
       SELECT
       			DATE_TRUNC('second', NOW()) AS as_of,
                COUNT( d2 ) AS total_today,
                COUNT( lw2 ) AS total_last_week,
                COUNT( m2 ) AS total_this_month,
                COUNT( lq2 ) AS total_last_quarter,
                COUNT( t2 ) AS total,

                COUNT( d ) AS closed_today,
                COUNT( lw ) AS closed_last_week,
                COUNT( m ) AS closed_this_month,
				COUNT( lq ) AS closed_last_quarter,
                COUNT( t ) AS closed_total,
                
                COUNT( d3 ) AS deleted_today,
                COUNT( lw3 ) AS deleted_last_week,
                COUNT( m3 ) AS deleted_this_month,
                COUNT( lq3 ) AS deleted_last_quarter,
                COUNT( t3 ) AS deleted_total


		FROM oas.response r

        LEFT OUTER JOIN oas.base_object d ON ( r.id = d.id AND DATE_TRUNC('day', d.date_created) = DATE_TRUNC('day', NOW()) AND d.is_deleted=FALSE AND r.is_closed=TRUE)
        LEFT OUTER JOIN oas.base_object m ON ( r.id = m.id AND DATE_TRUNC('month', m.date_created) = DATE_TRUNC('month', NOW()) AND m.is_deleted=FALSE AND r.is_closed=TRUE)
        LEFT OUTER JOIN oas.base_object lw ON ( r.id = lw.id AND AGE(lw.date_created) < '7 days' AND lw.is_deleted=FALSE AND r.is_closed=TRUE)
        LEFT OUTER JOIN oas.base_object lq ON ( r.id = lq.id AND AGE(lq.date_created) < '90 days' AND lq.is_deleted=FALSE AND r.is_closed=TRUE)
        LEFT OUTER JOIN oas.base_object t ON ( r.id = t.id AND t.is_deleted=FALSE AND r.is_closed=TRUE)

        LEFT OUTER JOIN oas.base_object d2 ON ( r.id = d2.id AND DATE_TRUNC('day', d2.date_created) = DATE_TRUNC('day', NOW()))
        LEFT OUTER JOIN oas.base_object m2 ON ( r.id = m2.id AND DATE_TRUNC('month', m2.date_created) = DATE_TRUNC('month', NOW()))
        LEFT OUTER JOIN oas.base_object lw2 ON ( r.id = lw2.id AND AGE(lw2.date_created) < '7 days' )
        LEFT OUTER JOIN oas.base_object lq2 ON ( r.id = lq2.id AND AGE(lq2.date_created) < '90 days' )
        LEFT OUTER JOIN oas.base_object t2 ON ( r.id = t2.id )

        LEFT OUTER JOIN oas.base_object d3 ON ( r.id = d3.id AND DATE_TRUNC('day', d3.date_created) = DATE_TRUNC('day', NOW()) AND d3.is_deleted=TRUE)
        LEFT OUTER JOIN oas.base_object m3 ON ( r.id = m3.id AND DATE_TRUNC('month', m3.date_created) = DATE_TRUNC('month', NOW()) AND m3.is_deleted=TRUE)
        LEFT OUTER JOIN oas.base_object lw3 ON ( r.id = lw3.id AND AGE(lw3.date_created) < '7 days' AND lw3.is_deleted=TRUE)
        LEFT OUTER JOIN oas.base_object lq3 ON ( r.id = lq3.id AND AGE(lq3.date_created) < '90 days' AND lq3.is_deleted=TRUE)
        LEFT OUTER JOIN oas.base_object t3 ON ( r.id = t3.id AND t3.is_deleted=TRUE)
;


-- ---------------------------- TIME TAKEN TO COMPLETE RESPONSE ------------------------------

CREATE OR REPLACE VIEW oas.vw_time_taken_to_complete AS

	SELECT
		DISTINCT t.minutes			AS minutes,
		t.survey_id					AS survey_id,
		COUNT(t.minutes)			AS response_count
	FROM
	(
		SELECT
			r.survey_id		AS survey_id,
			EXTRACT('epoch' from DATE_TRUNC('minute', r.date_closed - bo.date_created))/60		AS minutes
			
		FROM oas.response r
		
		JOIN oas.base_object bo ON ( bo.id = r.id AND bo.is_deleted = false )
		
		
		WHERE
			r.is_closed = TRUE
	
		ORDER BY minutes
	) t
	GROUP BY t.minutes, t.survey_id
	ORDER BY
	t.survey_id, t.minutes
;


-- ---------------------------- ABANDONMENT REPORTS ------------------------------


CREATE OR REPLACE VIEW oas.vw_partial_responses AS
	SELECT r.* FROM oas.response r
		JOIN oas.base_object b ON ( b.id = r.id AND b.is_deleted = FALSE )
		WHERE is_closed = FALSE;

CREATE OR REPLACE VIEW oas.vw_partial_response_highest_question_details AS
	SELECT
		p.survey_id							AS survey_id,
		p.id								AS response_id,
		COALESCE(MAX(h.index_order), 0)		AS highest
	
	FROM oas.vw_partial_responses p
		JOIN oas.response_question_history h ON
		(
			h.response_id = p.id
		)
--		LEFT OUTER JOIN oas.answer a ON ( a.response_id = p.id )
--		LEFT OUTER JOIN oas.question q ON ( q.id = a.question_id )
	
	GROUP BY
		p.survey_id,
		p.id
	
	ORDER BY
		survey_id,highest DESC
;

CREATE OR REPLACE VIEW oas.vw_partial_response_highest_question_summary AS
	SELECT
		DISTINCT highest	AS question_index,
		COUNT(1)			AS abandoned_count,
		v.survey_id			AS survey_id
	
	FROM oas.vw_partial_response_highest_question_details v
	GROUP BY
		v.survey_id,
		v.highest
	ORDER BY
		v.survey_id
;

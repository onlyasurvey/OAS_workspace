DROP SCHEMA oas CASCADE;
CREATE SCHEMA oas;

CREATE TABLE oas.base_object
(
	id					BIGSERIAL PRIMARY KEY,
	is_deleted			BOOLEAN NOT NULL DEFAULT FALSE,
	date_created		TIMESTAMP NOT NULL DEFAULT  NOW(),
	date_deleted		TIMESTAMP
);

CREATE INDEX oas_idx_base_object_date_created ON oas.base_object( date_created );
CREATE INDEX oas_idx_base_object_is_deleted ON oas.base_object( is_deleted );

CREATE TABLE oas.supported_language
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	iso3lang			CHAR(3) NOT NULL UNIQUE
);


CREATE TABLE oas.object_name
(
	id					BIGSERIAL PRIMARY KEY,
	-- cascade here, since oas.object_name does not derive from oas.base_object
	object_id			BIGINT NOT NULL REFERENCES oas.base_object(id) ON DELETE CASCADE,
	-- cascade here, since oas.object_name does not derive from oas.base_object
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id) ON DELETE CASCADE,
	text_value			VARCHAR(512) NOT NULL,
	
	UNIQUE ( object_id, language_id )
);


CREATE INDEX oas_idx_object_name_oid ON oas.object_name( object_id );
CREATE INDEX oas_idx_object_name_oid_language ON oas.object_name( object_id, language_id );

-- Resources at the object level, ie., i18n
CREATE TABLE oas.object_resource
(
	id					BIGSERIAL PRIMARY KEY,
	object_id			BIGINT NOT NULL REFERENCES oas.base_object(id) ON DELETE CASCADE,
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id) ON DELETE CASCADE,
	resource_key		VARCHAR(256) NOT NULL,
	text_value			TEXT NOT NULL,
	last_modified		TIMESTAMP NOT NULL DEFAULT NOW(),
	
	UNIQUE ( object_id, language_id, resource_key )
);

CREATE INDEX oas_idx_object_resource_oid ON oas.object_resource( object_id );
CREATE INDEX oas_idx_object_resource_oid_language ON oas.object_resource( object_id, language_id );
CREATE INDEX oas_idx_object_resource_oid_key ON oas.object_resource( object_id, resource_key );
CREATE INDEX oas_idx_object_resource_oid_language_key ON oas.object_resource( object_id, language_id, resource_key );

CREATE TABLE oas.account_owner
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES work.user_account(id) ON DELETE CASCADE,
	firstname			VARCHAR(32) NOT NULL,
	lastname			VARCHAR(32) NOT NULL,
	organization		VARCHAR(64),
	telephone			VARCHAR(24),
	learned_about		VARCHAR(2000),
	
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id),
	
	is_government		BOOLEAN NOT NULL DEFAULT FALSE,
	newsletter_flag		BOOLEAN NOT NULL DEFAULT FALSE,
	
	ip_on_join			CHAR(15) NOT NULL,
	
	last_login			TIMESTAMP NOT NULL
);

CREATE INDEX oas_idx_account_owner_last_login		ON oas.account_owner( last_login );
CREATE INDEX oas_idx_account_owner_language_id		ON oas.account_owner( language_id );
CREATE INDEX oas_idx_account_owner_is_government	ON oas.account_owner( is_government );


CREATE TABLE oas.survey
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	owner_id			BIGINT NOT NULL REFERENCES work.actor(id),
	published			BOOLEAN NOT NULL,
	paid_for			BOOLEAN NOT NULL,
	template_option		VARCHAR(16) NOT NULL
);

CREATE INDEX oas_idx_survey_owner_id ON oas.survey( owner_id );
CREATE INDEX oas_idx_survey_published ON oas.survey( published );

CREATE TABLE oas.survey_language
(
	id					BIGSERIAL PRIMARY KEY,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id) ON DELETE CASCADE,
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id) ON DELETE CASCADE,
	
	UNIQUE( survey_id, language_id )
);
CREATE INDEX oas_idx_survey_language_survey_id ON oas.survey_language( survey_id );


CREATE TABLE oas.template
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id),
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id),
	type_code			VARCHAR(32) NOT NULL,
	base_url			VARCHAR(1024),
	imported_from_url	VARCHAR(1024),
	hash_at_import		CHAR(32),
	before_content		VARCHAR(131072),
	after_content		VARCHAR(131072)
);
CREATE INDEX oas_idx_template_survey_id ON oas.template( survey_id );


CREATE TABLE oas.survey_logo
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id),
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id),
	upload_time			TIMESTAMP NOT NULL,
	content_type		VARCHAR(64) NOT NULL,
	position			VARCHAR(10) NOT NULL,
	width				INT NOT NULL,
	height				INT NOT NULL,
	size				INT NOT NULL,
	alt_text			VARCHAR(255),
	payload				TEXT NOT NULL,
	
	UNIQUE( survey_id, language_id, position )
);
CREATE INDEX oas_idx_survey_logo_survey_id ON oas.survey_logo( survey_id );
CREATE INDEX oas_idx_survey_logo_survey_id_language_id ON oas.survey_logo( survey_id, language_id );


CREATE TABLE oas.response
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id),
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id),
	is_closed			BOOLEAN NOT NULL,
	ip_address			CHAR(15) NOT NULL
);

CREATE INDEX oas_idx_response_survey_id ON oas.response( survey_id );
CREATE INDEX oas_idx_response_is_closed ON oas.response( is_closed );

CREATE TABLE oas.invitation
(
	id					BIGSERIAL PRIMARY KEY,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id) ON UPDATE CASCADE ON DELETE CASCADE,
	response_id			BIGINT REFERENCES oas.response(id) ON UPDATE CASCADE ON DELETE CASCADE,
	status				VARCHAR(16) NOT NULL,
	email_address		VARCHAR(255) NOT NULL,

	reminder_count		INT NOT NULL DEFAULT 0,
	reminder_sent_date	TIMESTAMP,
	
	error_flag			BOOLEAN NOT NULL DEFAULT FALSE,
	error_message		TEXT

);
CREATE INDEX oas_idx_invitation_survey_id_status ON oas.invitation( survey_id, status );
CREATE INDEX oas_idx_invitation_survey_id_reminder_count ON oas.invitation( survey_id, reminder_count );
CREATE INDEX oas_idx_invitation_status ON oas.invitation( status );


CREATE TABLE oas.question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id),
	display_order		INT,
	allow_other_text	BOOLEAN NOT NULL DEFAULT FALSE,
	required			BOOLEAN NOT NULL DEFAULT FALSE,
	style				TEXT
);
CREATE INDEX oas_idx_question_survey_id ON oas.question( survey_id );



CREATE TABLE oas.entry_rule
(
	id					BIGSERIAL PRIMARY KEY,
	
	-- cascade since entry_rule is not a base_object
	question_id			BIGINT NOT NULL REFERENCES oas.question(id) ON DELETE CASCADE,
	
	other_object_id		BIGINT REFERENCES oas.base_object(id) ON DELETE CASCADE,
	
	rule_type			VARCHAR(32) NOT NULL,
	rule_action			VARCHAR(32) NOT NULL,
	
	apply_order			INT
);
CREATE INDEX oas_idx_entry_rule_question_id ON oas.entry_rule( question_id );
CREATE INDEX oas_idx_entry_rule_other_question_id ON oas.entry_rule( other_object_id );

CREATE TABLE oas.exit_rule
(
	id					BIGSERIAL PRIMARY KEY,
	
	-- cascade since entry_rule is not a base_object
	question_id			BIGINT NOT NULL REFERENCES oas.question(id) ON DELETE CASCADE,
	jump_question_id	BIGINT REFERENCES oas.question(id) ON DELETE CASCADE,
	choice_id			BIGINT REFERENCES oas.choice(id) ON DELETE CASCADE,
	
	rule_type			VARCHAR(32) NOT NULL,
	rule_action			VARCHAR(32) NOT NULL,
	
	apply_order			INT
);
CREATE INDEX oas_idx_exit_rule_question_id ON oas.exit_rule( question_id );
CREATE INDEX oas_idx_exit_rule_jump_question_id ON oas.exit_rule( jump_question_id );
CREATE INDEX oas_idx_exit_rule_choice_id ON oas.exit_rule( choice_id );


CREATE TABLE oas.choice
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	question_id			BIGINT NOT NULL REFERENCES oas.question(id),
	display_order		INT
);

CREATE INDEX oas_idx_choice_question_id ON oas.choice( question_id );

CREATE TABLE oas.boolean_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	default_value		BOOLEAN
);

CREATE TABLE oas.choice_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	unlimited			BOOLEAN NOT NULL DEFAULT FALSE,
	maximum_sum			INTEGER
);

CREATE TABLE oas.text_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	max_length			INT NOT NULL,
	num_rows			INT,
	field_display_length	INT,
	default_value		VARCHAR(65536),
	
	CHECK ( num_rows > 0 )
);


CREATE TABLE oas.scale_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	minimum				INT NOT NULL,
	maximum				INT NOT NULL
--	default_value		INT
);

CREATE INDEX oas_idx_scale_question_min_max ON oas.scale_question( minimum, maximum );

CREATE TABLE oas.answer
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	response_id			INT REFERENCES oas.response(id),
	question_id			INT REFERENCES oas.question(id)
);

CREATE INDEX oas_idx_answer_question_id ON oas.answer( question_id );
CREATE INDEX oas_idx_answer_response_id ON oas.answer( response_id );
CREATE INDEX oas_idx_answer_question_id_response_id ON oas.answer( question_id, response_id );

CREATE TABLE oas.boolean_answer
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.answer(id) ON DELETE CASCADE,
	-- not nullable - when "none" there are no records
	answer_value		BOOLEAN NOT NULL
);

CREATE TABLE oas.choice_answer
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.answer(id) ON DELETE CASCADE,
	-- not nullable - when "none" there are no choice_answer records
	answer_value		BIGINT NOT NULL REFERENCES oas.choice(id),
	-- a value used in a sum question
	sum_value			INTEGER
);

CREATE INDEX oas_idx_choice_answer_answer_value ON oas.choice_answer( answer_value );

CREATE TABLE oas.text_answer
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.answer(id) ON DELETE CASCADE,
	-- not nullable - when "none" there are no choice_answer records
	answer_value		VARCHAR(65536)  NOT NULL
);

CREATE TABLE oas.scale_answer
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.answer(id) ON DELETE CASCADE,
	-- not nullable - when "none" there are no choice_answer records
	answer_value		INT NOT NULL
);



CREATE TABLE oas.contact_us_message
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	-- nullable, must be manually removed if account owner is to be deleted
	owner_id			BIGINT REFERENCES work.actor(id),
	email				VARCHAR(255),
	message				VARCHAR(16384) NOT NULL
);


INSERT INTO work.application( identifier, name_en, name_fr ) VALUES ( 'OAS', 'OAS', 'OAS' );
INSERT INTO work.role_definition( application_id, identifier ) VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'ROLE_USER');
INSERT INTO work.role_definition( application_id, identifier ) VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'ROLE_ENTERPRISE_ADMIN');


INSERT INTO oas.base_object( id ) VALUES ( -1000 );
INSERT INTO oas.supported_language(id, iso3lang) VALUES ( -1000, 'eng');
INSERT INTO oas.base_object( id ) VALUES ( -1001 );
INSERT INTO oas.supported_language(id,iso3lang) VALUES ( -1001, 'fra');

INSERT INTO oas.object_name(object_id, language_id, text_value) VALUES ( -1000, (SELECT id FROM oas.supported_language WHERE iso3lang = 'eng'), 'English');
INSERT INTO oas.object_name(object_id, language_id, text_value) VALUES ( -1000, (SELECT id FROM oas.supported_language WHERE iso3lang = 'fra'), 'Anglais');

INSERT INTO oas.object_name(object_id, language_id, text_value) VALUES ( -1001, (SELECT id FROM oas.supported_language WHERE iso3lang = 'eng'), 'French');
INSERT INTO oas.object_name(object_id, language_id, text_value) VALUES ( -1001, (SELECT id FROM oas.supported_language WHERE iso3lang = 'fra'), 'Français');


--INSERT INTO work.configuration_item (application_id, identifier, item_value)
--	VALUES ((SELECT id FROM work.application WHERE identifier='OAS'), 'reportUrlPrefix', 'http://localhost:8080/ftl');

create table work.persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null) ;
create index persistent_logins_username_idx ON work.persistent_logins ( username );


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
	LEFT OUTER JOIN oas.response r ON ( r.survey_id = s.id )
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
	--JOIN oas.response r ON ( r.id = base_r.id )
	JOIN oas.question q ON ( q.id = a.question_id )
	JOIN oas.survey s ON ( s.id = q.survey_id )
	JOIN oas.base_object base_ta ON ( base_ta.id = ta.id )
;

INSERT INTO work.configuration_item( application_id, identifier, item_value )
	VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'publicContentFilesystemPrefix', '/home/oaspubliccontent/public/' );

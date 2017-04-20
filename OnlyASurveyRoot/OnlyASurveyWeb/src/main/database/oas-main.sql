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


-- Attachments
CREATE TABLE oas.object_attachment
(
	id					BIGSERIAL PRIMARY KEY,
	object_id			BIGINT NOT NULL REFERENCES oas.base_object(id) ON DELETE CASCADE,
	type_code			VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
	content_type		VARCHAR(64) NOT NULL
);
CREATE INDEX oas_idx_object_attachment_object_id ON oas.object_attachment( object_id );
CREATE INDEX oas_idx_object_attachment_object_id_type_code ON oas.object_attachment( object_id, type_code );

-- Attachment Payloads
CREATE TABLE oas.object_attachment_payload
(
	id					BIGINT NOT NULL REFERENCES oas.object_attachment(id) ON DELETE CASCADE,
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id) ON DELETE CASCADE,
	upload_time			TIMESTAMP NOT NULL,
	size				INT4 NOT NULL,
	alt_text			TEXT,
	payload				TEXT NOT NULL,
	
	UNIQUE( id, language_id )
);

--CREATE INDEX oas_idx_object_attachment_payload_id_language_id ON oas.object_attachment_payload( id, language_id );

CREATE TABLE oas.account_owner
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES work.user_account(id) ON DELETE CASCADE,
	firstname			VARCHAR(32) NOT NULL,
	lastname			VARCHAR(32) NOT NULL,
	organization		VARCHAR(64),
	telephone			VARCHAR(24),
	learned_about		VARCHAR(2000),
	
	-- Bill Type (demo, pay_as_you_go, monthly)
	bill_type			VARCHAR(32) NOT NULL,
	
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language(id),
	
	is_government		BOOLEAN NOT NULL DEFAULT FALSE,
	newsletter_flag		BOOLEAN NOT NULL DEFAULT FALSE,
	
	ip_on_join			CHAR(15) NOT NULL,
	
	join_date			TIMESTAMP NOT NULL
);

--CREATE INDEX oas_idx_account_owner_last_login		ON oas.account_owner( last_login );
CREATE INDEX oas_idx_account_owner_language_id		ON oas.account_owner( language_id );
CREATE INDEX oas_idx_account_owner_is_government	ON oas.account_owner( is_government );


CREATE TABLE oas.survey
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	owner_id			BIGINT NOT NULL REFERENCES work.actor(id),
	published			BOOLEAN NOT NULL,
	paid_for			BOOLEAN NOT NULL,
	template_option		VARCHAR(16) NOT NULL,
	optin_percentage	SMALLINT DEFAULT 25,
	global_password		VARCHAR(32)
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
	date_closed			TIMESTAMP,
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
	invitation_code		VARCHAR(32),
	
	
	reminder_count		INT NOT NULL DEFAULT 0,
	reminder_sent_date	TIMESTAMP,
	
	error_flag			BOOLEAN NOT NULL DEFAULT FALSE,
	error_message		TEXT

);
CREATE INDEX oas_idx_invitation_survey_id_status ON oas.invitation( survey_id, status );
CREATE INDEX oas_idx_invitation_survey_id_reminder_count ON oas.invitation( survey_id, reminder_count );
CREATE INDEX oas_idx_invitation_status ON oas.invitation( status );

CREATE TABLE oas.invitation_mail_out
(
	id					BIGSERIAL PRIMARY KEY,
	survey_id			BIGINT NOT NULL REFERENCES oas.survey(id) ON DELETE CASCADE,
	mail_out_type		VARCHAR(16) NOT NULL,
	date_created		TIMESTAMP NOT NULL,
	from_address		VARCHAR(256) NOT NULL,
	subject				VARCHAR(256) NOT NULL,
	body				TEXT NOT NULL
);

CREATE INDEX oas_idx_inv_mail_out_survey_id ON oas.invitation_mail_out( survey_id );
CREATE INDEX oas_idx_inv_mail_out_mot ON oas.invitation_mail_out( mail_out_type );

CREATE TABLE oas.invitation_mail_queue
(
	id					BIGSERIAL PRIMARY KEY,
	mail_out_id			BIGINT NOT NULL REFERENCES oas.invitation_mail_out(id) ON DELETE CASCADE,
	invitation_id		BIGINT NOT NULL REFERENCES oas.invitation(id) ON DELETE CASCADE,
	status				VARCHAR(16) NOT NULL,
	date_created		TIMESTAMP NOT NULL,
	date_sent			TIMESTAMP,
	error_string		VARCHAR(1024)
);

CREATE INDEX oas_idx_invmq_mail_out_id ON oas.invitation_mail_queue( mail_out_id );
CREATE INDEX oas_idx_invmq_invitation_id ON oas.invitation_mail_queue( invitation_id );
CREATE INDEX oas_idx_invmq_status ON oas.invitation_mail_queue( status );
CREATE INDEX oas_idx_invmq_date_sent ON oas.invitation_mail_queue( date_sent );
CREATE INDEX oas_idx_invmq_error_string ON oas.invitation_mail_queue( error_string );

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



CREATE TABLE oas.choice
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	question_id			BIGINT NOT NULL REFERENCES oas.question(id),
	display_order		INT
);

CREATE INDEX oas_idx_choice_question_id ON oas.choice( question_id );


CREATE TABLE oas.response_question_history
(
	response_id			BIGINT NOT NULL REFERENCES oas.response(id) ON DELETE CASCADE,
	question_id			BIGINT NOT NULL REFERENCES oas.question(id) ON DELETE CASCADE,
	index_order			INT NOT NULL,
	
	-- Composite key
	PRIMARY KEY ( response_id, question_id, index_order )
);

CREATE INDEX oas_idx_respones_question_history_response_id ON oas.response_question_history( response_id );
CREATE INDEX oas_idx_respones_question_history_question_id ON oas.response_question_history( question_id );

CREATE TABLE oas.response_question_choice_history
(
	response_id			BIGINT NOT NULL REFERENCES oas.response(id) ON DELETE CASCADE,
	choice_id			BIGINT NOT NULL REFERENCES oas.choice(id) ON DELETE CASCADE,
	index_order			INT NOT NULL,
	
	-- Composite key
	PRIMARY KEY ( response_id, choice_id, index_order )
);

CREATE INDEX oas_idx_response_question_choice_history_response_id ON oas.response_question_choice_history( response_id );
CREATE INDEX oas_idx_response_question_choice_history_choice_id_id ON oas.response_question_choice_history( choice_id );



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



CREATE TABLE oas.boolean_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	default_value		BOOLEAN
);

CREATE TABLE oas.choice_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	unlimited			BOOLEAN NOT NULL DEFAULT FALSE,
	randomize			BOOLEAN NOT NULL DEFAULT FALSE,
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

CREATE TABLE oas.page_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	
	show_back			BOOLEAN NOT NULL DEFAULT FALSE,
	show_forward		BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE oas.scale_question
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.question(id) ON DELETE CASCADE,
	minimum				INT NOT NULL,
	maximum				INT NOT NULL,
	labels_only			BOOLEAN NOT NULL DEFAULT FALSE,
--	default_value		INT
);

CREATE INDEX oas_idx_scale_question_min_max ON oas.scale_question( minimum, maximum );

CREATE TABLE oas.scale_question_label
(
	scale_question_id	BIGINT NOT NULL REFERENCES oas.scale_question ON DELETE CASCADE,
	language_id			BIGINT NOT NULL REFERENCES oas.supported_language ON DELETE CASCADE,
	scale_value			INT NOT NULL,
	label_value			VARCHAR(32768),
	
	PRIMARY KEY ( scale_question_id, language_id, scale_value )
);
CREATE INDEX oas_idx_scale_question_label_index ON oas.scale_question_label( scale_question_id );
CREATE INDEX oas_idx_scale_question_label_index2 ON oas.scale_question_label( scale_question_id, language_id );


-- ==========================================================================

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
CREATE INDEX oas_idx_choice_answer_sum_value ON oas.choice_answer( sum_value );

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




-- --------------------------------------------------------------------------
-- CUSTOM REPORTS
-- --------------------------------------------------------------------------


--CREATE TABLE oas.custom_report
--(
--	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
--	survey_id			BIGINT NOT NULL REFERENCES oas.base_object(id) ON DELETE CASCADE,
--	closed_min			TIMESTAMP,	
--	closed_max			TIMESTAMP	
--);
--
--CREATE TABLE oas.report_slice
--(
--	id					BIGSERIAL PRIMARY KEY,
--	report_id			BIGINT NOT NULL REFERENCES oas.custom_report(id) ON DELETE CASCADE,
--	question_id			BIGINT NOT NULL REFERENCES oas.question(id) ON DELETE CASCADE,
--	slice_type			VARCHAR(64) NOT NULL,
--	display_order		INT NOT NULL,
--	closed_min			TIMESTAMP,	
--	closed_max			TIMESTAMP,
--	contains_text		TEXT,
--	numeric_value		BIGINT,
--	
--	UNIQUE ( report_id, display_order )
--);
--
--CREATE TABLE oas.report_slice_choice
--(
--	slice_id			BIGINT NOT NULL REFERENCES oas.report_slice(id) ON DELETE CASCADE,
--	choice_id			BIGINT NOT NULL REFERENCES oas.choice(id) ON DELETE CASCADE,
--	
--	PRIMARY KEY ( slice_id, choice_id )
--);
--
--
--CREATE TABLE oas.report_item
--(
--	id					BIGSERIAL PRIMARY KEY,
--	report_id			BIGINT NOT NULL REFERENCES oas.custom_report(id) ON DELETE CASCADE,
--	question_id			BIGINT REFERENCES oas.question(id) ON DELETE CASCADE,
--	item_type			VARCHAR(64) NOT NULL,
--	display_order		INT NOT NULL,
--	
--	UNIQUE ( report_id, display_order )
--);


-- --------------------------------------------------------------------------



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



-- Hostname to use on public URLs - invites, lost password, etc.
INSERT INTO work.configuration_item( application_id, identifier, item_value )
	VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'publicHostname', 'www.FIXME.com' );

-- Hostname to use on "short" public URLs - for starting a new response from a small URL - MUST include protocol and trailing slash 
-- if required for the feature to work (eg., as opposed to ending in "?id="), as the application will NOT modify this value in any way
--
-- DEFAULT is to not set this item but it's here for reference and copying
--
--INSERT INTO work.configuration_item( application_id, identifier, item_value )
--	VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'shortUrlPrefix', 'http://www.FIXME.com/' );

-- Location of "public content" files
INSERT INTO work.configuration_item( application_id, identifier, item_value )
	VALUES ( (SELECT id FROM work.application WHERE identifier='OAS'), 'publicContentFilesystemPrefix', '/home/oaspubliccontent/public/' );

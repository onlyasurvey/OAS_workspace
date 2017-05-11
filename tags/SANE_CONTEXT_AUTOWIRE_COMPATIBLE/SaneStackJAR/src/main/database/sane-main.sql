DROP SCHEMA work CASCADE;
CREATE SCHEMA work;
SET search_path = work;

CREATE TABLE application
(
	id							BIGSERIAL PRIMARY KEY,
	identifier 				VARCHAR(255) UNIQUE NOT NULL,
	name_en				VARCHAR(64) UNIQUE,
	name_fr				VARCHAR(64) UNIQUE,
	start_url				VARCHAR(255)
);


CREATE TABLE resource_string
(
	id							BIGSERIAL PRIMARY KEY,
	application_id		BIGINT NOT NULL REFERENCES application(id) ON UPDATE CASCADE ON DELETE CASCADE,
	identifier				VARCHAR(255) NOT NULL,
	last_modified_date	TIMESTAMP NOT NULL,
	value_en				VARCHAR(4000) ,
	value_fr				VARCHAR(4000) ,
	
	UNIQUE( application_id, identifier )
);

CREATE TABLE actor
(
	id							BIGSERIAL PRIMARY KEY
);

CREATE TABLE user_account
(
	id							BIGINT PRIMARY KEY REFERENCES actor(id),
	username				VARCHAR(64) NOT NULL UNIQUE,
	md5_password	VARCHAR(32),
	email					VARCHAR(255) UNIQUE
);

CREATE TABLE actor_group
(
	id							BIGINT PRIMARY KEY REFERENCES actor(id),
	application_id		BIGINT NOT NULL REFERENCES application(id),
	name_en				VARCHAR(256) NOT NULL,
	name_fr				VARCHAR(256) NOT NULL
);

CREATE TABLE actor_group_member
(
	actor_id				BIGINT NOT NULL REFERENCES actor(id),
	group_id				BIGINT NOT NULL REFERENCES actor_group(id),
	
	UNIQUE( actor_id, group_id )
);

CREATE TABLE configuration_item
(
	id							BIGSERIAL PRIMARY KEY,
	application_id		BIGINT NOT NULL REFERENCES application(id),
	title_id					BIGINT REFERENCES resource_string(id),
	
	-- key part of the key/value pair
	identifier				VARCHAR(255) NOT NULL,
	value_type			VARCHAR(32) NOT NULL DEFAULT 'string',
	
	item_value			VARCHAR(4000),
		
	-- can only be defined once
	UNIQUE( application_id, identifier )	
);

CREATE TABLE role_definition
(
	id							BIGSERIAL PRIMARY KEY,
	application_id		BIGINT NOT NULL REFERENCES application(id),
	identifier				VARCHAR(128) NOT NULL,
	
	UNIQUE( application_id, identifier )
);

CREATE TABLE actor_role
(
	id							BIGSERIAL PRIMARY KEY,
	actor_id				BIGINT NOT NULL REFERENCES actor(id)
		ON UPDATE CASCADE ON DELETE CASCADE
	,
	role_id					BIGINT NOT NULL REFERENCES role_definition(id)
		ON UPDATE CASCADE ON DELETE CASCADE
	,
	UNIQUE( actor_id, role_id )
);

CREATE TABLE preference_definition
(
	id							BIGSERIAL PRIMARY KEY,
	application_id		BIGINT NOT NULL REFERENCES application(id) ON UPDATE CASCADE ON DELETE CASCADE,
	identifier				VARCHAR(128) NOT NULL,
	
	name_en				VARCHAR(128) NOT NULL,
	name_fr				VARCHAR(128) NOT NULL,
	
	UNIQUE( application_id, identifier )
);

CREATE TABLE preference_value
(
	id							BIGSERIAL PRIMARY KEY,
	definition_id			BIGINT NOT NULL REFERENCES preference_definition(id) ON UPDATE CASCADE ON DELETE CASCADE,
	actor_id				BIGINT NOT NULL REFERENCES actor(id) ON UPDATE CASCADE ON DELETE CASCADE,
	pref_value			VARCHAR(4000) NOT NULL,
	
	UNIQUE( definition_id, actor_id )
);





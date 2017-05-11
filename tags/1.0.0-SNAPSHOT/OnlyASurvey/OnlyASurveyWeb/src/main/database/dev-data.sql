
-- Create a user account for Jason
INSERT INTO actor( id ) VALUES ( DEFAULT );
INSERT INTO user_account( id, username, email, md5_password ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1), 'jason', 'xhalliday@gmail.com', md5('3ryp11c')
	);
INSERT INTO oas.account_owner( id, firstname, lastname, organization, telephone, learned_about, ip_on_join, last_login, language_id ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1),
	'Jason',
	'Halliday',
	'Inforealm, Inc.',
	'613-555-1212',
	'Co-founder',
	'127.0.0.1',
	NOW(),
	(SELECT id FROM oas.supported_language WHERE iso3lang='eng')
	);

-- application access

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='xhalliday@gmail.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_USER')
	);

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='xhalliday@gmail.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_ENTERPRISE_ADMIN')
	);

	

-- Create a user account for Mark
INSERT INTO actor( id ) VALUES ( DEFAULT );
INSERT INTO user_account( id, username, email, md5_password ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1), 'mmckay', 'mark@onlyasurvey.com', md5('Mark.222')
	);
INSERT INTO oas.account_owner( id, firstname, lastname, organization, telephone, learned_about, ip_on_join, last_login, language_id ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1),
	'Mark',
	'McKay',
	'Spokentext, Inc.',
	'613-555-1212',
	'Co-founder',
	'127.0.0.1',
	NOW(),
	(SELECT id FROM oas.supported_language WHERE iso3lang='eng')
	);

-- application access

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='mark@onlyasurvey.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_USER')
	);

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='mark@onlyasurvey.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_ENTERPRISE_ADMIN')
	);

-- CMS prefix for a Windows box
UPDATE work.configuration_item 
	SET item_value = 'D:\\workspace\\PublicSiteContent'
	WHERE
		application_id = (SELECT id FROM work.application WHERE identifier='OAS')
		AND identifier = 'publicContentFilesystemPrefix'
	;	

-- Create a user account for JFC
INSERT INTO actor( id ) VALUES ( DEFAULT );
INSERT INTO user_account( id, username, email, md5_password ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1), 'jchenier', 'jfc@onlyasurvey.com', md5('rome92')
	);
INSERT INTO oas.account_owner( id, firstname, lastname, organization, telephone, learned_about, ip_on_join, last_login, language_id ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1),
	'JF',
	'Chenier',
	'Spokentext, Inc.',
	'613-555-1212',
	'Dev',
	'127.0.0.1',
	NOW(),
	(SELECT id FROM oas.supported_language WHERE iso3lang='eng')
	);

-- application access

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='jfc@onlyasurvey.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_USER')
	);

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='jfc@onlyasurvey.com'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='OAS' and r.application_id = a.id ) WHERE r.identifier='ROLE_ENTERPRISE_ADMIN')
	);

	

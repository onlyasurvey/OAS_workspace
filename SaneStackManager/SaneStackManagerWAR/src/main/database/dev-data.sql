--
-- Data for manual testing/inspection/development.  It will ONLY work if it's run after a big-bang.
--

-- Create a user account
INSERT INTO actor( id ) VALUES ( DEFAULT );
INSERT INTO user_account( id, username, email, md5_password ) VALUES (
	(SELECT id FROM actor ORDER BY id DESC LIMIT 1), 'test', 	'test@test.test', md5('test')
	);

-- SADMAN access

INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='test@test.test'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='SADMAN' and r.application_id = a.id ) WHERE r.identifier='ROLE_USER')
	);




--
-- some test app
--
INSERT INTO application( identifier, name_en, name_fr ) VALUES ('MSDA', 'Multi-Channel Service Delivery Assistant', 'MSDA');
INSERT INTO role_definition( application_id, identifier) VALUES (
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'ROLE_USER'
	);
INSERT INTO role_definition( application_id, identifier) VALUES (
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'ROLE_NATIONAL_SECRETARIAT'
	);
INSERT INTO role_definition( application_id, identifier) VALUES (
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'ROLE_APPLICATION_ADMIN'
	);
INSERT INTO configuration_item( application_id, identifier, value_type, item_value ) VALUES ( 
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'faxServiceEnabled', 'boolean', 'true'
	);
INSERT INTO configuration_item( application_id, identifier, value_type, item_value ) VALUES ( 
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'readOnlyMode', 'boolean', 'false'
	);
INSERT INTO configuration_item( application_id, identifier, value_type, item_value ) VALUES ( 
	(SELECT id FROM application WHERE identifier = 'MSDA'),
	'nationalSecretariateEmail', 'string', 'natsec@canadabusiness.com'
	);


INSERT INTO actor_role ( actor_id, role_id ) VALUES (
	(SELECT id FROM user_account WHERE email='test@test.test'),
	(SELECT r.id FROM role_definition r JOIN application a ON ( a.identifier='MSDA' and r.application_id = a.id ) WHERE r.identifier='ROLE_APPLICATION_ADMIN')
	);




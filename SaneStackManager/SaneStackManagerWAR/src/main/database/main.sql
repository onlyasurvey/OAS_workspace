

INSERT INTO application( identifier, name_en, name_fr ) VALUES ('SADMAN', 'SADMAN', 'SADMAN');
INSERT INTO role_definition( application_id, identifier) VALUES (
	(SELECT id FROM application WHERE identifier = 'SADMAN'),
	'ROLE_USER'
	);
INSERT INTO role_definition( application_id, identifier) VALUES (
	(SELECT id FROM application WHERE identifier = 'SADMAN'),
	'ROLE_APPLICATION_ADMIN'
	);



--
-- Configuration items and default configuration values
--
INSERT INTO configuration_item( application_id, identifier, value_type, item_value ) VALUES (
	(SELECT id FROM application WHERE identifier = 'SADMAN'),
	'OFFLINE_FOR_MAINTENANCE',
	'boolean',
	'false'
	);

INSERT INTO configuration_item( application_id, identifier, value_type, item_value ) VALUES (
	(SELECT id FROM application WHERE identifier = 'SADMAN'),
	'OFFLINE_REASON',
	'boolean',
	'false'
	);
	
	
	

CREATE TABLE oas.account_type
(
	id					BIGINT NOT NULL PRIMARY KEY REFERENCES oas.base_object(id) ON DELETE CASCADE,
	identifier			VARCHAR(32) NOT NULL UNIQUE,
	
);
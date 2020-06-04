CREATE TABLE directories
(
	id     SERIAL NOT NULL
		CONSTRAINT directories_pkey
			PRIMARY KEY,
	name   TEXT   NOT NULL,
	parent INTEGER
		CONSTRAINT directories_parent_fkey
			REFERENCES directories
			ON UPDATE CASCADE ON DELETE CASCADE
);

ALTER TABLE directories
	OWNER TO postgres;

CREATE TABLE servers
(
	id   SERIAL NOT NULL
		CONSTRAINT servers_pkey
			PRIMARY KEY,
	url  TEXT   NOT NULL
		CONSTRAINT servers_url_key
			UNIQUE,
	name TEXT
);
INSERT INTO servers (id, url, name) VALUES (1, 'http://localhost:9001', 'server1');
INSERT INTO servers (id, url, name) VALUES (2, 'http://localhost:9002', 'server2');
INSERT INTO servers (id, url, name) VALUES (3, 'http://localhost:9003', 'server3');


ALTER TABLE servers
	OWNER TO postgres;

CREATE TABLE files
(
	id            SERIAL    NOT NULL
		CONSTRAINT files_pkey
			PRIMARY KEY,
	name          TEXT      NOT NULL,
	creation_date TIMESTAMP NOT NULL,
	size          INTEGER,
	hash          CHAR(32)  NOT NULL
		CONSTRAINT files_hash_check
			CHECK (length(hash) = 32),
	directory     INTEGER
		CONSTRAINT files_directory_fkey
			REFERENCES directories
			ON UPDATE CASCADE ON DELETE CASCADE
);

ALTER TABLE files
	OWNER TO postgres;

CREATE TABLE servers_files
(
	server_id INTEGER NOT NULL
		CONSTRAINT servers_files_server_id_fkey
			REFERENCES servers
			ON UPDATE CASCADE ON DELETE CASCADE,
	file_id   INTEGER NOT NULL
		CONSTRAINT servers_files_file_id_fkey
			REFERENCES files
			ON UPDATE CASCADE ON DELETE CASCADE
);

ALTER TABLE servers_files
	OWNER TO postgres;

CREATE FUNCTION create_file(_name TEXT, _hash TEXT, _size INTEGER, _directory INTEGER) RETURNS INTEGER
	LANGUAGE plpgsql
AS
$$
DECLARE
	_id INT;
BEGIN
	IF _name IS NULL THEN
		RETURN -1;
	END IF;
	IF length(_hash) != 32 THEN
		RETURN -2;
	END IF;
	IF _size <= 0 THEN
		RETURN -3;
	END IF;
	IF (_directory IS NOT NULL) AND (NOT EXISTS(SELECT id FROM directories WHERE id = _directory)) THEN
		RETURN -4;
	END IF;
	INSERT INTO files (name, creation_date, size, hash, directory)
	VALUES (_name, current_timestamp, _size, _hash, _directory)
	RETURNING id INTO _id;
	RETURN _id;
END;
$$;

ALTER FUNCTION create_file(TEXT, TEXT, INTEGER, INTEGER) OWNER TO postgres;

CREATE FUNCTION create_file(_name TEXT, _hash TEXT, _size INTEGER) RETURNS INTEGER
	LANGUAGE plpgsql
AS
$$
BEGIN
	RETURN (SELECT create_file(_name, _hash, _size, NULL));
END;
$$;

ALTER FUNCTION create_file(TEXT, TEXT, INTEGER) OWNER TO postgres;

CREATE FUNCTION create_directory(_name CHARACTER VARYING, _parent INTEGER) RETURNS INTEGER
	LANGUAGE plpgsql
AS
$$
DECLARE
	_id INT;
BEGIN
	IF _name IS NULL THEN
		RETURN -1;
	END IF;
	SELECT btrim(_name) INTO _name;
	IF _name NOT SIMILAR TO '[a-zA-Z0-9_ ]+' OR length(_name) = 0 THEN
		RETURN -4;
	END IF;
	IF (_parent IS NOT NULL) THEN
		IF NOT EXISTS(SELECT id FROM directories WHERE id = _parent) THEN
			RETURN -2;
		END IF;
		IF EXISTS(SELECT id FROM directories WHERE parent = _parent AND name = _name) THEN
			RETURN -3;
		END IF;
	ELSE
		IF EXISTS(SELECT id FROM directories WHERE parent IS NULL AND name = _name) THEN
			RETURN -3;
		END IF;
	END IF;
	INSERT INTO directories (name, parent) VALUES (_name, _parent) RETURNING id INTO _id;
	RETURN _id;
END
$$;

ALTER FUNCTION create_directory(VARCHAR, INTEGER) OWNER TO postgres;

CREATE FUNCTION get_path_to_directory(_id INTEGER)
	RETURNS TABLE
			(
				id     INTEGER,
				name   TEXT,
				p_id   INTEGER,
				p_name TEXT
			)
	LANGUAGE plpgsql
AS
$$
DECLARE
	_parent_id INT;
BEGIN
	SELECT d.parent FROM directories d WHERE d.id = _id INTO _parent_id;
	IF _parent_id IS NULL THEN
		RETURN QUERY (SELECT d.id AS id, d.name AS name, d.parent AS p_id, NULL AS p_name
					  FROM directories d
					  WHERE d.id = _id);
	ELSE
		RETURN QUERY (SELECT *
					  FROM get_path_to_directory(_parent_id)
					  UNION ALL
					  SELECT d.id AS id, d.name AS name, d.parent AS p_id, dd.name AS p_name
					  FROM directories d
							   JOIN directories dd ON dd.id = d.parent
					  WHERE d.id = _id);
	END IF;
END;
$$;

ALTER FUNCTION get_path_to_directory(INTEGER) OWNER TO postgres;

CREATE FUNCTION remove_from_server(_file_id INTEGER, _server_id INTEGER) RETURNS INTEGER
	LANGUAGE plpgsql
AS
$$
BEGIN
	IF EXISTS(SELECT ' ' FROM servers_files WHERE server_id = _server_id AND file_id = _file_id) THEN
		DELETE FROM servers_files WHERE server_id = _server_id AND file_id = _file_id;
		RETURN 0;
	END IF;
	IF NOT EXISTS(SELECT id FROM servers WHERE id = _server_id) THEN
		RETURN -2;
	END IF;
	IF NOT EXISTS(SELECT id FROM files WHERE id = _file_id) THEN
		RETURN -1;
	END IF;
	RETURN -3;
END;
$$;

ALTER FUNCTION remove_from_server(INTEGER, INTEGER) OWNER TO postgres;

CREATE FUNCTION add_to_server(_file_id INTEGER, _server_id INTEGER) RETURNS INTEGER
	LANGUAGE plpgsql
AS
$$
BEGIN
	IF NOT EXISTS(SELECT id FROM files WHERE id = _file_id) THEN
		RETURN -1;
	END IF;
	IF NOT EXISTS(SELECT id FROM servers WHERE id = _server_id) THEN
		RETURN -2;
	END IF;
	IF NOT EXISTS(SELECT 'a' FROM servers_files WHERE server_id = _server_id AND file_id = _file_id) THEN
		INSERT INTO servers_files (server_id, file_id) VALUES (_server_id, _file_id);
	END IF;
	RETURN 0;
END;
$$;

ALTER FUNCTION add_to_server(INTEGER, INTEGER) OWNER TO postgres;



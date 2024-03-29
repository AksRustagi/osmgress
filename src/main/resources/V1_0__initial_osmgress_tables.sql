CREATE TYPE faction AS ENUM ('green', 'blue');

CREATE TABLE osmg_user (
  id bigserial NOT NULL,
  name character varying(50) NOT NULL,
  password character (64) NOT NULL,
  faction faction NOT NULL,
  CONSTRAINT osmg_user_pkey PRIMARY KEY (id),
  CONSTRAINT osmg_user_name_key UNIQUE (name)
) WITH (OIDS=FALSE);
ALTER TABLE osmg_user OWNER TO osmgress;
GRANT ALL ON TABLE osmg_user TO postgres;

CREATE TABLE osmg_userroles (
  name character varying(50) NOT NULL,
  role character varying(50) NOT NULL,
  primary key (name, role)
) WITH (OIDS=FALSE);
ALTER TABLE osmg_userroles OWNER TO osmgress;
GRANT ALL ON TABLE osmg_userroles TO postgres;

CREATE TABLE osmg_portal(
  id bigserial NOT NULL,
  osm_id bigint NOT NULL,
  owner_id bigint,
  way geometry(Point,3857),
  name text,
  slots json,
  CONSTRAINT osmg_portal_owner_id_fkey FOREIGN KEY (owner_id)
      REFERENCES osmg_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT osmg_portal_pkey PRIMARY KEY (id),
  CONSTRAINT osmg_portal_osm_id_key UNIQUE (osm_id)
) WITH (OIDS=FALSE);
ALTER TABLE osmg_portal OWNER TO osmgress;
GRANT ALL ON TABLE osmg_portal TO postgres;

CREATE TABLE osmg_link(
  id bigserial NOT NULL,
  source_id bigint NOT NULL,
  target_id bigint NOT NULL,
  owner_id bigint NOT NULL,
  CONSTRAINT osmg_link_source_id_fkey FOREIGN KEY (source_id)
      REFERENCES osmg_portal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT osmg_link_target_id_fkey FOREIGN KEY (target_id)
      REFERENCES osmg_portal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (OIDS=FALSE);
ALTER TABLE osmg_link OWNER TO osmgress;
GRANT ALL ON TABLE osmg_link TO postgres;

INSERT INTO osmg_user(name, faction, password) values ('green', 'green', concat('{SHA}', encode(digest('green', 'sha256'), 'base64')));
INSERT INTO osmg_user(name, faction, password) values ('blue', 'blue', concat('{SHA}', encode(digest('blue', 'sha256'), 'base64')));

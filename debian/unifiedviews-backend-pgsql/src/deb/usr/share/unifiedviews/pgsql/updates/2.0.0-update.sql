
DROP VIEW exec_view;

DROP VIEW pipeline_view;

CREATE SEQUENCE seq_organization
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_permission
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_role
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE TABLE organization (
	id integer NOT NULL,
	name character varying(256) NOT NULL
);

CREATE TABLE permission (
	id integer NOT NULL,
	name character varying(142) NOT NULL,
	rwonly boolean
);

CREATE TABLE "role" (
	id integer NOT NULL,
	name character varying(142) NOT NULL
);

CREATE TABLE user_role_permission (
	role_id integer NOT NULL,
	permission_id integer NOT NULL
);

CREATE TABLE usr_extuser (
	id_usr integer NOT NULL,
	id_extuser character varying(256) NOT NULL
);

ALTER TABLE exec_pipeline
	ADD COLUMN organization_id integer;

ALTER TABLE exec_schedule
	ADD COLUMN organization_id integer;

ALTER TABLE ppl_model
	ADD COLUMN organization_id integer;

ALTER TABLE usr_user
	ALTER COLUMN username TYPE character varying(256) /* TYPE change - table: usr_user original: character varying(25) new: character varying(256) */,
	ALTER COLUMN u_password TYPE character(256) /* TYPE change - table: usr_user original: character(142) new: character(256) */,
	ALTER COLUMN full_name TYPE character varying(256) /* TYPE change - table: usr_user original: character varying(55) new: character varying(256) */;

ALTER TABLE organization
	ADD CONSTRAINT organization_pkey PRIMARY KEY (id);

ALTER TABLE permission
	ADD CONSTRAINT permission_pkey PRIMARY KEY (id);

ALTER TABLE "role"
	ADD CONSTRAINT role_pkey PRIMARY KEY (id);

ALTER TABLE user_role_permission
	ADD CONSTRAINT user_role_permission_pkey PRIMARY KEY (role_id, permission_id);

ALTER TABLE usr_extuser
	ADD CONSTRAINT usr_extuser_pkey PRIMARY KEY (id_usr, id_extuser);

ALTER TABLE exec_pipeline
	ADD CONSTRAINT exec_pipeline_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE exec_schedule
	ADD CONSTRAINT exec_schedule_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE organization
	ADD CONSTRAINT organization_name_key UNIQUE (name);

ALTER TABLE ppl_model
	ADD CONSTRAINT ppl_model_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES organization(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE permission
	ADD CONSTRAINT permission_name_key UNIQUE (name);

ALTER TABLE "role"
	ADD CONSTRAINT role_name_key UNIQUE (name);

ALTER TABLE user_role_permission
	ADD CONSTRAINT user_role_permission_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES permission(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE user_role_permission
	ADD CONSTRAINT user_role_permission_role_id_fkey FOREIGN KEY (role_id) REFERENCES role(id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE usr_extuser
	ADD CONSTRAINT usr_extuser_id_usr_fkey FOREIGN KEY (id_usr) REFERENCES usr_user(id) ON UPDATE CASCADE ON DELETE CASCADE;


INSERT INTO "role" VALUES (nextval('seq_role'), 'Administrator'),(nextval('seq_role'),'User'); 

INSERT INTO "permission" VALUES (nextval('seq_permission'), 'administrator', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.delete', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.save', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.edit', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.export', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportScheduleRules', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.import', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.importScheduleRules', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.importUserData', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.schedule', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.read', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.runDebug', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportDpuData', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportDpuJars', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.setVisibilityAtCreate', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.delete', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.stop', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.run', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.downloadAllLogs', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.read', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readDpuInputOutputData', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readEvent', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readLog', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.sparqlDpuInputOutputData', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.create', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.delete', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.edit', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.disable', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.enable', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.read', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.execute', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.setPriority', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.create', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.save', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.setVisibilityAtCreate', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.delete', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.edit', true);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.export', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.copy', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.import', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.read', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.management', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.create', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.edit', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.login', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.read', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.delete', true);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.create', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.edit', true);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.read', false);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.delete', true);
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.create', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.copy', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'deleteDebugResources', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'runtimeProperties.edit', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'userNotificationSettings.editEmailGlobal', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'userNotificationSettings.editNotificationFrequency', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'userNotificationSettings.createPipelineExecutionSettings', false);
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));

INSERT INTO "usr_extuser" VALUES ((select id from "usr_user" where username='admin'), 'admin');
INSERT INTO "usr_extuser" VALUES ((select id from "usr_user" where username='user'), 'user');

update "usr_user_role" set role_id=2 where role_id=1;
update "usr_user_role" set role_id=1 where role_id=0;

ALTER TABLE usr_user_role
	ADD CONSTRAINT usr_user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES role(id) ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX "ix_EXEC_PIPELINE_organization_id" ON exec_pipeline USING btree (organization_id);

CREATE INDEX "ix_EXEC_SCHEDULE_organization_id" ON exec_schedule USING btree (organization_id);

CREATE INDEX ix_organization_name ON organization USING btree (name);

CREATE INDEX "ix_PPL_MODEL_organization_id" ON ppl_model USING btree (organization_id);

CREATE INDEX ix_permission_name ON permission USING btree (name);

CREATE INDEX ix_role_name ON "role" USING btree (name);

CREATE VIEW exec_view AS
	SELECT exec.id,
    exec.status,
    ppl.id AS pipeline_id,
    ppl.name AS pipeline_name,
    exec.debug_mode,
    exec.t_start,
    exec.t_end,
    exec.schedule_id,
    owner.username AS owner_name,
    exec.stop,
    exec.t_last_change,
    org.name AS org_name
   FROM (((exec_pipeline exec
     LEFT JOIN ppl_model ppl ON ((ppl.id = exec.pipeline_id)))
     LEFT JOIN usr_user owner ON ((owner.id = exec.owner_id)))
     LEFT JOIN organization org ON ((exec.organization_id = org.id)));

CREATE VIEW pipeline_view AS
	SELECT ppl.id,
    ppl.name,
    exec.t_start,
    exec.t_end,
    exec.status,
    usr.username AS usr_name,
    org.name AS org_name,
    ppl.visibility
   FROM (((ppl_model ppl
     LEFT JOIN exec_last_view exec ON ((exec.pipeline_id = ppl.id)))
     LEFT JOIN usr_user usr ON ((ppl.user_id = usr.id)))
     LEFT JOIN organization org ON ((ppl.organization_id = org.id)));

-- Update version.
UPDATE "properties" SET "value" = '002.000.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '002.000.000' WHERE "key" = 'UV.Plugin-DevEnv.version';

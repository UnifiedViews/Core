-- Update permission table
ALTER TABLE permission RENAME COLUMN rwonly TO sharedEntityInstanceWriteRequired;
-- Update version.
UPDATE "properties" SET "value" = '002.001.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '002.001.000' WHERE "key" = 'UV.Plugin-DevEnv.version';

-- Add new columns
ALTER TABLE "dpu_instance"
ADD COLUMN "menu_name" VARCHAR(255);
ALTER TABLE "dpu_template"
ADD COLUMN "menu_name" VARCHAR(255);

-- Email notifications
ALTER TABLE "sch_sch_notification"
ADD COLUMN "type_started" SMALLINT DEFAULT 2;

ALTER TABLE "sch_usr_notification"
ADD COLUMN "type_started" SMALLINT DEFAULT 2;

ALTER TABLE "sch_usr_notification"
ADD COLUMN "report_not_scheduled" boolean DEFAULT false;

-- full name for user is now mandatory parameter - copy username if missing
UPDATE usr_user SET full_name = username WHERE full_name IS NULL;
ALTER TABLE usr_user ALTER COLUMN full_name SET NOT NULL;

-- Organizations removed
DROP VIEW pipeline_view;
DROP VIEW exec_view;

CREATE SEQUENCE "seq_user_actor" START 1;
CREATE TABLE "user_actor"
(
    "id" INTEGER,
    "id_extuser" VARCHAR(256) NOT NULL,
    "name" VARCHAR(256) NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("id_extuser")
);

ALTER TABLE exec_pipeline DROP COLUMN organization_id;
ALTER TABLE exec_pipeline ADD COLUMN user_actor_id INTEGER;
CREATE INDEX "ix_EXEC_PIPELINE_actor_id" ON "exec_pipeline" ("user_actor_id");
ALTER TABLE exec_schedule DROP COLUMN organization_id;
ALTER TABLE exec_schedule ADD COLUMN user_actor_id INTEGER;
CREATE INDEX "ix_EXEC_SCHEDULE_user_actor_id" ON "exec_schedule" ("user_actor_id");
ALTER TABLE ppl_model DROP COLUMN organization_id;
ALTER TABLE ppl_model ADD COLUMN user_actor_id INTEGER;
CREATE INDEX "ix_PPL_MODEL_user_actor_id" ON "ppl_model" ("user_actor_id");
DROP TABLE organization;

-- Constraints
ALTER TABLE "exec_pipeline"
ADD FOREIGN KEY ("user_actor_id")
    REFERENCES "user_actor" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "exec_schedule"
ADD FOREIGN KEY ("user_actor_id")
    REFERENCES "user_actor" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "ppl_model"
ADD FOREIGN KEY ("user_actor_id")
    REFERENCES "user_actor" ("id")
    ON UPDATE CASCADE ON DELETE CASCADE;

-- Recreate views
CREATE VIEW "pipeline_view" AS
SELECT ppl.id AS id, ppl.name AS name, exec.t_start AS t_start, exec.t_end AS t_end, exec.status AS status, usr.username as usr_name, usr.full_name as usr_full_name, 
ppl.visibility AS visibility, actor.name AS user_actor_name FROM "ppl_model" AS ppl
LEFT JOIN "exec_last_view" AS exec ON exec.pipeline_id = ppl.id
LEFT JOIN "usr_user" AS usr ON ppl.user_id = usr.id 
LEFT JOIN "user_actor" AS actor ON ppl.user_actor_id = actor.id;

CREATE VIEW "exec_view" AS
SELECT exec.id AS id, exec.status AS status, ppl.id AS pipeline_id, ppl.name AS pipeline_name, exec.debug_mode AS debug_mode, exec.t_start AS t_start, 
exec.t_end AS t_end, exec.schedule_id AS schedule_id, owner.username AS owner_name, owner.full_name AS owner_full_name, exec.stop AS stop, exec.t_last_change AS t_last_change, 
actor.name AS user_actor_name FROM "exec_pipeline" AS exec
LEFT JOIN "ppl_model" AS ppl ON ppl.id = exec.pipeline_id
LEFT JOIN "usr_user" AS owner ON owner.id = exec.owner_id
LEFT JOIN "user_actor" AS actor ON actor.id = exec.user_actor_id;
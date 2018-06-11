-- #############################################################################
-- ##    Database update script for both standard and eDemo UV installation    #
-- ##    Update from v. 2.1.x to 2.2.0                                         #
-- #############################################################################
CREATE SEQUENCE "seq_backend_servers" START 1;
CREATE TABLE "backend_servers"
(
	"id" INTEGER,
	"backend_id" VARCHAR(128),
	"last_update" TIMESTAMP,
	PRIMARY KEY ("id"),
    UNIQUE ("backend_id")
);

ALTER TABLE "exec_pipeline" ADD COLUMN "backend_id" VARCHAR(128);
CREATE INDEX "ix_EXEC_PIPELINE_backend_id" ON "exec_pipeline" ("backend_id");

DROP VIEW IF EXISTS "exec_view";
CREATE VIEW "exec_view" AS
SELECT exec.id AS id, exec.status AS status, ppl.id AS pipeline_id, ppl.name AS pipeline_name, exec.debug_mode AS debug_mode, exec.t_start AS t_start, 
exec.t_end AS t_end, exec.schedule_id AS schedule_id, owner.username AS owner_name, owner.full_name AS owner_full_name, exec.stop AS stop, exec.t_last_change AS t_last_change, 
exec.backend_id AS backend_id, actor.name AS user_actor_name FROM "exec_pipeline" AS exec
LEFT JOIN "ppl_model" AS ppl ON ppl.id = exec.pipeline_id
LEFT JOIN "usr_user" AS owner ON owner.id = exec.owner_id
LEFT JOIN "user_actor" AS actor ON actor.id = exec.user_actor_id;

-- Update version.
UPDATE "properties" SET "value" = '002.002.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '002.001.004' WHERE "key" = 'UV.Plugin-DevEnv.version';
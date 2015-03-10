CREATE SEQUENCE "seq_organization" START 1;
CREATE TABLE "organization"
(
 "id" INTEGER,
 "name" varchar(256) NOT NULL,
 PRIMARY KEY ("id"),
 UNIQUE ("name")
);
CREATE INDEX "ix_organization_name" ON "organization" ("name");

ALTER TABLE "dpu_template" ADD COLUMN  "organization_id" INTEGER;
CREATE INDEX "ix_DPU_TEMPLATE_organization_id" ON "dpu_template" ("organization_id");

ALTER TABLE "exec_pipeline" ADD COLUMN "organization_id" INTEGER;
CREATE INDEX "ix_EXEC_PIPELINE_organization_id" ON "exec_pipeline" ("organization_id");

ALTER TABLE "exec_schedule" ADD COLUMN "organization_id" INTEGER;
CREATE INDEX "ix_EXEC_SCHEDULE_organization_id" ON "exec_schedule" ("organization_id");

ALTER TABLE "ppl_model" ADD COLUMN "organization_id" INTEGER;
CREATE INDEX "ix_PPL_MODEL_organization_id" ON "ppl_model" ("organization_id");


ALTER TABLE "exec_pipeline"
ADD FOREIGN KEY ("organization_id")
 REFERENCES "organization" ("id")
 ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE "exec_schedule"
ADD FOREIGN KEY ("organization_id")
 REFERENCES "organization" ("id")
 ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE "ppl_model"
ADD FOREIGN KEY ("organization_id")
 REFERENCES "organization" ("id")
 ON UPDATE CASCADE ON DELETE CASCADE;


CREATE TABLE "usr_extuser" (
 "id_usr" INTEGER NOT NULL,
 "id_extuser" varchar(256) NOT NULL,
 PRIMARY KEY ("id_usr","id_extuser")
);

 ALTER TABLE "user_role_permission"
 ADD FOREIGN KEY ("permission_id")
 REFERENCES "permission" ("id")
 ON DELETE CASCADE ON UPDATE CASCADE;

 ALTER TABLE "user_role_permission"
 ADD FOREIGN KEY ("role_id")
 REFERENCES "role" ("id")
 ON DELETE CASCADE ON UPDATE CASCADE;

 ALTER TABLE "usr_extuser"
 ADD FOREIGN KEY ("id_usr")
 REFERENCES "usr_user" ("id")
 ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "usr_user" ALTER "username" VARCHAR(256) NOT NULL;
ALTER TABLE "usr_user" ALTER "u_password" CHAR(256) NOT NULL;
ALTER TABLE "usr_user" ALTER "full_name" VARCHAR(256);


CREATE SEQUENCE "seq_permission" START WITH 1;
CREATE TABLE "permission" (
"id" INTEGER,
"name" varchar(142) NOT NULL,
"rwonly" boolean,
PRIMARY KEY ("id"),
UNIQUE ("name")
);
CREATE INDEX "ix_permission_name" ON "permission" ("name");

-- Update version.
UPDATE "properties" SET "value" = '001.006.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '001.003.000' WHERE "key" = 'UV.Plugin-DevEnv.version';

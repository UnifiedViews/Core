-- Update version.
UPDATE "properties" SET "value" = '002.001.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '002.000.000' WHERE "key" = 'UV.Plugin-DevEnv.version';

ALTER TABLE "dpu_instance"
ADD COLUMN "menu_name" VARCHAR(255);

ALTER TABLE "dpu_template"
ADD COLUMN "menu_name" VARCHAR(255);

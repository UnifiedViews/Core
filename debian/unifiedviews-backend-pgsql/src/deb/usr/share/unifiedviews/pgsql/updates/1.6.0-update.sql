-- Update version.
UPDATE "properties" SET "value" = '001.006.000' WHERE "key" = 'UV.Core.version';
UPDATE "properties" SET "value" = '001.003.000' WHERE "key" = 'UV.Plugin-DevEnv.version';

ALTER TABLE ppl_model ADD CONSTRAINT ppl_model_name_constraint UNIQUE(name);

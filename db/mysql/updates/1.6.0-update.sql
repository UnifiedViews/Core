
-- Update version.
UPDATE `properties` SET `value` = '001.006.000' WHERE `key` = 'UV.Core.version';
UPDATE `properties` SET `value` = '001.003.000' WHERE `key` = 'UV.Plugin-DevEnv.version';

-- This constraint is only limited to first 255 characters in column. Larger constraint is only
-- possible with 'innodb_large_prefix' setting on database.
ALTER TABLE ppl_model ADD UNIQUE (name(255));

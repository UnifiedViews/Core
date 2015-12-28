-- Update version.
UPDATE `properties` SET `value` = '001.005.000' WHERE `key` = 'UV.Core.version';
UPDATE `properties` SET `value` = '001.001.000' WHERE `key` = 'UV.Plugin-DevEnv.version';

-- Add property for template shared configuration.
ALTER TABLE `dpu_instance` ADD COLUMN `use_template_config` SMALLINT NOT NULL DEFAULT 0;

-- Views.

# Last execution for each pipeline
CREATE VIEW `exec_last_view` AS
SELECT id, pipeline_id, t_end, t_start, status
FROM `exec_pipeline` AS exec
WHERE t_end = (SELECT max(t_end) FROM `exec_pipeline` AS lastExec WHERE exec.pipeline_id = lastExec.pipeline_id);

# Pipeline list
CREATE VIEW `pipeline_view` AS
SELECT ppl.id AS id, ppl.name AS name, exec.t_start AS t_start, exec.t_end AS t_end, exec.status AS status
FROM `ppl_model` AS ppl
LEFT JOIN `exec_last_view` AS exec ON exec.pipeline_id = ppl.id;

# Execution list.
CREATE VIEW `exec_view` AS
SELECT exec.id AS id, exec.status AS status, ppl.id AS pipeline_id, ppl.name AS pipeline_name, exec.debug_mode AS debug_mode, exec.t_start AS t_start, exec.t_end AS t_end, exec.schedule_id AS schedule_id, owner.username AS owner_name, exec.stop AS stop, exec.t_last_change AS t_last_change
FROM `exec_pipeline` AS exec
JOIN `ppl_model` AS ppl ON ppl.id = exec.pipeline_id
JOIN `usr_user` AS owner ON owner.id = exec.owner_id;

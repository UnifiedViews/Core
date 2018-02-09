SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `dpu_template` 
ADD COLUMN `organization_id` INT(11) NULL DEFAULT NULL AFTER `user_id`,
ADD INDEX `ix_DPU_TEMPLATE_organization_id` (`organization_id` ASC);

ALTER TABLE `exec_pipeline` 
ADD COLUMN `organization_id` INT(11) NULL DEFAULT NULL AFTER `owner_id`,
ADD INDEX `ix_EXEC_PIPELINE_organization_id` (`organization_id` ASC);

ALTER TABLE `exec_schedule` 
ADD COLUMN `organization_id` INT(11) NULL DEFAULT NULL AFTER `user_id`,
ADD INDEX `ix_EXEC_SCHEDULE_organization_id` (`organization_id` ASC);

CREATE TABLE IF NOT EXISTS `organization` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name` (`name` ASC),
  INDEX `ix_organization_name` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS `permission` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(142) NOT NULL,
  `rwonly` TINYINT(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name` (`name` ASC),
  INDEX `ix_permission_name` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

ALTER TABLE `ppl_model` 
ADD COLUMN `organization_id` INT(11) NULL DEFAULT NULL AFTER `user_id`,
ADD INDEX `ix_PPL_MODEL_organization_id` (`organization_id` ASC);

CREATE TABLE IF NOT EXISTS `role` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(142) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name` (`name` ASC),
  INDEX `ix_role_name` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS `user_role_permission` (
  `role_id` INT(11) NOT NULL,
  `permission_id` INT(11) NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS `usr_extuser` (
  `id_usr` INT(11) NOT NULL,
  `id_extuser` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id_usr`, `id_extuser`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

ALTER TABLE `usr_user_role` 
ADD INDEX `role_id` (`role_id` ASC);

ALTER TABLE `exec_pipeline` 
ADD CONSTRAINT `exec_pipeline_ibfk_6`
  FOREIGN KEY (`organization_id`)
  REFERENCES `organization` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `exec_schedule` 
ADD CONSTRAINT `exec_schedule_ibfk_3`
  FOREIGN KEY (`organization_id`)
  REFERENCES `organization` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `ppl_model` 
ADD CONSTRAINT `ppl_model_ibfk_2`
  FOREIGN KEY (`organization_id`)
  REFERENCES `organization` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `usr_user_role` 
ADD CONSTRAINT `usr_user_role_ibfk_2`
  FOREIGN KEY (`role_id`)
  REFERENCES `role` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

DROP VIEW IF EXISTS `exec_last_view`;
CREATE VIEW `exec_last_view` AS
SELECT id, pipeline_id, t_end, t_start, status
FROM `exec_pipeline` AS exec
WHERE t_end = (SELECT max(t_end) FROM `exec_pipeline` AS lastExec WHERE exec.pipeline_id = lastExec.pipeline_id);

DROP VIEW IF EXISTS `exec_view`;
CREATE VIEW `exec_view` AS
SELECT exec.id AS id, exec.status AS status, ppl.id AS pipeline_id, ppl.name AS pipeline_name, exec.debug_mode AS debug_mode, exec.t_start AS t_start, 
exec.t_end AS t_end, exec.schedule_id AS schedule_id, owner.username AS owner_name, exec.stop AS stop, exec.t_last_change AS t_last_change,
org.name AS org_name
FROM `exec_pipeline` AS exec
LEFT JOIN `ppl_model` AS ppl ON ppl.id = exec.pipeline_id
LEFT JOIN `usr_user` AS owner ON owner.id = exec.owner_id
left JOIN `organization` as org ON exec.organization_id = org.id;

DROP VIEW IF EXISTS `pipeline_view`;
CREATE VIEW `pipeline_view` AS
SELECT ppl.id AS id, ppl.name AS name, exec.t_start AS t_start, exec.t_end AS t_end, exec.status AS status, usr.username as usr_name, org.name 
AS org_name , ppl.visibility AS visibility FROM `ppl_model` AS ppl
LEFT JOIN `exec_last_view` AS exec ON exec.pipeline_id = ppl.id
LEFT JOIN `usr_user` AS usr ON ppl.user_id = usr.id
left JOIN `organization` as org ON ppl.organization_id = org.id;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

INSERT INTO `role` VALUES (NULL, 'Administrator');
INSERT INTO `role` VALUES (NULL,'User');

-- INSERT INTO `permission` VALUES (nextval('seq_permission'), 'pipeline.definePipelineDependencies');
INSERT INTO `permission` VALUES (NULL, 'administrator', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.delete', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.save', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.edit', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.export', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.exportScheduleRules', false);
INSERT INTO `permission` VALUES (NULL, 'pipeline.import', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.importScheduleRules', false);
INSERT INTO `permission` VALUES (NULL, 'pipeline.importUserData', false);
INSERT INTO `permission` VALUES (NULL, 'pipeline.schedule', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.read', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.runDebug', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.exportDpuData', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.exportDpuJars', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.setVisibilityAtCreate', false);
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.delete', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.stop', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.run', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.downloadAllLogs', false);
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.read', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.readDpuInputOutputData', false);
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.readEvent', false);
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.readLog', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipelineExecution.sparqlDpuInputOutputData', false);
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.create', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.delete', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.edit', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.disable', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.enable', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.read', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.execute', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'scheduleRule.setPriority', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.create', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.save', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.setVisibilityAtCreate', false);
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.delete', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.edit', true);
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.export', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.copy', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.import', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'dpuTemplate.read', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'user.management', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'user.create', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'user.edit', false);
INSERT INTO `permission` VALUES (NULL, 'user.login', false);
INSERT INTO `permission` VALUES (NULL, 'user.read', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'user.delete', true);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'role.create', false);
INSERT INTO `permission` VALUES (NULL, 'role.edit', true);
INSERT INTO `permission` VALUES (NULL, 'role.read', false);
INSERT INTO `permission` VALUES (NULL, 'role.delete', true);
INSERT INTO `permission` VALUES (NULL, 'pipeline.create', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'pipeline.copy', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'deleteDebugResources', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'runtimeProperties.edit', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'userNotificationSettings.editEmailGlobal', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'userNotificationSettings.editNotificationFrequency', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));
INSERT INTO `permission` VALUES (NULL, 'userNotificationSettings.createPipelineExecutionSettings', false);
INSERT INTO `user_role_permission` values((select id from `role` where name='Administrator'), (SELECT max(id) FROM  `permission`));
INSERT INTO `user_role_permission` values((select id from `role` where name='User'), (SELECT max(id) FROM  `permission`));

INSERT INTO `usr_extuser` VALUES ((select id from usr_user where username='admin'), 'admin');
INSERT INTO `usr_extuser` VALUES ((select id from usr_user where username='user'), 'user');

DELETE FROM `usr_user_role`;
INSERT INTO `usr_user_role` VALUES ((select id from usr_user where username='admin'),(select id from role where name='Administrator'));
INSERT INTO `usr_user_role` VALUES ((select id from usr_user where username='admin'),(select id from role where name='User'));
INSERT INTO `usr_user_role` VALUES ((select id from usr_user where username='user'),(select id from role where name='User'));

-- Update version.
UPDATE `properties` SET `value` = '002.000.000' WHERE `key` = 'UV.Core.version';
UPDATE `properties` SET `value` = '002.000.000' WHERE `key` = 'UV.Plugin-DevEnv.version';

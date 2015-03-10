DROP VIEW IF EXISTS `pipeline_view`;
DROP VIEW IF EXISTS `exec_last_view`;
DROP VIEW IF EXISTS `exec_view`;
DROP TABLE IF EXISTS `exec_dataunit_info`;
DROP TABLE IF EXISTS `exec_context_dpu`;
DROP TABLE IF EXISTS `exec_record`;
DROP TABLE IF EXISTS `ppl_open_event`;
DROP TABLE IF EXISTS `exec_pipeline`;
DROP TABLE IF EXISTS `exec_context_pipeline`;
DROP TABLE IF EXISTS `exec_schedule_after`;
DROP TABLE IF EXISTS `ppl_edge`;
DROP TABLE IF EXISTS `ppl_node`;
DROP TABLE IF EXISTS `ppl_graph`;
DROP TABLE IF EXISTS `sch_sch_notification_email`;
DROP TABLE IF EXISTS `sch_sch_notification`;
DROP TABLE IF EXISTS `sch_usr_notification_email`;
DROP TABLE IF EXISTS `sch_usr_notification`;
DROP TABLE IF EXISTS `exec_schedule`;
DROP TABLE IF EXISTS `ppl_ppl_conflicts`;
DROP TABLE IF EXISTS `ppl_model`;
DROP TABLE IF EXISTS `dpu_instance`;
DROP TABLE IF EXISTS `dpu_template`;
DROP TABLE IF EXISTS `ppl_position`;
DROP TABLE IF EXISTS `user_role_permission`;
DROP TABLE IF EXISTS `permission`;
DROP TABLE IF EXISTS `usr_extuser`;
DROP TABLE IF EXISTS `usr_user_role`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `usr_user`;
DROP TABLE IF EXISTS `organization`;
DROP TABLE IF EXISTS `sch_email`;
DROP TABLE IF EXISTS `rdf_ns_prefix`;
DROP TABLE IF EXISTS `properties`;
DROP TABLE IF EXISTS `runtime_properties`;

CREATE TABLE `dpu_instance`
(
-- DPURecord
  `id` INTEGER AUTO_INCREMENT,
  `name` VARCHAR(1024),
  `use_dpu_description` boolean,
  `description` TEXT,
  `configuration` LONGBLOB,
  `config_valid` boolean NOT NULL,
-- DPUInstaceRecord
  `dpu_id` INTEGER,
  `use_template_config` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_DPU_INSTANCE_dpu_id` ON `dpu_instance` (`dpu_id`);

CREATE TABLE `dpu_template`
(
-- DPURecord
  `id` INTEGER AUTO_INCREMENT,
  `name` VARCHAR(1024),
  `use_dpu_description` boolean,
  `description` TEXT,  
  `configuration` LONGBLOB,
  `parent_id` INTEGER,
  `config_valid` boolean NOT NULL,
-- DPUTemplateRecord
  `user_id` INTEGER,
  `organization_id` INTEGER,
  `visibility` SMALLINT,
  `type` SMALLINT,
  `jar_directory` VARCHAR(255),
  `jar_name` VARCHAR(255),
  `jar_description` VARCHAR(1024),  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_DPU_TEMPLATE_jar_directory` ON `dpu_template` (`jar_directory`);
CREATE INDEX `ix_DPU_TEMPLATE_parent_id` ON `dpu_template` (`parent_id`);
CREATE INDEX `ix_DPU_TEMPLATE_user_id` ON `dpu_template` (`user_id`);
CREATE INDEX `ix_DPU_TEMPLATE_organization_id` ON `dpu_template` (`organization_id`);
CREATE INDEX `ix_DPU_TEMPLATE_visibility` ON `dpu_template` (`visibility`);

CREATE TABLE `exec_dataunit_info`
(
  `id` INTEGER AUTO_INCREMENT,
  `name` VARCHAR(2048),
  `idx` INTEGER,
  `type` SMALLINT,
  `is_input` boolean,
  `exec_context_dpu_id` INTEGER,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_EXEC_DATAUNIT_INFO_exec_context_dpu_id` ON `exec_dataunit_info` (`exec_context_dpu_id`);

CREATE TABLE `exec_context_pipeline`
(
  `id` INTEGER AUTO_INCREMENT,
  `dummy` boolean,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `exec_context_dpu`
(
  `id` INTEGER AUTO_INCREMENT,
  `exec_context_pipeline_id` INTEGER,
  `dpu_instance_id` INTEGER,
  `state` SMALLINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_EXEC_CONTEXT_DPU_exec_context_pipeline_id` ON `exec_context_dpu` (`exec_context_pipeline_id`);
CREATE INDEX `ix_EXEC_CONTEXT_DPU_dpu_instance_id` ON `exec_context_dpu` (`dpu_instance_id`);

CREATE TABLE `exec_record`
(
  `id` INTEGER AUTO_INCREMENT,
  `r_time` DATETIME,
  `r_type` SMALLINT,
  `dpu_id` INTEGER,
  `execution_id` INTEGER,
  `short_message` VARCHAR(128),
  `full_message` TEXT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_EXEC_RECORD_r_time` ON `exec_record` (`r_time`);
CREATE INDEX `ix_EXEC_RECORD_r_type` ON `exec_record` (`r_type`);
CREATE INDEX `ix_EXEC_RECORD_dpu_id` ON `exec_record` (`dpu_id`);
CREATE INDEX `ix_EXEC_RECORD_execution_id` ON `exec_record` (`execution_id`);

CREATE TABLE `exec_pipeline`
(
  `id` INTEGER AUTO_INCREMENT,
  `status` INTEGER,
  `pipeline_id` INTEGER,
  `debug_mode` boolean,
  `t_start` DATETIME,
  `t_end` DATETIME,
  `context_id` INTEGER,
  `schedule_id` INTEGER,
  `silent_mode` boolean,
  `debugnode_id` INTEGER,
  `stop` boolean,
  `t_last_change` DATETIME,
  `owner_id` INTEGER,
  `organization_id` INTEGER,
 `order_number` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_EXEC_PIPELINE_status` ON `exec_pipeline` (`status`);
CREATE INDEX `ix_EXEC_PIPELINE_pipeline_id` ON `exec_pipeline` (`pipeline_id`);
CREATE INDEX `ix_EXEC_PIPELINE_debug_mode` ON `exec_pipeline` (`debug_mode`);
-- Virtuoso 7 cannot handle the following index for some reason, see GH-952.
-- CREATE INDEX `ix_EXEC_PIPELINE_t_start` ON `exec_pipeline` (`t_start`);
CREATE INDEX `ix_EXEC_PIPELINE_context_id` ON `exec_pipeline` (`context_id`);
CREATE INDEX `ix_EXEC_PIPELINE_schedule_id` ON `exec_pipeline` (`schedule_id`);
CREATE INDEX `ix_EXEC_PIPELINE_owner_id` ON `exec_pipeline` (`owner_id`);
CREATE INDEX `ix_EXEC_PIPELINE_organization_id` ON `exec_pipeline` (`organization_id`);


CREATE TABLE `exec_schedule`
(
  `id` INTEGER AUTO_INCREMENT,
  `description` TEXT,
  `pipeline_id` INTEGER NOT NULL,
  `user_id` INTEGER,
  `organization_id` INTEGER,
 `just_once` boolean,
  `enabled` boolean,
  `type` SMALLINT,
  `first_exec` DATETIME,
  `last_exec` DATETIME,
  `time_period` INTEGER,
  `period_unit` SMALLINT,
  `strict_timing` boolean,
  `strict_tolerance` INTEGER,
  `priority` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- composite index to optimize fetching schedules following pipeline
CREATE INDEX `ix_EXEC_SCHEDULE_pipeline_id_type` ON `exec_schedule` (`pipeline_id`, `type`);
CREATE INDEX `ix_EXEC_SCHEDULE_user_id` ON `exec_schedule` (`user_id`);
CREATE INDEX `ix_EXEC_SCHEDULE_organization_id` ON `exec_schedule` (`organization_id`);
CREATE INDEX `ix_EXEC_SCHEDULE_enabled` ON `exec_schedule` (`enabled`);
CREATE INDEX `ix_EXEC_SCHEDULE_type` ON `exec_schedule` (`type`);

CREATE TABLE `exec_schedule_after`
(
  `schedule_id` INTEGER,
  `pipeline_id` INTEGER,
  PRIMARY KEY (`schedule_id`, `pipeline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `ppl_model`
(
  `id` INTEGER AUTO_INCREMENT,
  `name` VARCHAR(1024),
  `description` TEXT,
  `user_id` INTEGER,
  `organization_id` INTEGER,
 `visibility` SMALLINT,
  `last_change` DATETIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_MODEL_user_id` ON `ppl_model` (`user_id`);
CREATE INDEX `ix_PPL_MODEL_organization_id` ON `ppl_model` (`organization_id`);

CREATE TABLE `ppl_ppl_conflicts`
(
  `pipeline_id` INTEGER AUTO_INCREMENT,
  `pipeline_conflict_id` INTEGER,
  PRIMARY KEY (`pipeline_id`, `pipeline_conflict_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_PPL_CONFLICTS_pipeline_id` ON `ppl_ppl_conflicts` (`pipeline_id`);

CREATE TABLE `ppl_edge`
(
  `id` INTEGER AUTO_INCREMENT,
  `graph_id` INTEGER,
  `node_from_id` INTEGER,
  `node_to_id` INTEGER,
  `data_unit_name` VARCHAR(2048),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_EDGE_graph_id` ON `ppl_edge` (`graph_id`);
CREATE INDEX `ix_PPL_EDGE_node_from_id` ON `ppl_edge` (`node_from_id`);
CREATE INDEX `ix_PPL_EDGE_node_to_id` ON `ppl_edge` (`node_to_id`);

CREATE TABLE `ppl_node`
(
  `id` INTEGER AUTO_INCREMENT,
  `graph_id` INTEGER,
  `instance_id` INTEGER,
  `position_id` INTEGER,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_NODE_graph_id` ON `ppl_node` (`graph_id`);
CREATE INDEX `ix_PPL_NODE_instance_id` ON `ppl_node` (`instance_id`);

CREATE TABLE `ppl_graph`
(
  `id` INTEGER AUTO_INCREMENT,
  `pipeline_id` INTEGER,
  PRIMARY KEY (`id`),
  UNIQUE (pipeline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_GRAPH_pipeline_id` ON `ppl_graph` (`pipeline_id`);

CREATE TABLE `ppl_position`
(
  `id` INTEGER AUTO_INCREMENT,
  `pos_x` INTEGER,
  `pos_y` INTEGER,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `runtime_properties`
(
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NULL,
  `value` VARCHAR(100) NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sch_sch_notification`
(
  `id` INTEGER AUTO_INCREMENT,
  `schedule_id` INTEGER NOT NULL,
  `type_success` SMALLINT,
  `type_error` SMALLINT,
  PRIMARY KEY (`id`),
  UNIQUE (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sch_usr_notification`
(
  `id` INTEGER AUTO_INCREMENT,
  `user_id` INTEGER NOT NULL,
  `type_success` SMALLINT,
  `type_error` SMALLINT,
  PRIMARY KEY (`id`),
  UNIQUE (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_SCH_USR_NOTIFICATION_user_id` ON `sch_usr_notification` (`user_id`);

CREATE TABLE `sch_email`
(
  `id` INTEGER AUTO_INCREMENT,
  `email` VARCHAR(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_SCH_EMAIL_email` ON `sch_email` (`email`);

CREATE TABLE `sch_sch_notification_email`
(
  `notification_id` INTEGER,
  `email_id` INTEGER,
  PRIMARY KEY (`notification_id`, `email_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sch_usr_notification_email`
(
  `notification_id` INTEGER AUTO_INCREMENT,
  `email_id` INTEGER,
  PRIMARY KEY (`notification_id`, `email_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_SCH_USR_NOTIFICATION_EMAIL_email_id` ON `sch_usr_notification_email` (`email_id`);


CREATE TABLE `organization`
(
  `id` INTEGER AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_organization_name` ON `organization` (`name`);

CREATE TABLE `usr_user`
(
  `id` INTEGER AUTO_INCREMENT,
  `username` VARCHAR(25) NOT NULL,
  `email_id` INTEGER,
  `u_password` CHAR(142) NOT NULL,
  `full_name` VARCHAR(55),
  `table_rows` INTEGER,
  PRIMARY KEY (`id`),
  UNIQUE (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_USR_USER_email_id` ON `usr_user` (`email_id`);

CREATE TABLE `permission` (
  `id` INTEGER AUTO_INCREMENT,
  `name` varchar(142) NOT NULL,
  `rwonly` boolean,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX `ix_permission_name` ON `permission` (`name`);

CREATE TABLE `role` (
  `id` INTEGER AUTO_INCREMENT,
  `name` varchar(142) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX `ix_role_name` ON `role` (`name`);

CREATE TABLE `user_role_permission` (
  `role_id` INTEGER NOT NULL,
  `permission_id` INTEGER NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `usr_user_role`
(
  `user_id` INTEGER NOT NULL,
  `role_id` INTEGER NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `usr_extuser` (
  `id_usr` INTEGER NOT NULL,
  `id_extuser` varchar(255) NOT NULL,
  PRIMARY KEY (`id_usr`,`id_extuser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `rdf_ns_prefix`
(
  `id` INTEGER AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `uri` VARCHAR(2048) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Table with timestamps when was the last time users opened pipelines in canvas
CREATE TABLE `ppl_open_event`
(
  `id` INTEGER AUTO_INCREMENT,
  `pipeline_id` INTEGER NOT NULL,
  `user_id` INTEGER NOT NULL,
  `opened` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX `ix_PPL_OPEN_EVENT_pipeline_id` ON `ppl_open_event` (`pipeline_id`);
CREATE INDEX `ix_PPL_OPEN_EVENT_user_id` ON `ppl_open_event` (`user_id`);

CREATE TABLE `properties`
(
  `key` VARCHAR(200) NOT NULL,
  `value` VARCHAR(200),
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- CONSTRAINTS #################################################################


-- Table `dpu_instance`
ALTER TABLE `dpu_instance`
ADD FOREIGN KEY (`dpu_id`)
    REFERENCES `dpu_template` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `dpu_template`
ALTER TABLE `dpu_template`
ADD FOREIGN KEY (`parent_id`)
    REFERENCES `dpu_template` (`id`)
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table `exec_dataunit_info`
ALTER TABLE `exec_dataunit_info`
ADD FOREIGN KEY (`exec_context_dpu_id`)
    REFERENCES `exec_context_dpu` (`id`)
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table `exec_context_dpu`
ALTER TABLE `exec_context_dpu`
ADD FOREIGN KEY (`exec_context_pipeline_id`)
    REFERENCES `exec_context_pipeline` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_context_dpu`
ADD FOREIGN KEY (`dpu_instance_id`)
    REFERENCES `dpu_instance` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `exec_record`
ALTER TABLE `exec_record`
ADD FOREIGN KEY (`dpu_id`)
    REFERENCES `dpu_instance` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_record`
ADD FOREIGN KEY (`execution_id`)
    REFERENCES `exec_pipeline` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `exec_pipeline`
ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`context_id`)
    REFERENCES `exec_context_pipeline` (`id`)
	ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`schedule_id`)
    REFERENCES `exec_schedule` (`id`)
	ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`debugnode_id`)
    REFERENCES `ppl_node` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`owner_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_pipeline`
ADD FOREIGN KEY (`organization_id`)
    REFERENCES `organization` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

-- Table `exec_schedule`
ALTER TABLE `exec_schedule`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_schedule`
ADD FOREIGN KEY (`user_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_schedule`
ADD FOREIGN KEY (`organization_id`)
    REFERENCES `organization` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

-- Table `exec_schedule_after`
ALTER TABLE `exec_schedule_after`
ADD FOREIGN KEY (`schedule_id`)
    REFERENCES `exec_schedule` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `exec_schedule_after`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `ppl_model`
ALTER TABLE `ppl_model`
ADD FOREIGN KEY (`user_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_model`
ADD FOREIGN KEY (`organization_id`)
    REFERENCES `organization` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

-- Table `ppl_ppl_conflicts`
ALTER TABLE `ppl_ppl_conflicts`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_ppl_conflicts`
ADD FOREIGN KEY (`pipeline_conflict_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `ppl_graph`
ALTER TABLE `ppl_graph`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `ppl_node`
ALTER TABLE `ppl_node`
ADD FOREIGN KEY (`graph_id`)
    REFERENCES `ppl_graph` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_node`
ADD FOREIGN KEY (`instance_id`)
    REFERENCES `dpu_instance` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_node`
ADD FOREIGN KEY (`position_id`)
    REFERENCES `ppl_position` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `ppl_edge`
ALTER TABLE `ppl_edge`
ADD FOREIGN KEY (`graph_id`)
    REFERENCES `ppl_graph` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_edge`
ADD FOREIGN KEY (`node_from_id`)
    REFERENCES `ppl_node` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_edge`
ADD FOREIGN KEY (`node_to_id`)
    REFERENCES `ppl_node` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `sch_sch_notification`
ALTER TABLE `sch_sch_notification`
ADD FOREIGN KEY (`schedule_id`)
    REFERENCES `exec_schedule` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `sch_sch_notification_email`
ALTER TABLE `sch_sch_notification_email`
ADD FOREIGN KEY (`notification_id`)
    REFERENCES `sch_sch_notification` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `sch_sch_notification_email`
ADD FOREIGN KEY (`email_id`)
    REFERENCES `sch_email` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `sch_usr_notification`
ALTER TABLE `sch_usr_notification`
ADD FOREIGN KEY (`user_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `sch_usr_notification_email`
ALTER TABLE `sch_usr_notification_email`
ADD FOREIGN KEY (`notification_id`)
    REFERENCES `sch_usr_notification` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `sch_usr_notification_email`
ADD FOREIGN KEY (`email_id`)
    REFERENCES `sch_email` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `usr_user`
ALTER TABLE `usr_user`
ADD FOREIGN KEY (`email_id`)
    REFERENCES `sch_email` (`id`)
	ON UPDATE CASCADE ON DELETE SET NULL;


-- Table `usr_user_role`
ALTER TABLE `usr_user_role`
ADD FOREIGN KEY (`user_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- Table `ppl_open_event`
ALTER TABLE `ppl_open_event`
ADD FOREIGN KEY (`user_id`)
    REFERENCES `usr_user` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `ppl_open_event`
ADD FOREIGN KEY (`pipeline_id`)
    REFERENCES `ppl_model` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;


-- TRIGGERS      ######################################################


-- BEGIN MYSQL ONLY

CREATE TRIGGER update_last_change BEFORE UPDATE ON `exec_pipeline`
 FOR EACH ROW SET NEW.t_last_change = NOW();
-- END MYSQL ONLY

-- TABLE FOR LOGS

DROP TABLE IF EXISTS `logging`;
CREATE TABLE `logging`
(
-- BEGIN MYSQL ONLY
 `id` INTEGER unsigned NOT NULL AUTO_INCREMENT,
-- END MYSQL ONLY
  `log_level` INT(11) NOT NULL,
  `timestmp` BIGINT NOT NULL,
  `logger` VARCHAR(254) NOT NULL,
  `message` TEXT,
  `dpu` INTEGER,
  `execution` INTEGER NOT NULL,
  `stack_trace` TEXT,
  `relative_id` INTEGER,
  PRIMARY KEY (id)
-- BEGIN MYSQL ONLY
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- END MYSQL ONLY

CREATE INDEX `ix_LOGGING_dpu` ON `logging` (`dpu`);
CREATE INDEX `ix_LOGGIN_execution` ON `logging` (`execution`);
CREATE INDEX `ix_LOGGIN_relative_id` ON `logging` (`relative_id`);

-- Views.

# Last execution for each pipeline
CREATE VIEW `exec_last_view` AS
SELECT id, pipeline_id, t_end, t_start, status
FROM `exec_pipeline` AS exec
WHERE t_end = (SELECT max(t_end) FROM `exec_pipeline` AS lastExec WHERE exec.pipeline_id = lastExec.pipeline_id);

# Pipeline list
CREATE VIEW `pipeline_view` AS
SELECT ppl.id AS id, ppl.name AS name, exec.t_start AS t_start, exec.t_end AS t_end, exec.status AS status, usr.username as usr_name, org.name AS org_name 
FROM `ppl_model` AS ppl
LEFT JOIN `exec_last_view` AS exec ON exec.pipeline_id = ppl.id
LEFT JOIN `usr_user` AS usr ON ppl.user_id = usr.id
left JOIN `organization` as org ON ppl.organization_id = org.id;

# Execution list.
CREATE VIEW `exec_view` AS
SELECT exec.id AS id, exec.status AS status, ppl.id AS pipeline_id, ppl.name AS pipeline_name, exec.debug_mode AS debug_mode, exec.t_start AS t_start, exec.t_end AS t_end, exec.schedule_id AS schedule_id, owner.username AS owner_name, exec.stop AS stop, exec.t_last_change AS t_last_change
FROM `exec_pipeline` AS exec
JOIN `ppl_model` AS ppl ON ppl.id = exec.pipeline_id
JOIN `usr_user` AS owner ON owner.id = exec.owner_id;

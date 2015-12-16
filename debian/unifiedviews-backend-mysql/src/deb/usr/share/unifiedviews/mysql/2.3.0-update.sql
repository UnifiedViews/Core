ALTER TABLE dpu_instance MODIFY use_dpu_description tinyint(1) DEFAULT '0';
ALTER TABLE dpu_instance DROP COLUMN config_valid;

ALTER TABLE dpu_template MODIFY use_dpu_description tinyint(1) DEFAULT '0';
ALTER TABLE dpu_template DROP COLUMN config_valid;

ALTER TABLE exec_dataunit_info MODIFY is_input tinyint(1) DEFAULT '0';

ALTER TABLE exec_context_pipeline MODIFY dummy tinyint(1) DEFAULT '0';

ALTER TABLE exec_record MODIFY r_type int(11) DEFAULT NULL;

ALTER TABLE backend_servers MODIFY last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE exec_pipeline MODIFY debug_mode tinyint(1) DEFAULT '0';
ALTER TABLE exec_pipeline MODIFY silent_mode tinyint(1) DEFAULT '0';
ALTER TABLE exec_pipeline MODIFY stop tinyint(1) DEFAULT '0';
ALTER TABLE exec_pipeline MODIFY order_number bigint(20) DEFAULT NULL;

ALTER TABLE exec_schedule MODIFY enabled tinyint(1) DEFAULT '0';
ALTER TABLE exec_schedule MODIFY just_once tinyint(1) DEFAULT '0';
ALTER TABLE exec_schedule MODIFY period_unit int(11) DEFAULT NULL;
ALTER TABLE exec_schedule MODIFY strict_timing tinyint(1) DEFAULT '0';

ALTER TABLE ppl_position ADD NODE_id bigint(20) DEFAULT NULL;
UPDATE ppl_position SET ppl_position.NODE_id = (SELECT ppl_node.id FROM ppl_node WHERE ppl_node.position_id=ppl_position.id);

ALTER TABLE sch_usr_notification MODIFY report_not_scheduled tinyint(1) DEFAULT '0';

ALTER TABLE usr_user MODIFY u_password varchar(142) DEFAULT NULL;

ALTER TABLE permission MODIFY sharedentityinstancewriterequired tinyint(1) DEFAULT '0';

ALTER TABLE logging MODIFY logger varchar(255) NOT NULL;

DROP TABLE properties;
DROP VIEW pipeline_view;
DROP VIEW exec_last_view;
DROP VIEW exec_view;


ALTER TABLE `ppl_node` DROP FOREIGN KEY `ppl_node_ibfk_2`;
DROP INDEX `ix_PPL_NODE_instance_id` ON `ppl_node`;
CREATE UNIQUE INDEX `ix_PPL_NODE_instance_id` ON `ppl_node` (`instance_id`);
ALTER TABLE `ppl_node`
ADD FOREIGN KEY (`instance_id`)
    REFERENCES `dpu_instance` (`id`)
	ON UPDATE CASCADE ON DELETE CASCADE;

CREATE UNIQUE INDEX `ix_PPL_NODE_position_id` ON `ppl_node` (`position_id`);


UPDATE `properties` SET `value` = '002.003.000' WHERE `key` = 'UV.Core.version';
UPDATE `properties` SET `value` = '002.001.005' WHERE `key` = 'UV.Plugin-DevEnv.version';

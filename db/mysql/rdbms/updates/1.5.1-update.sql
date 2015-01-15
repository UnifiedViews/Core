ALTER TABLE dpu_instance MODIFY use_dpu_description boolean;
ALTER TABLE dpu_instance MODIFY config_valid boolean;
ALTER TABLE dpu_instance MODIFY use_template_config boolean NOT NULL DEFAULT FALSE;
ALTER TABLE dpu_template MODIFY use_dpu_description boolean;
ALTER TABLE dpu_template MODIFY config_valid boolean NOT NULL;
ALTER TABLE exec_dataunit_info MODIFY is_input boolean;
ALTER TABLE exec_context_pipeline MODIFY dummy boolean;
ALTER TABLE exec_pipeline MODIFY debug_mode boolean;
ALTER TABLE exec_pipeline MODIFY silent_mode boolean;
ALTER TABLE exec_pipeline MODIFY stop boolean;
ALTER TABLE exec_schedule MODIFY just_once boolean;
ALTER TABLE exec_schedule MODIFY enabled boolean;
ALTER TABLE exec_schedule MODIFY strict_timing boolean;

INSERT INTO `runtime_properties` (name, value) VALUES ('locale', 'en');


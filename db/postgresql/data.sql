﻿INSERT INTO "properties" VALUES ('UV.Core.version','001.006.000'),('UV.Plugin-DevEnv.version','001.002.000');
INSERT INTO "sch_email" VALUES (nextval('seq_sch_email'),'admin@example.com'),(nextval('seq_sch_email'),'user@example.com');
INSERT INTO "role" VALUES (nextval('seq_role'), 'Administrator'),(nextval('seq_role'),'User'),(nextval('seq_role'),'Povinna osoba'),(nextval('seq_role'),'Spravca transformacii'); 

--INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.definePipelineDependencies');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.edit');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.export');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportDpuData');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportDpuJars');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.exportScheduleRules');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.import');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.importScheduleRules');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.importUserData');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.run');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.runDebug');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.setVisibilityAtCreate');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.downloadAllLogs');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readDpuInputOutputData');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readEvent');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.readLog');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipelineExecution.sparqlDpuInputOutputData');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.edit');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.execute');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'scheduleRule.setPriority');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.setVisibilityAtCreate');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.edit');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.export');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'dpuTemplate.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.edit');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.login');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'user.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.edit');
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.read');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
INSERT INTO "permission" VALUES (nextval('seq_permission'), 'role.delete');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));

INSERT INTO "usr_user" VALUES (nextval('seq_usr_user'),'admin',1,'100000:3069f2086098a66ec0a859ec7872b09af7866bc7ecafe2bed3ec394454056db2:b5ab4961ae8ad7775b3b568145060fabb76d7bca41c7b535887201f79ee9788a','John Admin',20);
INSERT INTO "usr_extuser" VALUES (currval('seq_usr_user'), 'admin');
INSERT INTO "usr_user" VALUES (nextval('seq_usr_user'),'user',2,'100000:3069f2086098a66ec0a859ec7872b09af7866bc7ecafe2bed3ec394454056db2:b5ab4961ae8ad7775b3b568145060fabb76d7bca41c7b535887201f79ee9788a','John User',20);
INSERT INTO "usr_extuser" VALUES (currval('seq_usr_user'), 'user');

INSERT INTO "sch_usr_notification" VALUES (nextval('seq_sch_notification'),1,1,1),(nextval('seq_sch_notification'),2,1,1);
INSERT INTO "sch_usr_notification_email" VALUES (1,1),(2,2);
INSERT INTO "usr_user_role" VALUES (1,1),(1,2),(2,1);
INSERT INTO "runtime_properties" ("id", "name", "value") VALUES (nextval('seq_runtime_properties'), 'backend.scheduledPipelines.limit', '5');
INSERT INTO "runtime_properties" ("id", "name", "value") VALUES (nextval('seq_runtime_properties'), 'run.now.pipeline.priority', '1');
INSERT INTO "runtime_properties" ("id", "name", "value") VALUES (nextval('seq_runtime_properties'), 'locale', 'en');

INSERT INTO "permission" VALUES (nextval('seq_permission'), 'pipeline.create');
INSERT INTO "user_role_permission" values((select id from "role" where name='Administrator'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='User'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Povinna osoba'), currval('seq_permission'));
INSERT INTO "user_role_permission" values((select id from "role" where name='Spravca transformacii'), currval('seq_permission'));
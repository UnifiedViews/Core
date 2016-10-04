package cz.cuni.mff.xrg.odcs.commons.app.auth;

public class EntityPermissions {

    // *********** Pipeline permissions
    public static final String PIPELINE_EDIT = "pipeline.edit";

    public static final String PIPELINE_RUN = "pipeline.run";

    public static final String PIPELINE_RUN_DEBUG = "pipeline.runDebug";

    public static final String PIPELINE_SCHEDULE = "pipeline.schedule";

    public static final String PIPELINE_READ = "pipeline.read";

    public static final String PIPELINE_COPY = "pipeline.copy";

    public static final String PIPELINE_EXPORT = "pipeline.export";

    public static final String PIPELINE_CREATE = "pipeline.create";

    public static final String PIPELINE_IMPORT = "pipeline.import";

    public static final String PIPELINE_EXPORT_DPU_DATA = "pipeline.exportDpuData";

    public static final String PIPELINE_EXPORT_DPU_JARS = "pipeline.exportDpuJars";

    public static final String PIPELINE_EXPORT_SCHEDULES = "pipeline.exportScheduleRules";

    public static final String PIPELINE_DELETE = "pipeline.delete";

    public static final String PIPELINE_EXECUTION_READ = "pipelineExcecution.read";

    public static final String PIPELINE_IMPORT_SCHEDULE_RULES = "pipeline.importScheduleRules";

    public static final String PIPELINE_IMPORT_USER_DATA = "pipeline.importUserData";

    public static final String PIPELINE_DEFINE_DEPENDENCIES = "pipeline.definePipelineDependencies";

    public static final String PIPELINE_EXECUTION_STOP = "pipelineExecution.stop";

    public static final String PIPELINE_SET_VISIBILITY = "pipeline.setVisibility";

    public static final String PIPELINE_SET_VISIBILITY_PUBLIC_RW = "pipeline.setVisibilityPublicRw";

    // ************  DPUTemplate permissions
    public static final String DPU_TEMPLATE_CREATE = "dpuTemplate.create";

    public static final String DPU_TEMPLATE_EXPORT = "dpuTemplate.export";

    public static final String DPU_TEMPLATE_EDIT = "dpuTemplate.edit";

    public static final String DPU_TEMPLATE_COPY = "dpuTemplate.copy";

    public static final String DPU_TEMPLATE_DELETE = "dpuTemplate.delete";

    public static final String DPU_TEMPLATE_SET_VISIBILITY = "dpuTemplate.setVisibility";

    public static final String DPU_TEMPLATE_CREATE_FROM_INSTANCE = "dpuTemplate.createFromInstance";

    public static final String DPU_TEMPLATE_SHOW_SCREEN = "dpuTemplate.showScreen";

    // ************* Schedule permissions
    public static final String SCHEDULE_RULE_CREATE = "scheduleRule.create";

    public static final String SCHEDULE_RULE_DELETE = "scheduleRule.delete";

    public static final String SCHEDULE_RULE_EDIT = "scheduleRule.edit";

}

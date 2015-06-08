package cz.cuni.mff.xrg.odcs.commons.app.conf;

/**
 * Recognized configuration properties.
 *
 * @author Jan Vojt
 */
public enum ConfigProperty {

    GENERAL_WORKINGDIR("general.workingdir"),

    BACKEND_NAME("backend.name"),
    BACKEND_HOST("backend.host"),
    BACKEND_PORT("backend.port"),
    BACKEND_LOG_DIR("backend.log.directory"),
    BACKEND_LOG_KEEP("backend.log.keepDays"),
    BACKEND_DEFAULTRDF("backend.defaultRdf"),
    BACKEND_LIMIT_OF_SCHEDULED_PIPELINES("backend.scheduledPipelines.limit"),
    LOCALE("locale"),

    EXECUTION_LOG_HISTORY("exec.log.history"),
    EXECUTION_LOG_SIZE_MAX("exec.log.msg.maxSize"),

    /**
     * Used to generate url for pipeline execution in emails.
     */
    FRONTEND_URL("frontend.url"),

    FRONTEND_RUN_NOW_PIPELINE_PRIORITY("run.now.pipeline.priority"),
    FRONTEND_THEME("frontend.theme"),

    /**
     * string diplayed next to the UV logo in GUI
     */
    INSTALLATION_NAME("installation.name"),

    // namespaces for virtuoso configuration
    RDBMS("database.sql"),
    RDF("database.rdf"),

    DATABASE_HOSTNAME("hostname"),
    DATABASE_PORT("port"),
    DATABASE_USER("user"),
    DATABASE_PASSWORD("password"),
    DATABASE_CHARSET("charset"),
    DATABASE_NAME("dbname"),
    DATABASE_PLATFORM("platform"),
    DATABASE_RETRIES("retries"),
    DATABASE_WAIT("wait"),
    DATABASE_EXTENSION("useExtension"),
    DATABASE_SQL_PASSWORD("database.sql.password"),
    DATABASE_RDF_PASSWORD("database.rdf.password"),

    // namespaces for database inaccessible actions
    DATABASE_INACCESSIBLE("inaccessible"),
    INACCESSIBLE_CMD("cmd"),
    INACCESSIBLE_PATH("path"),

    MODULE_PATH("module.path"),
    MODULE_FRONT_EXPOSE("module.frontend.expose"),
    MODULE_BACK_EXPOSE("module.backend.expose"),

    EMAIL_ENABLED("email.enabled"),
    EMAIL_SMTP_HOST("email.smtp.host"),
    EMAIL_SMTP_PORT("email.smtp.port"),
    EMAIL_SMTP_TLS("email.smtp.tls"),
    EMAIL_FROM_EMAIL("email.from"),
    EMAIL_FROM_NAME("email.from.name"),
    EMAIL_AUTHORIZATION("email.authorization"),
    EMAIL_USERNAME("email.username"),
    EMAIL_PASSWORD("email.password"),
    EMAIL_ADMIN("email.admin"),

    CKAN_LOCATION("ckan.location"),
    CKAN_API_KEY("ckan.api.key"),

    LOGOUT_URL("logout.url"),
    CAS_SERVER_HOST("cas.server.host"),
    CAS_SECURITY_CHECK_URL("cas.security.check.url"),
    CAS_LOGIN_URL("cas.login.url"),
    CAS_LOGOUT_URL("cas.logout.url"),

    ADMIN_PERMISSION("admin.permission"),

    MASTER_API_USER("master.api.user"),
    MASTER_API_PASSWORD("master.api.password"),

    CRYPTOGRAPHY_ENABLED("cryptography.enabled"),
    CRYPTOGRAPHY_KEY_FILE("cryptography.key.file"),

    DPU_UV_T_FILES_METADATA_POOL_PARTY_PASSWORD("dpu.uv-t-filesMetadata.pool.party.password"),
    DPU_UV_L_RELATIONAL_TO_CKAN_SECRET_TOKEN("dpu.uv-l-relationalToCkan.secret.token"),
    DPU_UV_L_RELATIONAL_DIFF_TO_CKAN_SECRET_TOKEN("dpu.uv-l-relationalDiffToCkan.secret.token"),
    DPU_UV_L_RDF_TO_CKAN_SECRET_TOKEN("dpu.uv-l-rdfToCkan.secret.token"),
    DPU_UV_L_FILES_TO_CKAN_SECRET_TOKEN("dpu.uv-l-filesToCkan.secret.token"),
    DPU_UV_L_RDF_TO_VIRTUOSO_PASSWORD("dpu.l-rdfToVirtuoso.password"),

    USE_LOCALIZED_DPU_NAME("dpu.name.localized"),

    FRONTEND_INITIAL_PAGE("frontend.initial.page");

    private final String property;

    public final String springValue;

    private ConfigProperty(final String property) {
        this.property = property;
        this.springValue = "${" + property + "}";
    }

    @Override
    public String toString() {
        return property;
    }
}

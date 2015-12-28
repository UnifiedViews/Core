/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    BACKEND_HTTP_PROBE_PORT("backend.probe.http.port"),
    BACKEND_LOG_DIR("backend.log.directory"),
    BACKEND_LOG_KEEP("backend.log.keepDays"),
    BACKEND_DEFAULTRDF("backend.defaultRdf"),
    BACKEND_LIMIT_OF_SCHEDULED_PIPELINES("backend.scheduledPipelines.limit"),
    BACKEND_ALIVE_LIMIT("backend.alive.limit"),
    BACKEND_ID("backend.id"),
    BACKEND_STARTUP_RESTART_RUNNING("backend.startup.restart.running"),
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

    RENEW_IFRAME_ENABLED("frontend.renew.iframe.enabled"),
    RENEW_IFRAME_SRC("frontend.renew.iframe.src"),

    DPU_UV_T_FILES_METADATA_POOL_PARTY_PASSWORD("dpu.uv-t-filesMetadata.pool.party.password"),
    DPU_UV_L_RELATIONAL_TO_CKAN_SECRET_TOKEN("dpu.uv-l-relationalToCkan.secret.token"),
    DPU_UV_L_RELATIONAL_DIFF_TO_CKAN_SECRET_TOKEN("dpu.uv-l-relationalDiffToCkan.secret.token"),
    DPU_UV_L_RDF_TO_CKAN_SECRET_TOKEN("dpu.uv-l-rdfToCkan.secret.token"),
    DPU_UV_L_FILES_TO_CKAN_SECRET_TOKEN("dpu.uv-l-filesToCkan.secret.token"),
    DPU_UV_L_RDF_TO_VIRTUOSO_PASSWORD("dpu.l-rdfToVirtuoso.password"),

    USE_LOCALIZED_DPU_NAME("dpu.name.localized"),

    FRONTEND_INITIAL_PAGE("frontend.initial.page"),
    EXTERNAL_MENU_LINK_NAME("external.menu.link.name"),
    EXTERNAL_MENU_LINK_URL("external.menu.link.url"),
    DPU_INSTANCE_USE_TEMPLATE_CONFIG("dpu.instance.template.config.enabled"),
    FRONTEND_PIPELINE_SHOW_COMPOSITE_BUTTONS("frontend.pipeline.composite.buttons.show");

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

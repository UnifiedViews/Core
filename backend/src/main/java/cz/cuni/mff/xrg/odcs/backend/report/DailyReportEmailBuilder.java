package cz.cuni.mff.xrg.odcs.backend.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

/**
 * Create email with daily report.
 * 
 * @author Petyr
 */
@Component
class DailyReportEmailBuilder {

    @Autowired
    private AppConfig config;

    public String buildFinishedExecutionsMail(List<PipelineExecution> executions) {
        StringBuilder body = new StringBuilder();

        addBackendInstanceName(body);

        String urlBase = getUrlBase();

        body.append("<table border=2 cellpadding=2 >");
        body.append("<tr bgcolor=\"#C0C0C0\">");
        body.append(Messages.getString("DailyReportEmailBuilder.header"));
        // add column for link
        if (urlBase != null) {
            body.append("<th>");
            body.append(Messages.getString("DailyReportEmailBuilder.detail"));
            body.append("</th>");
        }
        body.append("</tr>");

        for (PipelineExecution exec : executions) {
            body.append(EmailUtils.getTableRowHTMLStartTag(exec.getStatus()));
            // pipeline
            body.append("<td>");
            body.append(exec.getPipeline().getName());
            body.append("</td>");
            // execution id
            body.append("<td>");
            body.append(exec.getId().toString());
            body.append("</td>");
            // start
            body.append("<td>");
            body.append(EmailUtils.formatDate(exec.getStart()));
            body.append("</td>");
            // end
            body.append("<td>");
            body.append(EmailUtils.formatDate(exec.getEnd()));
            body.append("</td>");
            // executed by
            body.append("<td>");
            body.append(EmailUtils.getDisplayedExecutionOwner(exec));
            body.append("</td>");
            // result
            body.append("<td>");
            body.append(EmailUtils.getStatusAsString(exec.getStatus()));
            body.append("</td>");
            // link 
            if (urlBase != null) {
                body.append("<td> <a href=/");
                body.append(urlBase);
                body.append(exec.getId().toString());
                body.append("\" >");
                body.append(Messages.getString("DailyReportEmailBuilder.execution.detail"));
                body.append("<a/> </td>");
            }

            // end line
            body.append("</tr>");
        }
        body.append("</table>");

        body.append("<br>");

        return body.toString();
    }

    public String buildStartedExecutionsMail(List<PipelineExecution> executions) {
        StringBuilder body = new StringBuilder();

        addBackendInstanceName(body);

        String urlBase = getUrlBase();

        body.append("<table border=2 cellpadding=2 >");
        body.append("<tr bgcolor=\"#C0C0C0\">");
        body.append(Messages.getString("DailyReportEmailBuilder.started.header"));
        // add column for link
        if (urlBase != null) {
            body.append("<th>");
            body.append(Messages.getString("DailyReportEmailBuilder.detail"));
            body.append("</th>");
        }
        body.append("</tr>");

        for (PipelineExecution exec : executions) {
            body.append(EmailUtils.getTableRowHTMLStartTag(exec.getStatus()));
            // pipeline
            body.append("<td>");
            body.append(exec.getPipeline().getName());
            body.append("</td>");
            // execution id
            body.append("<td>");
            body.append(exec.getId().toString());
            body.append("</td>");
            // start
            body.append("<td>");
            body.append(EmailUtils.formatDate(exec.getStart()));
            body.append("</td>");
            // executed by
            body.append("<td>");
            body.append(EmailUtils.getDisplayedExecutionOwner(exec));
            body.append("</td>");
            // status
            body.append("<td>");
            body.append(EmailUtils.getStatusAsString(exec.getStatus()));
            body.append("</td>");
            // link 
            if (urlBase != null) {
                body.append("<td> <a href=/");
                body.append(urlBase);
                body.append(exec.getId().toString());
                body.append("\" >");
                body.append(Messages.getString("DailyReportEmailBuilder.execution.detail"));
                body.append("<a/> </td>");
            }

            // end line
            body.append("</tr>");
        }
        body.append("</table>");

        body.append("<br>");

        return body.toString();
    }

    private void addBackendInstanceName(StringBuilder body) {
        try {
            final String name = this.config.getString(ConfigProperty.BACKEND_NAME);
            body.append("<p>");
            body.append(Messages.getString("DailyReportEmailBuilder.instance"));
            body.append(name);
            body.append("</p><br/>");
        } catch (MissingConfigPropertyException e) {
            // no name is presented
        }
    }

    private String getUrlBase() {
        String urlBase = null;
        try {
            urlBase = this.config.getString(ConfigProperty.FRONTEND_URL);
            if (!urlBase.endsWith("/")) {
                urlBase = urlBase + "/";
            }
            urlBase = urlBase + "#!ExecutionList/exec=";
        } catch (MissingConfigPropertyException e) {
            // no name is presented
        }

        return urlBase;

    }

}

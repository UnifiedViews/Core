package cz.cuni.mff.xrg.odcs.backend.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.filter.Compare;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.DbMessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;

/**
 * Create email with instant report.
 * 
 * @author Petyr
 */
@Component
class InstantReportEmailBuilder {

    @Autowired
    private AppConfig config;

    @Autowired
    private DbMessageRecord dbMessageRecord;

    public String buildExecutionStartedMail(PipelineExecution execution, Schedule schedule) {
        StringBuilder body = new StringBuilder();

        addCommonMailBody(body, execution, schedule);

        addExecutionLink(body, execution, schedule);

        return body.toString();
    }

    public String buildExecutionFinishedMail(PipelineExecution execution, Schedule schedule) {
        StringBuilder body = new StringBuilder();

        addCommonMailBody(body, execution, schedule);

        body.append(Messages.getString("InstantReportEmailBuilder.execution.ends", EmailUtils.formatDate(execution.getEnd())));
        body.append("<br/>");
        body.append(Messages.getString("InstantReportEmailBuilder.execution.result", EmailUtils.getStatusAsString(execution.getStatus())));
        body.append("<br/>");

        addExecutionLink(body, execution, schedule);

        body.append("<br/><br/>");
        // append messages
        final List<MessageRecord> messages = getMessagesForExecution(execution);
        body.append("<b>");
        body.append(Messages.getString("InstantReportEmailBuilder.published.messages"));
        body.append("</b> <br/>");
        body.append("<table border=2 cellpadding=2 >");
        body.append("<tr bgcolor=\"#C0C0C0\">");
        body.append(Messages.getString("InstantReportEmailBuilder.published.table"));
        body.append("</tr>");

        for (MessageRecord message : messages) {
            // set color based on type
            switch (message.getType()) {
                case DPU_DEBUG:
                    body.append("<tr>");
                    break;
                case DPU_ERROR:
                case PIPELINE_ERROR:
                    body.append("<tr bgcolor=\"#FFE0E0\">");
                    break;
                case DPU_INFO:
                case PIPELINE_INFO:
                    body.append("<tr bgcolor=\"#E0E0FF\">");
                    break;
                case DPU_WARNING:
                    body.append("<tr bgcolor=\"#FFFFA0\">");
                    break;
            }

            // name
            body.append("<td>");
            if (message.getDpuInstance() != null) {
                body.append(message.getDpuInstance().getName());
            }
            body.append("</td>");
            // time
            body.append("<td>");
            body.append(EmailUtils.formatDate(message.getTime()));
            body.append("</td>");
            // type
            body.append("<td>");
            body.append(EmailUtils.getMessageTypeAsString(message.getType()));
            body.append("</td>");
            // short message			
            body.append("<td>");
            body.append(message.getShortMessage());
            body.append("</td>");
            // ...
            body.append("</tr>");
        }
        body.append("</table>");

        return body.toString();
    }

    private void addCommonMailBody(StringBuilder body, PipelineExecution execution, Schedule schedule) {
        try {
            final String name = config.getString(ConfigProperty.BACKEND_NAME);
            body.append("<p>");
            body.append(Messages.getString("InstantReportEmailBuilder.instance", name));
            body.append("</p><br/>");
        } catch (MissingConfigPropertyException e) {
            // no name is presented
        }

        body.append(Messages.getString("InstantReportEmailBuilder.report", execution.getPipeline().getName()));
        body.append("<br/>");
        body.append(Messages.getString("InstantReportEmailBuilder.execution", execution.getId().toString()));
        body.append("<br/>");
        body.append(Messages.getString("InstantReportEmailBuilder.execution.owner", EmailUtils.getDisplayedExecutionOwner(execution)));
        body.append("<br/>");
        body.append(Messages.getString("InstantReportEmailBuilder.execution.starts", EmailUtils.formatDate(execution.getStart())));
        body.append("<br/>");
    }

    private void addExecutionLink(StringBuilder body, PipelineExecution execution, Schedule schedule) {
        // add link to the execution detail if the url is specified
        try {
            String urlBase = this.config.getString(ConfigProperty.FRONTEND_URL);
            if (!urlBase.endsWith("/")) {
                urlBase = urlBase + "/";
            }
            urlBase = urlBase + "#!ExecutionList/exec=" + execution.getId().toString();
            // generate link
            body.append("<br/>");
            body.append("<a href=/");
            body.append(urlBase);
            body.append("\" >");
            body.append(Messages.getString("InstantReportEmailBuilder.execution.detail"));
            body.append("<a/> ");
        } catch (MissingConfigPropertyException e) {
            // no name is presented
        }
    }

    private List<MessageRecord> getMessagesForExecution(PipelineExecution execution) {
        DbQueryBuilder<MessageRecord> query = this.dbMessageRecord.createQueryBuilder();
        query.addFilter(Compare.equal("execution", execution));
        query.sort("id", true);

        return this.dbMessageRecord.executeList(query.getQuery());
    }

}

package cz.cuni.mff.xrg.odcs.backend.report;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineFinished;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineStarted;
import cz.cuni.mff.xrg.odcs.commons.app.communication.EmailSender;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.NotificationRecord;

/**
 * Listen to the application event and if need send email about the event to the
 * user.
 * 
 * @author Petyr
 */
@Component
class InstantReport implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOG = LoggerFactory
            .getLogger(InstantReport.class);

    /**
     * Provide functionality to send email.
     */
    @Autowired
    private EmailSender emailSender;

    @Autowired
    private InstantReportEmailBuilder emailBuilder;

    /**
     * Add email based on {@link ScheduleNotificationRecord}.
     * 
     * @param emails
     * @param execution
     * @param notification
     *            Notification settings.
     * @param emailsToAdd
     */
    private static void add(ApplicationEvent event, Set<String> emails,
            PipelineExecution execution,
            NotificationRecord notification,
            Set<EmailAddress> emailsToAdd) {
        if (event instanceof PipelineFinished) {
            switch (execution.getStatus()) {
                case FINISHED_SUCCESS:
                case FINISHED_WARNING:
                    switch (notification.getTypeSuccess()) {
                        case INSTANT: // add email
                            for (EmailAddress email : emailsToAdd) {
                                emails.add(email.toString());
                            }
                            return;
                        case DAILY:
                            // we do not send now ... will be send in daily report
                        case NO_REPORT:
                            return;
                    }
                    break;
                case FAILED:
                case CANCELLED:
                    switch (notification.getTypeError()) {
                        case INSTANT: // add email
                            for (EmailAddress email : emailsToAdd) {
                                emails.add(email.toString());
                            }
                            return;
                        case DAILY:
                            // we do not send now ... will be send in daily report
                        case NO_REPORT:
                            return;
                    }
                    break;
                default:
                    LOG.warn("Unexpected execution status '{}' ", execution.getStatus());
                    return;
            }

        } else if (event instanceof PipelineStarted) {
            switch (notification.getTypeStarted()) {
                case INSTANT:
                    for (EmailAddress email : emailsToAdd) {
                        emails.add(email.toString());
                    }
                    return;
                case DAILY:
                case NO_REPORT:
                    return;
            }

        }
    }

    /**
     * Prepare subject for email that inform about single execution.
     * 
     * @param execution
     * @param schedule
     * @return
     */
    private static String subjectInstantStarted(PipelineExecution execution, Schedule schedule) {
        return Messages.getString("InstantReport.execution.started.report", execution.getPipeline().getName(),
                EmailUtils.formatDate(execution.getStart()));
    }

    /**
     * Prepare subject for email that inform about single execution.
     * 
     * @param execution
     * @param schedule
     * @return
     */
    private static String subjectInstantEnded(PipelineExecution execution, Schedule schedule) {
        return Messages.getString("InstantReport.execution.ended.report", execution.getPipeline().getName(),
                EmailUtils.formatDate(execution.getStart()));
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof PipelineFinished || event instanceof PipelineStarted) {
            try {
                PipelineExecution execution = null;
                if (event instanceof PipelineFinished) {
                    execution = ((PipelineFinished) event).getExecution();
                } else if (event instanceof PipelineStarted) {
                    execution = ((PipelineStarted) event).getExecution();
                }
                Schedule schedule = execution.getSchedule();

                // create list with recipients
                Set<String> emails = new HashSet<>();

                // check if send mail also for non scheduled executions
                if (schedule == null) {
                    if (execution.getOwner() != null && execution.getOwner().getNotification() != null) {
                        if (execution.getOwner().getNotification().isReportNotScheduled()) {
                            add(event, emails, execution, execution.getOwner().getNotification(),
                                    execution.getOwner().getNotification().getEmails());
                        }
                    }
                } else { // email notifications for scheduled executions
                    // use Schedule notification if available
                    // otherwise use setting from user
                    if (schedule.getNotification() == null) {
                        if (schedule.getOwner() == null) {
                            // there is no owner to use to send email .. 
                            LOG.warn("Missing owner for schedule id: {}", schedule.getId());
                            return;
                        }

                        if (schedule.getOwner().getNotification() == null) {
                            // there is no rule to use to send email .. 
                            LOG.warn("Missing notificaiton rule for schedule id: {}", schedule.getId());
                            return;
                        }

                        add(event, emails, execution, schedule.getOwner().getNotification(),
                                schedule.getOwner().getNotification().getEmails());
                    } else {
                        LOG.debug("Using schedule's settings for email");
                        add(event, emails, execution, schedule.getNotification(),
                                schedule.getNotification().getEmails());
                    }
                }

                if (emails.isEmpty()) {
                    // no one to send the email
                    LOG.debug("There is no addres to which send email for execution id: {}",
                            execution.getId());
                    return;
                }

                String subject = null;
                String body = null;
                if (event instanceof PipelineFinished) {
                    subject = subjectInstantEnded(execution, schedule);
                    body = this.emailBuilder.buildExecutionFinishedMail(execution, schedule);
                } else if (event instanceof PipelineStarted) {
                    subject = subjectInstantStarted(execution, schedule);
                    body = this.emailBuilder.buildExecutionStartedMail(execution, schedule);
                }

                // create list of recipients
                ArrayList<String> recipients = new ArrayList<>();
                recipients.addAll(emails);

                for (String email : emails) {
                    LOG.debug("Sending email for schedule {} "
                            , email);
                }

                this.emailSender.send(subject, body, recipients);
            } catch (Exception e) {
                LOG.error("Failed to send instant email report", e);
            }
        }
    }

}

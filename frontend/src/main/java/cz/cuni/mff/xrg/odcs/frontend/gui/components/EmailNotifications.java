package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import cz.cuni.mff.xrg.odcs.commons.app.user.NotificationRecordType;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserNotificationRecord;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Builds layout with GUI components for settings notifications about scheduled
 * events and their runs. Used in User Settings dialog and in Schedule a
 * pipeline dialog.
 * 
 * @author Maria Kukhar
 */
public class EmailNotifications {

    private int noSuccessful = 0;

    private int noError = 0;

    private int noStarted = 0;

//	public EmailComponent shEmail;
    private OptionGroup errorExec;

    private OptionGroup successfulExec;

    private OptionGroup startedExec;

    private CheckBox nonScheduledExecutionReports;

    /**
     * Parent component.
     */
    public SchedulePipeline parentComponentSh;

    /**
     * Alternative parent component.
     */
    public cz.cuni.mff.xrg.odcs.frontend.gui.views.Settings parentComponentUs;

    /**
     * Build layout.
     * 
     * @return built layout
     */
    public VerticalLayout buildEmailNotificationsLayout() {

        VerticalLayout emailNotificationsLayout = new VerticalLayout();
        emailNotificationsLayout.setMargin(true);
        emailNotificationsLayout.setSpacing(true);
        emailNotificationsLayout.setImmediate(true);

        GridLayout notifycationLayout = new GridLayout(3, 3);
        notifycationLayout.setSpacing(true);

        notifycationLayout.addComponent(new Label(Messages.getString("EmailNotifications.started")), 0, 0);
        this.startedExec = new OptionGroup();
        this.startedExec.setImmediate(true);
        this.startedExec.addItem(NotificationRecordType.INSTANT);
        this.startedExec.addItem(NotificationRecordType.DAILY);
        this.startedExec.addItem(NotificationRecordType.NO_REPORT);
        this.startedExec.setValue(NotificationRecordType.NO_REPORT);
        this.startedExec.setItemCaption(NotificationRecordType.INSTANT, Messages.getString("EmailNotifications.instant"));
        this.startedExec.setItemCaption(NotificationRecordType.DAILY, Messages.getString("EmailNotifications.bulk.report"));
        this.startedExec.setItemCaption(NotificationRecordType.NO_REPORT, Messages.getString("EmailNotifications.no.report.default"));
        this.startedExec.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (parentComponentUs != null && parentComponentUs.buttonNotificationBar != null) {
                    parentComponentUs.buttonNotificationBar.setEnabled(true);
                }

                if (event.getProperty().getValue().equals(NotificationRecordType.NO_REPORT)) {
                    noStarted = 1;
                    if ((noStarted == 1) && (noError == 1) && (noSuccessful == 1)) {
                        if (parentComponentSh != null) {
                            parentComponentSh.getEmailLayout().setEnabled(false);
                        }
                    }

                } else {
                    noStarted = 0;
                    if (parentComponentSh != null) {
                        parentComponentSh.getEmailLayout().setEnabled(true);
                    }

                }

            }
        });
        notifycationLayout.addComponent(this.startedExec, 1, 0);

        notifycationLayout.addComponent(new Label(Messages.getString("EmailNotifications.successful")), 0, 1);
        successfulExec = new OptionGroup();
        successfulExec.setImmediate(true);
        successfulExec.addItem(NotificationRecordType.INSTANT);
        successfulExec.addItem(NotificationRecordType.DAILY);
        successfulExec.addItem(NotificationRecordType.NO_REPORT);
        successfulExec.setValue(NotificationRecordType.DAILY);
        successfulExec.setItemCaption(NotificationRecordType.INSTANT, Messages.getString("EmailNotifications.instant"));
        successfulExec.setItemCaption(NotificationRecordType.DAILY, Messages.getString("EmailNotifications.bulk.report.default"));
        successfulExec.setItemCaption(NotificationRecordType.NO_REPORT, Messages.getString("EmailNotifications.no.report"));

        successfulExec.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (parentComponentUs != null && parentComponentUs.buttonNotificationBar != null) {
                    parentComponentUs.buttonNotificationBar.setEnabled(true);
                }

                if (event.getProperty().getValue().equals(NotificationRecordType.NO_REPORT)) {
                    noSuccessful = 1;
                    if ((noError == 1) && (noSuccessful == 1)) {
                        if (parentComponentSh != null) {
                            parentComponentSh.getEmailLayout().setEnabled(false);
                        }
//						if(parentComponentUs!=null)	
//							parentComponentUs.emailLayout.setEnabled(false);
                    }

                } else {
                    noSuccessful = 0;
                    if (parentComponentSh != null) {
                        parentComponentSh.getEmailLayout().setEnabled(true);
                    }
//					if(parentComponentUs!=null)	
//						parentComponentUs.emailLayout.setEnabled(true);

                }

            }
        });
        notifycationLayout.addComponent(successfulExec, 1, 1);

        notifycationLayout.addComponent(new Label(Messages.getString("EmailNotifications.error")), 0, 2);
        errorExec = new OptionGroup();
        errorExec.setImmediate(true);
        errorExec.setImmediate(true);
        errorExec.addItem(NotificationRecordType.INSTANT);
        errorExec.addItem(NotificationRecordType.DAILY);
        errorExec.addItem(NotificationRecordType.NO_REPORT);
        errorExec.setValue(NotificationRecordType.INSTANT);
        errorExec.setItemCaption(NotificationRecordType.INSTANT, Messages.getString("EmailNotifications.instant.default"));
        errorExec.setItemCaption(NotificationRecordType.DAILY, Messages.getString("EmailNotifications.bulk.report"));
        errorExec.setItemCaption(NotificationRecordType.NO_REPORT, Messages.getString("EmailNotifications.no.report"));

        errorExec.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (parentComponentUs != null && parentComponentUs.buttonNotificationBar != null) {
                    parentComponentUs.buttonNotificationBar.setEnabled(true);
                }

                if (event.getProperty().getValue().equals(NotificationRecordType.NO_REPORT)) {
                    noError = 1;

                    if ((noError == 1) && (noSuccessful == 1)) {
                        if (parentComponentSh != null) {
                            parentComponentSh.getEmailLayout().setEnabled(false);
                        }
//						if(parentComponentUs!=null)	
//							parentComponentUs.emailLayout.setEnabled(false);
                    }
                } else {
                    noError = 0;
                    if (parentComponentSh != null) {
                        parentComponentSh.getEmailLayout().setEnabled(true);
                    }
//					if(parentComponentUs!=null)	
//						parentComponentUs.emailLayout.setEnabled(true);

                }

            }
        });

        notifycationLayout.addComponent(errorExec, 1, 2);
        emailNotificationsLayout.addComponent(notifycationLayout);

        if (this.parentComponentUs != null) {
            this.nonScheduledExecutionReports = new CheckBox();
            this.nonScheduledExecutionReports.setCaption(Messages.getString("EmailNotifications.manual"));
            this.nonScheduledExecutionReports.addValueChangeListener(new ValueChangeListener() {

                private static final long serialVersionUID = 1L;

                @SuppressWarnings("unqualified-field-access")
                @Override
                public void valueChange(ValueChangeEvent event) {
                    if (parentComponentUs.buttonNotificationBar != null) {
                        parentComponentUs.buttonNotificationBar.setEnabled(true);
                    }
                }
            });
            emailNotificationsLayout.addComponent(this.nonScheduledExecutionReports);

        }

        return emailNotificationsLayout;
    }

    /**
     * Set notification record.
     * 
     * @param notification
     */
    public void setUserNotificatonRecord(UserNotificationRecord notification) {
        notification.setTypeStarted((NotificationRecordType) this.startedExec.getValue());
        notification.setTypeError((NotificationRecordType) errorExec.getValue());
        notification.setTypeSuccess((NotificationRecordType) successfulExec.getValue());
        notification.setReportNotScheduled(this.nonScheduledExecutionReports.getValue());
    }

    /**
     * Set notification record for schedule.
     * 
     * @param notification
     * @param schedule
     */
    public void setScheduleNotificationRecord(ScheduleNotificationRecord notification, Schedule schedule) {

        notification.setSchedule(schedule);
        notification.setTypeStarted((NotificationRecordType) this.startedExec.getValue());
        notification.setTypeError((NotificationRecordType) errorExec.getValue());
        notification.setTypeSuccess((NotificationRecordType) successfulExec.getValue());

    }

    /**
     * Gets {@link ScheduleNotificationRecord} for given schedule.
     * 
     * @param schedule
     *            Schedule which record to retrieve.
     */
    public void getScheduleNotificationRecord(Schedule schedule) {

        ScheduleNotificationRecord notification = schedule.getNotification();

        if (notification != null) {
            this.startedExec.setValue(notification.getTypeStarted());
            this.errorExec.setValue(notification.getTypeError());
            this.successfulExec.setValue(notification.getTypeSuccess());
        }

    }

    /**
     * Get notification record for user.
     * 
     * @param user
     */
    public void getUserNotificationRecord(User user) {

        UserNotificationRecord notification = user.getNotification();

        if (notification != null) {
            this.startedExec.setValue(notification.getTypeStarted());
            this.errorExec.setValue(notification.getTypeError());
            this.successfulExec.setValue(notification.getTypeSuccess());
            this.nonScheduledExecutionReports.setValue(notification.isReportNotScheduled());

        }

    }

    /**
     * Get default notification record for schedules.
     */
    public void getDefaultScheduleNotificationRecord() {
        this.startedExec.setValue(NotificationRecordType.NO_REPORT);
        this.errorExec.setValue(NotificationRecordType.INSTANT);
        this.successfulExec.setValue(NotificationRecordType.DAILY);
        if (this.nonScheduledExecutionReports != null) {
            this.nonScheduledExecutionReports.setValue(false);
        }

    }

    /**
     * Disable components.
     */
    public void setDisableComponents() {
        startedExec.setEnabled(false);
        successfulExec.setEnabled(false);
        errorExec.setEnabled(false);
        parentComponentSh.getEmailLayout().setEnabled(false);
//			shEmailLayout.setEnabled(false);

    }

    /**
     * Enables components in email notifications.
     */
    public void setEnableComponents() {
        startedExec.setEnabled(true);
        successfulExec.setEnabled(true);
        errorExec.setEnabled(true);
        parentComponentSh.getEmailLayout().setEnabled(true);
//			shEmailLayout.setEnabled(true);

    }
}

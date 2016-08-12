package cz.cuni.mff.xrg.odcs.commons.app.scheduling;

import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.NotificationRecord;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Notification settings for a single pipeline execution schedule.
 * 
 * @author Jan Vojt
 */
@Entity
@Table(name = "sch_sch_notification")
public class ScheduleNotificationRecord extends NotificationRecord {

    /**
     * Scheduler to notify (notification about particular scheduled
     * pipeline execution). Applicable if notification rule set in
     * Scheduler dialog.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, unique = true)
    private Schedule schedule;

    /**
     * E-mails the notification will be sent to.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "sch_sch_notification_email",
            joinColumns = @JoinColumn(name = "notification_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "email_id", referencedColumnName = "id"))
    private Set<EmailAddress> emails = new HashSet<>();

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * @return defensive copy of a set of emails to send notification to
     */
    @Override
    public Set<EmailAddress> getEmails() {
        return new HashSet<>(emails);
    }

    @Override
    public void setEmails(Set<EmailAddress> emails) {
        this.emails = new HashSet<>(emails);
    }

    @Override
    public void addEmail(EmailAddress email) {
        this.emails.add(email);
    }

    @Override
    public void removeEmail(EmailAddress email) {
        this.emails.remove(email);
    }

}

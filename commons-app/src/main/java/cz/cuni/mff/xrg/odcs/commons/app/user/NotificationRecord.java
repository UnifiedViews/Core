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
package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * Represent settings for scheduler notification.
 * 
 * @author Maria Kukhar
 */
@MappedSuperclass
public abstract class NotificationRecord implements DataObject {

    /**
     * Unique ID for each scheduler notification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sch_notification")
    @SequenceGenerator(name = "seq_sch_notification", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Type of notification in case of successful execution:
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type_success", columnDefinition = "SMALLINT")
    private NotificationRecordType typeSuccess;

    /**
     * Type of notification in case of error execution:
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type_error", columnDefinition = "SMALLINT")
    private NotificationRecordType typeError;

    /**
     * Type of notification in case of started execution
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type_started", columnDefinition = "SMALLINT")
    private NotificationRecordType typeStarted;

    /**
     * Empty constructor. Used by JPA. Do not use otherwise.
     */
    public NotificationRecord() {
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return defensive copy of a set of emails to send notification to
     */
    public abstract Set<EmailAddress> getEmails();

    public abstract void setEmails(Set<EmailAddress> emails);

    public abstract void addEmail(EmailAddress email);

    public abstract void removeEmail(EmailAddress email);

    public NotificationRecordType getTypeSuccess() {
        return typeSuccess;
    }

    public void setTypeSuccess(NotificationRecordType typeSuccess) {
        this.typeSuccess = typeSuccess;
    }

    public NotificationRecordType getTypeError() {
        return typeError;
    }

    public void setTypeError(NotificationRecordType typeError) {
        this.typeError = typeError;
    }

    public NotificationRecordType getTypeStarted() {
        return this.typeStarted;
    }

    public void setTypeStarted(NotificationRecordType typeStarted) {
        this.typeStarted = typeStarted;
    }

    /**
     * Returns true if two objects represent the same pipeline. This holds if
     * and only if <code>this.id == null ? this == obj : this.id == o.id</code>.
     * 
     * @param obj
     * @return true if both objects represent the same pipeline
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final DataObject other = (DataObject) obj;
        if (this.getId() == null) {
            return super.equals(other);
        }

        return Objects.equals(this.getId(), other.getId());
    }

    /**
     * Hashcode is compatible with {@link #equals(java.lang.Object)}.
     * 
     * @return The value of hashcode.
     */
    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return super.hashCode();
        }
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        return hash;
    }

}

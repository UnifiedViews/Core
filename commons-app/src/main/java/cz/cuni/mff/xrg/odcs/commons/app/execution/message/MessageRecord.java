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
package cz.cuni.mff.xrg.odcs.commons.app.execution.message;

import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

/**
 * Represent a single message created during DPURecord execution.
 * 
 * @author Petyr
 * @author Bogo
 */
@Entity
@Table(name = "exec_record")
@Index(name = "ix_EXEC_RECORD", columnNames = "r_time, r_type, dpu_id, execution_id")
public class MessageRecord implements DataObject {

    /**
     * Unique id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_exec_record")
    @SequenceGenerator(name = "seq_exec_record", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Time of creation.
     */
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "r_time")
    private Date time;

    /**
     * Type of record.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "r_type")
    private MessageRecordType type;

    /**
     * DPURecord which emitted the message.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "dpu_id")
    private DPUInstanceRecord dpuInstance;

    /**
     * Pipeline execution during which message was emitted.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id")
    private PipelineExecution execution;

    /**
     * Short message, should be under 50 characters.
     */
    @Column(name = "short_message", length = 128)
    private String shortMessage;

    /**
     * Full message text.
     */
    @Lob
    @Column(name = "full_message", columnDefinition = "TEXT")
    private String fullMessage;

    /**
     * No-arg constructor for JPA. Do not use!
     */
    public MessageRecord() {
    }

    /**
     * Constructor.
     * 
     * @param time
     *            Time of creation.
     * @param type
     *            Message type.
     * @param dpuInstance
     *            ID of source DPU or null.
     * @param execution
     *            ID of execution that produce the message.
     * @param shortMessage
     *            Short message.
     * @param fullMessage
     *            Long message.
     */
    public MessageRecord(Date time,
            MessageRecordType type,
            DPUInstanceRecord dpuInstance,
            PipelineExecution execution,
            String shortMessage,
            String fullMessage) {
        this.time = time;
        this.type = type;
        this.execution = execution;
        this.shortMessage = StringUtils.abbreviate(shortMessage, LenghtLimits.SHORT_MESSAGE);
        this.fullMessage = fullMessage;
        this.dpuInstance = dpuInstance;

        if (dpuInstance != null)
            dpuInstance.getMessageRecords().add(this);
        if (execution != null)
            execution.getMessages().add(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return Time of creation.
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return Type of message.
     */
    public MessageRecordType getType() {
        return type;
    }

    /**
     * @return ID of source DPU or null.
     */
    public DPUInstanceRecord getDpuInstance() {
        return dpuInstance;
    }

    /**
     * @return ID of execution during which the message has been created.
     */
    public PipelineExecution getExecution() {
        return execution;
    }

    /**
     * @return Short message.
     */
    public String getShortMessage() {
        return StringUtils.defaultString(shortMessage);
    }

    /**
     * @return Long message.
     */
    public String getFullMessage() {
        return StringUtils.defaultString(fullMessage);
    }

    /**
     * @return Time of creation.
     */
    public Timestamp getTimestamp() {
        return new Timestamp(time.getTime());
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

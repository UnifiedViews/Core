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
package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.Index;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents log message loaded from database.
 * 
 * @author Petyr
 */
@Entity
@Table(name = "logging")
@Index(name = "ix_LOGGING", columnNames = "dpu, execution, relative_id")
public class Log implements DataObject {

    /**
     * Log property name for logging messages produced by {@link PipelineExecution}.
     */
    public static final String MDC_EXECUTION_KEY_NAME = "execution";

    /**
     * Log property name for logging messages produced by {@link DPUInstanceRecord}. Such logs usually contain a {@link #MDC_EXECUTION_KEY_NAME} as well.
     */
    public static final String MDC_DPU_INSTANCE_KEY_NAME = "dpuInstance";

    /**
     * Primary key of message stored in database.
     * The naming of the sequence generator "tablename_columname_seq" is the PostgreSQL default
     * sequence naming for SERIAL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logging_id_seq")
    @SequenceGenerator(name = "logging_id_seq", sequenceName = "logging_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Level as string, so it can be persisted in DB.
     */
    @Column(name = "log_level", nullable = false)
    private Integer logLevel;

    /**
     * Timestamp of log message.
     */
    @Column(name = "timestmp", nullable = false)
    private Long timestamp;

    /**
     * Source class of log message.
     */
    @Column(name = "logger", nullable = false)
    private String source;

    /**
     * Text of formatted log massage.
     */
    @Lob
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Id of given DPU.
     */
    @Column(name = "dpu")
    private Long dpu;

    /**
     * Id of execution.
     */
    @Column(name = "execution", nullable = false)
    private Long execution;

    /**
     * Mapping to stack trace.
     */
    @Lob
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * Id (position) relative to a given execution. The id is given in same
     * order as the logs are generated. Starts from one.
     * Name of this property is used in {@link DbLogReadImpl}.
     */
    @Column(name = "relative_id")
    private Long relativeId;

    public Log() {
    }

    /**
     * @return Log's id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return Log's level as integer.
     */
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * @return Time of log creation.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * @return Source logger ie. class.
     */
    public String getSource() {
        return source;
    }

    /**
     * @return Message.
     */
    public String getMessage() {
        return StringUtils.defaultString(message);
    }

    /**
     * @return ID of DPU instance/template during whose execution has the log
     *         been created.
     */
    public Long getDpu() {
        return dpu;
    }

    /**
     * @return ID of execution during which the log has been created.
     */
    public Long getExecution() {
        return execution;
    }

    /**
     * Stack trace for given log if exist.
     * 
     * @return Empty string or stack trace. Never return null!
     */
    public String getStackTrace() {
        return StringUtils.defaultString(stackTrace);
    }

    /**
     * @return Relative ID ie. number of the log in respect to given execution.
     */
    public Long getRelativeId() {
        return relativeId;
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

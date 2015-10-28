package cz.cuni.mff.xrg.odcs.commons.app.execution.log;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Objects;

/**
 * Represents log message loaded from database.
 *
 * @author Petyr
 */
@Entity
@Table(name = "logging")
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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_logging")
    @SequenceGenerator(name = "seq_logging", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Level as string, so it can be persisted in DB.
     */
    @Column(name = "log_level")
    private Integer logLevel;

    /**
     * Timestamp of log message.
     */
    @Column(name = "timestmp")
    private Long timestamp;

    /**
     * Source class of log message.
     */
    @Column(name = "logger")
    private String source;

    /**
     * Text of formatted log massage.
     */
    @Column(name = "message")
    private String message;

    /**
     * Id of given DPU.
     */
    @JoinColumn(name = "dpu")
    private Long dpu;

    /**
     * Id of execution.
     */
    @JoinColumn(name = "execution")
    private Long execution;

    /**
     * Mapping to stack trace.
     */
    @Column(name = "stack_trace")
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
    public int getId() {
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

package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a single event of opening pipeline detail in canvas by given owner
 * at given time.
 *
 * @author Jan Vojt
 */
@Entity
@Table(name = "ppl_open_event")
public class OpenEvent implements DataObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ppl_open_event")
    @SequenceGenerator(name = "seq_ppl_open_event", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Pipeline that was open.
     */
    @ManyToOne
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;

    /**
     * User who opened the pipeline detail.
     * <p>
     * Field is named &quot;owner&quot; just because &quot;user&quot; is SQL-99 reserved keyword.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    /**
     * Timestamp when the event occurred.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "opened", nullable = false)
    private Date timestamp;

    @Override
    public Long getId() {
        return id;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;

        if (pipeline != null) pipeline.getOpenEvents().add(this);
    }

    public User getUser() {
        return owner;
    }

    public void setUser(User user) {
        this.owner = user;

        if (user != null) user.getOpenEvents().add(this);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Hashcode is compatible with {@link #equals(java.lang.Object)}.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        if (this.id == null) {
            return super.hashCode();
        }
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.pipeline);
        hash = 97 * hash + Objects.hashCode(this.owner);
        return hash;
    }

    /**
     * Returns true if two objects represent the same event. This holds if
     * and only if <code>this.id == null ? this == obj : this.id == o.id</code>.
     *
     * @param o
     * @return true if both objects represent the same event
     */
    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final OpenEvent other = (OpenEvent) o;
        if (this.id == null) {
            return super.equals(other);
        }

        return Objects.equals(this.id, other.id);
    }

}

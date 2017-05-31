package cz.cuni.mff.xrg.odcs.commons.app.execution.server;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "backend_servers")
public class ExecutionServer implements Serializable, DataObject {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_backend_servers")
    @SequenceGenerator(name = "seq_backend_servers", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    // TIMESTAMP won't work with MS SQL Server in this case, hence has to be
    // changed to DATETIME (columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date lastUpdate;

    @Column(name = "backend_id", unique = true, length = 128)
    private String backendId;

    @Override
    public Long getId() {
        return this.id;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getBackendId() {
        return this.backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public void setId(Long id) {
        this.id = id;
    }

}

package cz.cuni.mff.xrg.odcs.commons.app.pipeline;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.auth.SharedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.user.Organization;
import cz.cuni.mff.xrg.odcs.commons.app.user.OrganizationSharedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.OwnedEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Represents a fixed workflow composed of one or several extractor, transformer
 * and loader modules ({@link DPUInstanceRecord}s) organized in acyclic graph.
 *
 * @author Jiri Tomes
 * @author Jan Vojt
 * @author Bogo
 */
@XmlRootElement
@Entity
@Table(name = "ppl_model")
public class Pipeline implements OwnedEntity, SharedEntity, OrganizationSharedEntity, Serializable, DataObject {

    /**
     * Unique ID for each pipeline
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ppl_model")
    @SequenceGenerator(name = "seq_ppl_model", allocationSize = 1)
    private Long id;

    /**
     * Human-readable pipeline name
     */
    @Column
    private String name;

    /**
     * Human-readable pipeline description
     */
    @Column
    private String description;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "pipeline", fetch = FetchType.LAZY)
    private PipelineGraph graph;

    /**
     * User who created and owns this pipeline.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    /**
     * Public vs private shareType.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "visibility")
    private ShareType shareType;

    /**
     * List pipelines that must not run in order to run this pipeline.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ppl_ppl_conflicts",
            joinColumns =
            @JoinColumn(name = "pipeline_id", referencedColumnName = "id"),
            inverseJoinColumns =
            @JoinColumn(name = "pipeline_conflict_id", referencedColumnName = "id"))
    private Set<Pipeline> conflicts = new HashSet<>();

    /**
     * Timestamp when was the last time someone made changes to this pipeline.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_change")
    private Date lastChange;

    /**
     * Default constructor for JPA
     */
    public Pipeline() {
        graph = new PipelineGraph();
        graph.setPipeline(this);
    }

    /**
     * Copy constructor. Creates a deep copy of given pipeline.
     *
     * @param pipeline
     *            to copy
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Pipeline(Pipeline pipeline) {

        // primitive properties
        name = pipeline.name;
        description = pipeline.description;

        // graph
        graph = new PipelineGraph(pipeline.getGraph());
        graph.setPipeline(this);

        // conflicts
        conflicts = new HashSet<>(pipeline.conflicts);
    }

    /**
     * Constructor with given pipeline name and description.
     *
     * @param name
     *            Name of pipeline
     * @param description
     *            String value of pipeline description
     */
    public Pipeline(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the pipeline.
     *
     * @return the name of the pipeline.
     */
    public String getName() {
        return name;
    }

    /**
     * Set new name to the pipeline.
     *
     * @param newName
     *            String value of new name of pipeline.
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Returns the pipeline description.
     *
     * @return the value of the pipeline description.
     */
    public String getDescription() {
        return StringUtils.defaultString(description);
    }

    /**
     * Set new value of the pipeline description.
     *
     * @param newDescription
     *            new value of the pipeline description.
     */
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    /**
     * Returns the instance of {@link PipelineGraph} for this pipeline.
     *
     * @return the instance of {@link PipelineGraph} for this pipeline.
     */
    public PipelineGraph getGraph() {
        return graph;
    }

    /**
     * Set new value of {@link PipelineGraph} to this pipeline.
     *
     * @param graph
     *            instance of {@link PipelineGraph} will be set to this
     *            pipeline.
     */
    public void setGraph(PipelineGraph graph) {
        this.graph = graph;
        graph.setPipeline(this);
    }

    /**
     * Returns the set ID of this pipeline as {@link Long} value.
     *
     * @return the set ID of this pipeline as {@link Long} value.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set new value of pipeline owner.
     *
     * @param owner
     *            instance of {@link User} as new pipeline owner.
     */
    public void setUser(User owner) {
        this.owner = owner;
    }

    /**
     * Return the instance of {@link User} as owner of this pipeline.
     *
     * @return the owner of this pipeline.
     */
    @Override
    public User getOwner() {
        return owner;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Returns the set of pipeline conflicts.
     *
     * @return the set of pipeline conflicts.
     */
    public Set<Pipeline> getConflicts() {
        return conflicts;
    }

    /**
     * Returns the pipeline visibility as one of {@link ShareType} values set
     * for this pipeline.
     *
     * @return the pipeline visibility as one of {@link ShareType} values set
     *         for this pipeline.
     */
    @Override
    public ShareType getShareType() {
        return shareType;
    }

    /**
     * Set the pipeline {@link ShareType} values.
     *
     * @param shareType
     *            new value
     */
    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    /**
     * Returns the {@link Date} instance where the pipeline was last changed.
     *
     * @return {@link Date} where the pipeline was last changed.
     */
    public Date getLastChange() {
        return lastChange;
    }

    /**
     * Set the {@link Date} instance where the pipeline was last changed to this
     * pipeline.
     *
     * @param lastChange
     *            value of {@link Date} that will be set.
     */
    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
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

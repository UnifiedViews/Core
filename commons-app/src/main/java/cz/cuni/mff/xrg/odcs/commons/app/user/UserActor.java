package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_actor")
public class UserActor implements DataObject {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_actor")
    @SequenceGenerator(name = "seq_user_actor", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_extuser", unique = true, nullable = false)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "actor", orphanRemoval = true)
    private Set<Schedule> schedules = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "actor", orphanRemoval = true)
    private Set<Pipeline> pipelines = new HashSet<>();

    @Override
    public Long getId() {
        return this.id;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        this.schedules = schedules;
    }

    public Set<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(Set<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

}

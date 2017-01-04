package cz.cuni.mff.xrg.odcs.commons.app.user;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permission")
public class Permission implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key for entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_permission")
    @SequenceGenerator(name = "seq_permission", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "permissions")
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(name = "name", nullable = false, unique = true, length = 142)
    private String name;

    @Column(name = "sharedentityinstancewriterequired")
    private boolean sharedEntityInstanceWriteRequired;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSharedEntityInstanceWriteRequired() {
        return this.sharedEntityInstanceWriteRequired;
    }

    public void setSharedEntityInstanceWriteRequired(boolean sharedEntityInstanceWriteRequired) {
        this.sharedEntityInstanceWriteRequired = sharedEntityInstanceWriteRequired;
    }

    @Override
    public String getAuthority() {
        return name;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}

package cz.cuni.mff.xrg.odcs.commons.app.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

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
    private Long id;

    @Column
    private String name;

    @Column
    private boolean rwOnly;

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

    public boolean isRwOnly() {
        return rwOnly;
    }

    public void setRwOnly(boolean rwOnly) {
        this.rwOnly = rwOnly;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}

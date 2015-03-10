package cz.cuni.mff.xrg.odcs.commons.app.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;

/**
 * Holds user data (his account).
 *
 * @author Jiri Tomes
 */
@Entity
@Table(name = "usr_user")
@SecondaryTable(name = "usr_extuser", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id_usr", referencedColumnName = "id"))
public class User implements UserDetails, OwnedEntity, DataObject {

    /**
     * Primary key for entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usr_user")
    @SequenceGenerator(name = "seq_usr_user", allocationSize = 1)
    private Long id;

    /**
     * User name used for login and as a unique identification of User.
     */
    @Column
    private String username;

    /**
     * User email.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id")
    private EmailAddress email;

    /**
     * Full name.
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * Hashed password.
     */
    @Column(name = "u_password")
    private String password;

    @Column(name = "table_rows")
    private Integer tableRows;

    /**
     * User roles representing sets of privileges.
     */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usr_user_role", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") })
    private Set<RoleEntity> roles = new HashSet<>();

    /**
     * User notification settings used as a default for execution schedules.
     * Overridden by specific settings in {@link ScheduleNotificationRecord}.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserNotificationRecord notification;

    @Column(table = "usr_extuser", name = "id_extuser")
    private String externalIdentifier;

    @Transient
    private Organization organization;

    /**
     * Empty constructor required by JPA.
     */
    public User() {
    }

    /**
     * Returns user name as unique identifier.
     *
     * @return user name as unique identifier.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Set user name to defined value.
     *
     * @param username
     *            String value of user name.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns user email as instance of {@link EmailAddress}.
     *
     * @return user email as instance of {@link EmailAddress}.
     */
    public EmailAddress getEmail() {
        return email;
    }

    /**
     * Set new user email value as instance of {@link EmailAddress}.
     *
     * @param newEmail
     *            new user email as instance of {@link EmailAddress}.
     */
    public void setEmail(EmailAddress newEmail) {
        email = newEmail;
    }

    /**
     * Returns the full user name.
     *
     * @return the full user name.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Set the new value of full user name.
     *
     * @param fullName
     *            the new value of full user name.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the user password value as {@link String}.
     *
     * @return the user password value as {@link String}.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Set the user password as value of {@link String}.
     *
     * @param password
     *            String value of password
     */
    public void setPassword(String password) {
        try {
            this.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Add the role to the role set.
     *
     * @param role
     *            The value of {@link Role} will be added.
     */
    public void addRole(RoleEntity role) {
        roles.add(role);
    }

    /**
     * Returns the set of roles for the user.
     *
     * @return the set of roles for the user.
     */
    public Set<RoleEntity> getRoles() {
        return roles;
    }

    /**
     * Set the set of roles.
     *
     * @param newRoles
     *            the set of roles will be set.
     */
    public void setRoles(Set<RoleEntity> newRoles) {
        roles = newRoles;
    }

    /**
     * Returns the set ID of this user as {@link Long} value.
     *
     * @return the set ID of this user as {@link Long} value.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Returns the user notification settings.
     *
     * @return the user notification settings.
     */
    public UserNotificationRecord getNotification() {
        return notification;
    }

    /**
     * Set new value of user notification settings.
     *
     * @param notification
     *            value of user notification settings.
     */
    public void setNotification(UserNotificationRecord notification) {
        this.notification = notification;
        notification.setUser(this);
    }

    /**
     * Returns the number of table rows.
     *
     * @return the number of table rows.
     */
    public Integer getTableRows() {
        return tableRows;
    }

    /**
     * Set the number of table rows.
     *
     * @param value
     *            number of table rows.
     */
    public void setTableRows(Integer value) {
        tableRows = value;
    }

    /**
     * Returns the collection of set authorities.
     *
     * @return the collection of set authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<Permission> permissions = new ArrayList<>();
        for (RoleEntity role : getRoles()) {
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    if (!permissions.contains(p)) {
                        permissions.add(p);
                    }
                }
            }
        }
        return permissions;
    }

    /**
     * Returns true if account is not expired, false otherwise.
     *
     * @return true if account is not expired, false otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Returns true if account is not locked, false otherwise.
     *
     * @return true if account is not locked, false otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Returns true if the credentials are not expired, false otherwise.
     *
     * @return true if the credentials are not expired,, false otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Returns true if user details are enabled, false otherwise.
     *
     * @return true if user details are enabled, false otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Returns the owner.
     *
     * @return The owner
     */
    @Override
    public User getOwner() {
        return this;
    }

    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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

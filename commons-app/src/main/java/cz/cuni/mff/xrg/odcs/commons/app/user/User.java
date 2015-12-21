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
package cz.cuni.mff.xrg.odcs.commons.app.user;

import cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.OpenEvent;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.ScheduleNotificationRecord;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds user data (his account).
 */
@Entity
@Table(name = "usr_user")
@SecondaryTable(name = "usr_extuser", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id_usr", referencedColumnName = "id"))
public class User implements UserDetails, DataObject {

    /**
     * Primary key for entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usr_user")
    @SequenceGenerator(name = "seq_usr_user", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * User name used for login and as a unique identification of User.
     */
    @Column(name = "username", length = 25, unique = true, nullable = false)
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
    @Column(name = "full_name", length = 55)
    private String fullName;

    /**
     * Hashed password.
     */
    @Column(name = "u_password", length = 142, nullable = false)
    private String password;

    @Column(name = "table_rows")
    private Integer tableRows;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
    private Set<Schedule> schedules = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
    private Set<Pipeline> pipelines = new HashSet<>();

    /**
     * User roles representing sets of privileges.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usr_user_role",
            joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") })
    private Set<RoleEntity> roles = new HashSet<>();

    /**
     * User notification settings used as a default for execution schedules.
     * Overridden by specific settings in {@link ScheduleNotificationRecord}.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserNotificationRecord notification;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "owner")
    private Set<OpenEvent> openEvents = new HashSet<>();

    @Column(table = "usr_extuser", name = "id_extuser", nullable = false)
    private String externalIdentifier;

    @Transient
    private UserActor userActor;

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
        if (email != null)
            email.setUser(this);
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

        if (role != null)
            role.getUsers().add(this);
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
        Collection<Permission> permissions = new HashSet<>();
        for (RoleEntity role : getRoles()) {
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    permissions.add(p);
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

    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public void setUserActor(UserActor actor) {
        this.userActor = actor;
    }

    public UserActor getUserActor() {
        return this.userActor;
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

    public Set<OpenEvent> getOpenEvents() {
        return openEvents;
    }

    public void setOpenEvents(Set<OpenEvent> openEvents) {
        this.openEvents = openEvents;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username=" + username +
                ", fullName=" + fullName +
                ", externalIdentifier=" + externalIdentifier +
                ", email='" + email + '\'' +
                ", userActor='" + userActor + '\'' +
                '}';
    }
}

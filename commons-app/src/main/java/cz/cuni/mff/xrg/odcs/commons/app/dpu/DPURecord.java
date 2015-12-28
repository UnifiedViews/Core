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
package cz.cuni.mff.xrg.odcs.commons.app.dpu;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * Represent imported DPU in database.
 *
 * @author Petyr
 * @author Bogo
 * @author Maria Kukhar
 */
@MappedSuperclass
public abstract class DPURecord implements DataObject {

    private final static String NULL_CONFIG = "<null configuration/>";

    /**
     * Primary key of graph stored in db
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_dpu_record")
    @SequenceGenerator(name = "seq_dpu_record", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * DPURecord name, provided by DPU, changeable by user.
     */
    @Column(name = "name", length = 1024)
    private String name;

    /**
     * DPURecord name shown in menus (shorter version than dpu.name), provided by DPU, changeable by user.
     */
    @Column(name = "menu_name")
    private String menuName;

    /**
     * If true then the value of {@link #description} has been created by DPU's
     * dialog.
     * TODO: Rename
     */
    @Column(name = "use_dpu_description")
    private boolean useDPUDescription;

    /**
     * DPURecord description, can be provided by user or by the DPU's dialog.
     */
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * DPU's configuration in serialized version.
     */
    @Column(name = "configuration")
    private byte[] serializedConfiguration = NULL_CONFIG.getBytes();

    /**
     * DPU instance. Created in {{@link #loadInstance(ModuleFacade)}.
     */
    @Transient
    protected Object instance;

    /**
     * Allow empty constructor for JPA.
     */
    public DPURecord() {
    }

    /**
     * Constructor with name and type of DPU record.
     *
     * @param name
     *            Name of the DPU.
     */
    public DPURecord(String name) {
        this.name = name;
        this.useDPUDescription = false;
    }

    /**
     * Create new DPURecord by copying the values from existing DPURecord.
     *
     * @param dpuRecord
     *            Existing DPU record.
     */
    public DPURecord(DPURecord dpuRecord) {
        this.name = dpuRecord.name;
        this.menuName = dpuRecord.menuName;
        this.useDPUDescription = dpuRecord.useDPUDescription;
        this.description = dpuRecord.description;
        if (dpuRecord.serializedConfiguration == null) {
            this.serializedConfiguration = null;
        } else {
            // deep copy
            this.serializedConfiguration = dpuRecord.serializedConfiguration
                    .clone();
        }
    }

    /**
     * @return Name of the DPU.
     */
    public String getName() {
        return name;
    }

    /**
     * @param newName
     *            New DPU name.
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * @return If true then the value of {@link #description} has been created
     *         by DPU's dialog.
     */
    public boolean isUseDPUDescription() {
        return useDPUDescription;
    }

    /**
     * @param useDPUDescription
     *            If true then the value of {@link #description} has been created by DPU's dialog.
     */
    public void setUseDPUDescription(boolean useDPUDescription) {
        this.useDPUDescription = useDPUDescription;
    }

    /**
     * @return DPU's description.
     */
    public String getDescription() {
        return StringUtils.defaultString(description);
    }

    /**
     * @param newDescription
     *            New DPU description.
     */
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return DPU's type.
     */
    public abstract DPUType getType();

    /**
     * Load appropriate DPU instance info {@link #instance}. The instance is
     * then accessible through the {@link #getInstance()} method.
     *
     * @param moduleFacade
     * @throws ModuleException
     */
    public abstract void loadInstance(ModuleFacade moduleFacade) throws ModuleException;

    /**
     * @return full path from the DPU's jar file relative to DPU's directory.
     */
    public abstract String getJarPath();

    /**
     * Get stored instance if loaded. To load instance use {@link #loadInstance}.
     *
     * @return Stored instance.
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * @return raw configuration representation.
     */
    public String getRawConf() {
        if (serializedConfiguration == null) {
            return null;
        }
        // workaround for null configurations
        // the null configuratino is not supported by virtuoso jdbc driver
        String configuration;
        try {
            configuration = new String(AppConfig.postprocess(serializedConfiguration), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        if (NULL_CONFIG.equals(configuration)) {
            return null;
        } else {
            return configuration;
        }
    }

    /**
     * Set raw configuration representation. Use with caution!
     *
     * @param conf
     */
    public void setRawConf(String conf) {
        // workaround for null configurations
        try {
            serializedConfiguration = AppConfig.preprocess(StringUtils.defaultString(conf, NULL_CONFIG).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates hash code from primary key if it is available, otherwise from
     * the rest of the attributes.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        if (this.id == null) {
            hash = 83 * hash + Objects.hashCode(this.name);
            hash = 83 * hash + Objects.hashCode(this.description);
            hash = 83 * hash + Objects.hashCode(getJarPath());
            hash = 83 * hash + Objects.hashCode(this.serializedConfiguration);
        } else {
            hash = 83 * hash + Objects.hashCode(this.id);
        }
        return hash;
    }

    /**
     * Compares DPURecord to other object. Two DPURecord instances are equal if
     * they have the same non-null primary key, or if both their primary keys
     * are {@code null} and their attributes are equal. Note that template's
     * configuration is also a part ofDPUs identity, because we may want to have
     * same DPUs that only differ in configuration (although we should ideally
     * change DPUs name).
     *
     * @param obj
     * @return whether {@code this} object is equal to given object
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DPURecord other = (DPURecord) obj;

        // try primary key comparison
        if (this.id != null && other.id != null) {
            // both have primary keys
            return Objects.equals(this.id, other.id);
        }
        if (this.id == null ^ other.id == null) {
            // only one has primary key
            return false;
        }

        // compare attributes
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.getType() != other.getType()) {
            return false;
        }
        if (!Objects.equals(getJarPath(), other.getJarPath())) {
            return false;
        }
        if (!Objects.equals(this.serializedConfiguration,
                other.serializedConfiguration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}

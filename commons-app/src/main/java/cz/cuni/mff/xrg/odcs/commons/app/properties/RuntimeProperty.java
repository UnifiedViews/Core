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
package cz.cuni.mff.xrg.odcs.commons.app.properties;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "runtime_properties")
public class RuntimeProperty implements Serializable, DataObject {
    private static final long serialVersionUID = -3916669563463666541L;

    /**
     * Unique ID for each property
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_runtime_properties")
    @SequenceGenerator(name = "seq_runtime_properties", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Human-readable property name
     */
    @Column(name = "name", unique = true, length = 100)
    private String name;

    /**
     * Human-readable property value
     */
    @Column(name = "value", length = 100)
    private String value;

    /**
     * Default constructor for JPA
     */
    public RuntimeProperty() {
    }

    /**
     * Returns the set ID of this runtime property as {@link Long} value
     * 
     * @return the set ID of this runtime property as {@link Long} value
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Returns name of the property
     * 
     * @return name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets new value to the property
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns value of the property
     * 
     * @return value of the property
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets new value for the property
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Hashcode is compatible with {@link #equals(java.lang.Object)}.
     * 
     * @return The value of hashcode.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Returns true if two objects represent the same property. This holds if
     * and only if <code>this.id == null ? this == obj : this.id == o.id</code>.
     * 
     * @param obj
     * @return true if both objects represent the same pipeline
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RuntimeProperty other = (RuntimeProperty) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuntimeProperty [id=" + id + ", name=" + name + ", value=" + value + "]";
    }
}

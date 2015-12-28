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
package cz.cuni.mff.xrg.odcs.commons.app.execution.context;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;

/**
 * Holds information about single {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit} context.
 * 
 * @author Petyr
 */
@Entity
@Table(name = "exec_dataunit_info")
public class DataUnitInfo implements DataObject {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_exec_dataunit_info")
    @SequenceGenerator(name = "seq_exec_dataunit_info", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Index of DataUnit. Used to determine folder.
     */
    @Column(name = "idx")
    private Integer index;

    /**
     * Name of DataUnit given to the DataUnit by DPU or changed by user (on the
     * edge).
     */
    @Column(name = "name", length = 2048)
    private String name;

    /**
     * DataUnit type.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", columnDefinition = "SMALLINT")
    private ManagableDataUnit.Type type;

    /**
     * True if use as input otherwise false.
     */
    @Column(name = "is_input")
    private boolean isInput;

    /**
     * Empty constructor because of JAP.
     */
    public DataUnitInfo() {
    }

    /**
     * @param name
     *            Name of DataUnit.
     * @param index
     *            Index of data unit.
     * @param type
     *            Type of DataUnit.
     * @param isInput
     *            Is used as input?
     */
    public DataUnitInfo(Integer index,
            String name,
            ManagableDataUnit.Type type,
            boolean isInput) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.isInput = isInput;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Integer getIndex() {
        return index;
    }

    /**
     * @return DataUnit's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return DateUnit'stype.
     */
    public ManagableDataUnit.Type getType() {
        return type;
    }

    /**
     * @return True it represented DataUnit is used as an input.
     */
    public boolean isInput() {
        return isInput;
    }

    @Override
    public String toString() {
        return name;
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

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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class for storing UnifiedViews properties.
 * Used by Debian packages to find out the version of the database and automatically apply certain update scripts
 * Used also by DbPropertiesTableUtils to check whether DB is up and running
 * Supports properties:
 * INSERT INTO `properties` VALUES ('UV.Core.version','002.003.000'),('UV.Plugin-DevEnv.version','002.001.000');
 * 
 * @author tomasknap
 */
@Entity
@Table(name = "properties")
public class Property implements Serializable {

    /**
     * Property key
     */
    @Id
    @Column(name = "\"key\"", unique = true, nullable = false, length = 200)
    private String key;

    /**
     * Property value
     */
    @Column(name = "\"value\"", length = 200)
    private String value;

    /**
     * Default constructor for JPA
     */
    public Property() {
    }

    @Override
    public String toString() {
        return "Property [key=" + key + ", value=" + value + "]";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

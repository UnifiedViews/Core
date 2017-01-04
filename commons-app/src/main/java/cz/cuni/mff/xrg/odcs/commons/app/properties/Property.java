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

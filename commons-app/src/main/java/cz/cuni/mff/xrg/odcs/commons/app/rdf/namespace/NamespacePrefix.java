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
package cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;

import javax.persistence.*;
import java.util.Objects;

/**
 * Entity representing RDF namespace prefix.
 * 
 * @author Jan Vojt
 */
@Entity
@Table(name = "rdf_ns_prefix")
public class NamespacePrefix implements DataObject {

    /**
     * Primary key of entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_rdf_ns_prefix")
    @SequenceGenerator(name = "seq_rdf_ns_prefix", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    /**
     * Prefix for namespace.
     */
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    /**
     * URI represented by prefix.
     */
    @Column(name = "uri", nullable = false, length = 2048)
    private String prefixURI;

    /**
     * Default constructor is required by JPA.
     */
    public NamespacePrefix() {
    }

    /**
     * Constructs new prefix with given name for given URI.
     * 
     * @param name
     *            prefix
     * @param prefixURI
     *            URI
     */
    public NamespacePrefix(String name, String prefixURI) {
        this.name = name;
        this.prefixURI = prefixURI;
    }

    @Override
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

    public String getPrefixURI() {
        return prefixURI;
    }

    public void setPrefixURI(String prefixURI) {
        this.prefixURI = prefixURI;
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

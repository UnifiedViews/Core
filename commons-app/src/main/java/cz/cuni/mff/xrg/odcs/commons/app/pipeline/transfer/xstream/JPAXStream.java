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
package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream;

import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * Customised version of {@link XStream} for working with objects that are
 * stored in database.
 * Usage of this class require all getters and setters to respect basic
 * conventions.
 *
 * @author Škoda Petr
 */
public class JPAXStream extends XStream {

    private final List<MemberFilter> filters = new LinkedList<>();

    protected JPAXStream() {
        super();
    }

    protected JPAXStream(HierarchicalStreamDriver hierarchicalStreamDriver) {
        super(hierarchicalStreamDriver);
    }

    @Override
    protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {

            @Override
            public boolean shouldSerializeMember(Class definedIn,
                    String fieldName) {
                // apply filters
                for (MemberFilter filter : filters) {
                    if (!filter.shouldSerializeMember(definedIn, fieldName)) {
                        return false;
                    }
                }
                return super.shouldSerializeMember(definedIn, fieldName);
            }

        };
    }

    public static JPAXStream createForPipeline(HierarchicalStreamDriver hierarchicalStreamDriver) {
        JPAXStream stream;
        if (hierarchicalStreamDriver != null) {
            stream = new JPAXStream(hierarchicalStreamDriver);
        } else {
            stream = new JPAXStream();
        }
        // setup
        ClassFilter classFilter = new ClassFilter();
        classFilter.add("org.eclipse.persistence");
        stream.filters.add(classFilter);

        NameFilter skipName = new NameFilter();
        skipName.add("id");
        skipName.add("_persistence_");
        skipName.add("owner");
        skipName.add("organization");
        stream.filters.add(skipName);

        AllowedFieldsFilter allowedfieldFilter = new AllowedFieldsFilter();
        allowedfieldFilter.add(DPUTemplateRecord.class, "jarName");
        allowedfieldFilter.add(DPUTemplateRecord.class, "name");
        allowedfieldFilter.add(DPUTemplateRecord.class, "rawConf");
        stream.filters.add(allowedfieldFilter);

        // this will use getters and setters for ono plain objects
        // usage of get/set will invoke jpa to load the data
        stream.registerConverter(new JavaBeanConverter(stream.getMapper()),
                PRIORITY_VERY_LOW);
        return stream;
    }

    public static JPAXStream createForSchedule(HierarchicalStreamDriver hierarchicalStreamDriver) {
        JPAXStream stream;
        if (hierarchicalStreamDriver != null) {
            stream = new JPAXStream(hierarchicalStreamDriver);
        } else {
            stream = new JPAXStream();
        }
        // setup
        ClassFilter classFilter = new ClassFilter();
        classFilter.add("org.eclipse.persistence");
        stream.filters.add(classFilter);
        NameFilter skipName = new NameFilter();
        skipName.add("id");
        skipName.add("_persistence_");
        skipName.add("owner");
        skipName.add("pipeline");
        stream.filters.add(skipName);
        // this will use getters and setters for ono plain objects
        // usage of get/set will invoke jpa to load the data
        stream.registerConverter(new JavaBeanConverter(stream.getMapper()),
                PRIORITY_VERY_LOW);
        return stream;
    }

    public static JPAXStream createForDPUTemplate(HierarchicalStreamDriver hierarchicalStreamDriver) {
        JPAXStream stream;
        if (hierarchicalStreamDriver != null) {
            stream = new JPAXStream(hierarchicalStreamDriver);
        } else {
            stream = new JPAXStream();
        }

        stream.alias("template", DPUTemplateRecord.class);

        // setup
        ClassFilter classFilter = new ClassFilter();
        classFilter.add("org.eclipse.persistence");
        stream.filters.add(classFilter);
        NameFilter skipName = new NameFilter();
        skipName.add("id");
        skipName.add("_persistence_");
        skipName.add("owner");
        stream.filters.add(skipName);
        // this will use getters and setters for ono plain objects
        // usage of get/set will invoke jpa to load the data
        stream.registerConverter(new JavaBeanConverter(stream.getMapper()),
                PRIORITY_VERY_LOW);
        return stream;
    }
}

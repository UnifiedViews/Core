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
package cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Class gather all the annotation for all the fields for given instance and
 * return them as a {@link List} of {@link AnnotationContainer}.
 * 
 * @author Petyr
 */
public class AnnotationGetter {

    private AnnotationGetter() {
    }

    /**
     * Get annotations of given type for all the fields in given DPU instance.
     * 
     * @param <T>
     * @param instance
     *            DPU instance from which get the annotations.
     * @param type
     *            Type of annotation to get
     * @return List with {@link AnnotationContainer}, can be empty.
     */
    public static <T extends Annotation> List<AnnotationContainer<T>> getAnnotations(Object instance,
            Class<T> type) {
        List<AnnotationContainer<T>> result = new LinkedList<>();
        // get all declared fields
        final Field[] fields = instance.getClass().getDeclaredFields();
        if (fields == null) {
            // DPU contains no fields
            return result;
        }
        // for each field
        for (Field field : fields) {
            T annotation = field.getAnnotation(type);
            if (annotation == null) {
                // no annotation of required type
            } else {
                // add to the result
                result.add(new AnnotationContainer<>(field, annotation));
            }
        }
        return result;
    }

}

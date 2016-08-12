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

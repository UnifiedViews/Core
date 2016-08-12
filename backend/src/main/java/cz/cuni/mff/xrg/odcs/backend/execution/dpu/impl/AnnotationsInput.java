package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.execution.dpu.DPUPreExecutor;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationContainer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationGetter;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnit;

/**
 * Examine the given DPU instance for {@link InputDataUnit} annotations. If
 * there is {@link InputDataUnit} annotation on field then create or assign
 * suitable DataUnit. If there is no {@link DataUnit} suitable then publish
 * event and return false.
 * Executed for every state. If the DPU has been already finished then we will
 * still need {@link DataUnit}s at the end of the execution.
 * 
 * @author Petyr
 */
@Component
public class AnnotationsInput implements DPUPreExecutor {

    public static final int ORDER = AnnotationsOutput.ORDER + 1000;

    private static final Logger LOG = LoggerFactory
            .getLogger(AnnotationsInput.class);

    /**
     * Event publisher used to publish error event.
     */
    @Autowired
    private ApplicationEventPublisher eventPublish;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean preAction(Node node,
            Map<Node, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo,
            boolean willExecute) {
        // get current context and DPUInstanceRecord
        Context context = contexts.get(node);

        // InputDataUnit annotation
        List<AnnotationContainer<DataUnit.AsInput>> inputAnnotations = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsInput.class);
        for (AnnotationContainer<DataUnit.AsInput> item : inputAnnotations) {
            if (annotationInput(item, dpuInstance, context)) {
                // ok
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Set value to given field for given instance. In case of error publish
     * event and return false.
     * 
     * @param field
     *            Field to set.
     * @param instance
     *            Instance.
     * @param value
     *            Value to set.
     * @param context
     *            Used to publish exception.
     * @return True if the field has been set.
     */
    protected boolean setDataUnit(Field field,
            Object instance,
            Object value,
            Context context) {
        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // create message
            final String message = Messages.getString("AnnotationsInput.set.value.failed", field.getName(), e.getMessage());
            eventPublish.publishEvent(DPUEvent.createPreExecutorFailed(context,
                    this, message));
            return false;
        }
        return true;
    }

    /**
     * Filter given list of {@link ManagableDataUnit}s by using {@link Class}.
     * 
     * @param candidates
     * @param type
     * @return List with {@link ManagableDataUnit} can be empty, can not be
     *         null.
     */
    protected LinkedList<ManagableDataUnit> filter(
            List<ManagableDataUnit> candidates,
            Class<?> type) {
        LinkedList<ManagableDataUnit> result = new LinkedList<>();
        for (ManagableDataUnit item : candidates) {
            if (type.isInstance(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Filter given list of {@link ManagableDataUnit}s according to their URI.
     * Is case insensitive.
     * 
     * @param candidates
     * @param name
     *            Required name of DataUnits.
     * @return List with {@link ManagableDataUnit} can be empty, can not be
     *         null.
     */
    protected LinkedList<ManagableDataUnit> filter(
            List<ManagableDataUnit> candidates,
            String name) {
        LinkedList<ManagableDataUnit> result = new LinkedList<>();
        for (ManagableDataUnit item : candidates) {
            if (item.getName().compareToIgnoreCase(name) == 0) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Execute the input annotation ie. get input {@link DataUnit} from {@link Context} and assign it to annotated field. If annotationContainer
     * is null then instantly return true. If error appear then publish event
     * and return false.
     * 
     * @param annotationContainer
     *            Annotation container.
     * @param dpuInstance
     * @param context
     * @return False in case of error.
     */
    protected boolean annotationInput(
            AnnotationContainer<DataUnit.AsInput> annotationContainer,
            Object dpuInstance,
            Context context) {
        if (annotationContainer == null) {
            return true;
        }
        final Field field = annotationContainer.getField();
        final DataUnit.AsInput annotation = annotationContainer.getAnnotation();

        LinkedList<ManagableDataUnit> typeMatch = filter(context.getInputs(),
                field.getType());
        if (typeMatch.isEmpty()) {
            // check if not optional
            if (annotation.optional()) {
                return true;
            }
            final String message = Messages.getString("AnnotationsInput.no.input", field.getName());
            eventPublish.publishEvent(DPUEvent.createPreExecutorFailed(context,
                    this, message));
            return false;
        }
        // now we filter by name
        LinkedList<ManagableDataUnit> nameMatch = filter(typeMatch,
                annotation.name());
        if (nameMatch.isEmpty()) {
            // check if not optional
            if (annotation.optional()) {
                return true;
            }
            // error
            final String message = Messages.getString("AnnotationsInput.dataUnit.notFound")
                    + field.getName();
            eventPublish.publishEvent(DPUEvent.createPreExecutorFailed(
                    context, this, message));
            return false;
        } else {
            if (nameMatch.size() > 1) {
                LOG.warn("Multiple matches for {}", annotation.name());
            }

            LOG.debug("in: {}.{} = {}", context.getDPU().getName(), field
                    .getName(),
                    nameMatch.getFirst().getName());

            // use first with required name
            return setDataUnit(field, dpuInstance, nameMatch.getFirst(),
                    context);
        }
    }

}

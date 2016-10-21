package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import java.lang.reflect.Field;
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
import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitTypeResolver;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationContainer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationGetter;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Examine the given DPU instance for annotations. If there is {@link OutputDataUnit} annotation on field then create or assign suitable
 * DataUnit. If there is no {@link DataUnit} suitable then publish event and
 * return false.
 * Executed for every state.
 * 
 * @author Petyr
 */
@Component
public class AnnotationsOutput implements DPUPreExecutor {

    public static final int ORDER = DPUPreExecutorContextPreparator.ORDER + 1000;

    private static final Logger LOG = LoggerFactory
            .getLogger(AnnotationsOutput.class);

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

        // OutputDataUnit annotation
        List<AnnotationContainer<DataUnit.AsOutput>> outputAnnotations = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsOutput.class);
        for (AnnotationContainer<DataUnit.AsOutput> item : outputAnnotations) {
            if (annotationOutput(item, dpuInstance, context)) {
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
            final String message = Messages.getString("AnnotationsOutput.value.set.failed", field.getName(), e.getMessage());
            eventPublish.publishEvent(DPUEvent.createPreExecutorFailed(context,
                    this, message));
            return false;
        }
        return true;
    }

    /**
     * Execute the output annotation ie. create output {@link DataUnit} and
     * assign it to the annotated field. If annotation is null then instantly
     * return true. If error appear then publish event and return false.
     * 
     * @param annotationContainer
     *            Annotation container.
     * @param dpuInstance
     * @param context
     * @return False in case of error.
     */
    protected boolean annotationOutput(
            AnnotationContainer<DataUnit.AsOutput> annotationContainer,
            Object dpuInstance,
            Context context) {
        if (annotationContainer == null) {
            return true;
        }
        final Field field = annotationContainer.getField();
        final DataUnit.AsOutput annotation = annotationContainer.getAnnotation();
        LOG.debug("Data unit name is: {}", annotation.name());

        // get type
        ManagableDataUnit.Type type;
        type = DataUnitTypeResolver.resolveClassToType(field.getType());

        //classToDataUnitType(field.getType());
        if (type == null) {
            final String message = Messages.getString("AnnotationsOutput.unknown.field", field.getName());
            // type cannot be resolved -> publish event
            eventPublish.publishEvent(DPUEvent.createPreExecutorFailed(context,
                    this, message));
            return false;
        }
        LOG.debug("Data unit type is: {}", type.toString());

        // let's create dataUnit
        ManagableDataUnit dataUnit;
        // if the data unit with such name and type already
        // exist then is returned and reused
        try {
            dataUnit = context.addOutputDataUnit(type, annotation.name());
        } catch (DataUnitException ex) {
            LOG.error("Failed to add output DataUnit", ex);
            return false;
        }

        LOG.debug("out: {}.{} = {}", context.getDPU().getName(), field.getName(),
                dataUnit.getName());
        // and set it
        return setDataUnit(field, dpuInstance, dataUnit, context);
    }

}

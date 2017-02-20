package cz.cuni.mff.xrg.odcs.backend.execution.dpu.impl;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.context.ContextException;
import cz.cuni.mff.xrg.odcs.backend.context.ContextFacade;
import cz.cuni.mff.xrg.odcs.backend.dpu.event.DPUEvent;
import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeInstructions;
import cz.cuni.mff.xrg.odcs.commons.app.execution.DPUExecutionState;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ProcessingUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.*;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Examine the {@link DependencyGraph} for given {@link PipelineExecution}. Add
 * data from precedents' context to the context of the current DPU, that is
 * specified by {@link Node}.
 * We execute this only for {@link DPUExecutionState#PREPROCESSING} state as for any other state the context has been already prepared.
 * 
 * @author Petyr
 */
@Component
class DPUPreExecutorContextPreparator extends DPUPreExecutorBase {

    /**
     * Pre-executor order.
     */
    public static final int ORDER = 0;

    /**
     * Event publisher used to publish error event.
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ContextFacade contextFacade;

    public DPUPreExecutorContextPreparator() {
        super(Arrays.asList(DPUExecutionState.PREPROCESSING));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * In case of error log the error, publish message and the return false.
     */
    @Override
    protected boolean execute(ExecutedNode node,
            Map<ExecutedNode, Context> contexts,
            Object dpuInstance,
            PipelineExecution execution,
            ProcessingUnitInfo unitInfo) {
        // get current context
        Context context = contexts.get(node);

        // ! ! ! !
        // the context can contain data from previous
        // PREPROCESSING phase that has been interrupted
        // so some DataUnit can already been created and may contains some
        // data .. we solve this in contextFacade.merge
        // which take care about this

        // check all edges which have this not as the target node
        for (ExecutedEdge edge : node.getIncomingEdges()) {
            //iterate over all edges which has as target the examined node

            // identify source
            ExecutedNode sourceNode = edge.getFrom();
            Context sourceContext = contexts.get(sourceNode);
            if (sourceContext == null) {
                // publish message
                eventPublisher.publishEvent(
                        DPUEvent.createPreExecutorFailed(context, this, Messages.getString("DPUPreExecutorContextPreparator.missing.context", sourceNode.getDpuInstance().getName())));
                return false;
            }

            //if source node not analyzed, analyze
            if (!sourceNode.isOutputsAnalysedWhetherConsumedByMultipleDPUs()) {

                // If the source is producing data via data unit X to more then one DPU, then the data units consuming such data should not optimize.
                Set<String> dataUnitNamesWhichAreUsedMultipleTimes = new HashSet<>();
                Set<String> dataUnitNamesWhichWereAlreadyUsed = new HashSet<>();
                //Examine the scripts. If at least two edges contain the same label as the source, then mark the data unit with that label somehow
                for (ExecutedEdge ed : sourceNode.getOutgoingEdges()) {
                    examineEdge(ed.getEdge().getScript(), dataUnitNamesWhichWereAlreadyUsed, dataUnitNamesWhichAreUsedMultipleTimes);

                }

                //For each data unit used more times, mark corresponding data unit with a flag
                List<ManagableDataUnit> outputs = sourceContext.getOutputs();
                for (String s : dataUnitNamesWhichAreUsedMultipleTimes) {
                    for (ManagableDataUnit outDataUnit : outputs) {
                        if (outDataUnit.getName().equals(s)) {
                            // mark the output data unit with a flag
                            outDataUnit.setConsumedByMultipleInputs(true);
                            break;
                        }
                    }
                }
                sourceNode.setOutputsAnalysedWhetherConsumedByMultipleDPUs(true);
            }

            // merge data
            try {
                contextFacade.merge(context, sourceContext, edge.getEdge().getScript());
            } catch (ContextException e) {
                eventPublisher.publishEvent(
                        DPUEvent.createPreExecutorFailed(context, this,
                                Messages.getString("DPUPreExecutorContextPreparator.merge.failed"), e));
                return false;
            }

        }
        return true;
    }

    private void examineEdge(String script, Set<String> dataUnitNamesWhichWereAlreadyUsed, Set<String> dataUnitNamesWhichAreUsedMultipleTimes) {
        //edge script sample
        String[] rules = script.split(EdgeInstructions.Separator
                .getValue());
        for (String item : rules) {
            String[] elements = item.split(" ", 2);
            // test name ..
            if (elements.length < 2) {
                // not enough data .. skip
            } else { // elements.length == 2
                String outputDataUnitName = elements[0];
                if (dataUnitNamesWhichWereAlreadyUsed.contains(outputDataUnitName)) {
                    dataUnitNamesWhichAreUsedMultipleTimes.add(outputDataUnitName);
                } else {
                    dataUnitNamesWhichWereAlreadyUsed.add(outputDataUnitName);
                }
            }
        }
        return;

    }

}

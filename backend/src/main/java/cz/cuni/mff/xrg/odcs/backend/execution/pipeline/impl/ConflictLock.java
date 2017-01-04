package cz.cuni.mff.xrg.odcs.backend.execution.pipeline.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.backend.context.Context;
import cz.cuni.mff.xrg.odcs.backend.execution.pipeline.PreExecutor;
import cz.cuni.mff.xrg.odcs.backend.pipeline.event.PipelineInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;

/**
 * The main class for component that is used to lock pipelines. This
 * functionality is used to enable pipeline "Conflicts".
 * 
 * @author Petyr
 */
@Component
public class ConflictLock implements PreExecutor {

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * The second part of "pipeline conflict" component.
     */
    @Autowired
    private ConflictUnLock unlocker;

    /**
     * Store running execution together with respective threads.
     */
    private final HashMap<Pipeline, Thread> locks = new HashMap<>();

    /**
     * Return when given execution can continue ie. there are no conflict
     * pipelines running.
     * 
     * @param execution
     */
    public void lock(PipelineExecution execution) {
        synchronized (locks) {
            if (canRun(execution)) {
                // ok we can, just add us to the lock
                locks.put(execution.getPipeline(), Thread.currentThread());
                return;
            } else {
                // publish message ..
                publisher
                        .publishEvent(PipelineInfo.createWait(execution, this));
            }

            // and wait for success
            for (;;) {
                // wait for change on locks
                try {
                    locks.wait();
                } catch (InterruptedException e) {
                }
                // test if we can run
                if (canRun(execution)) {
                    publisher.publishEvent(PipelineInfo.createWaitEnd(
                            execution, this));
                    return;
                }
            }
        }
    }

    /**
     * Remove the pipeline from list of running pipelines. So the given
     * execution will no longer be considered as a candidate for possible
     * conflict for new pipeline.
     * 
     * @param execution
     */
    public void unlock(PipelineExecution execution) {
        synchronized (locks) {
            // just remove the execution
            locks.remove(execution.getPipeline());
            // and wake possibly waiting pipelines
            locks.notifyAll();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean preAction(PipelineExecution execution,
            Map<Node, Context> contexts,
            DependencyGraph graph,
            boolean success) {
        if (success) {
            // ok so far good, the pipeline will run get lock
            lock(execution);
        } else {
            // something faield .. the execution will not start .. 
        }
        return true;
    }

    /**
     * Return true if there are no conflicts for given execution. Must run from
     * inside block that is synchronised for {@link #locks}.
     * 
     * @param execution
     * @return False if there is conflict pipeline running.
     */
    private boolean canRun(PipelineExecution execution) {
        final Set<Pipeline> conflicts = execution.getPipeline().getConflicts();
        for (Pipeline conflict : conflicts) {
            if (locks.containsKey(conflict)) {
                // conflict pipeline can be running
                // test if thread is active ..
                if (locks.get(conflict).isAlive()) {
                    // still alive .. we have to wait ..
                    return false;
                } else {
                    // is dead .. we can remove it
                    // as it probably die horribly and does not call
                    // unlock
                    locks.remove(conflict);
                    // and continue
                }
            }
        }
        return true;
    }
}

package cz.cuni.mff.xrg.odcs.backend.execution;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.facade.ExecutionFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;

public class EngineMock extends Engine {

    public EngineMock() {
        this.startUpDone = true;
        this.backendID = "TestBackend";
    }

    public final List<PipelineExecution> historyOfExecution = new ArrayList<>();

    @Override
    public synchronized void run(PipelineExecution execution) {
        historyOfExecution.add(execution);
        execution.setStatus(PipelineExecutionStatus.FINISHED_SUCCESS);
        this.pipelineFacade.save(execution);
    }

    public PipelineFacade getPipelineFacade() {
        return pipelineFacade;
    }

    public void setPipelineFacade(PipelineFacade pipelineFacade) {
        this.pipelineFacade = pipelineFacade;
    }

    public void setExecutionFacade(ExecutionFacade executionFacade) {
        this.executionFacade = executionFacade;
    }

    public void doCheck() {
        checkJobs();
    }
}

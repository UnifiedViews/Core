package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionInfo;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.frontend.gui.components.DataUnitSelector.SelectionChangedEvent;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;

/**
 * Component for browsing results of {@link PipelineExecution}.
 * 
 * @author Bogo
 */
public class Browse extends CustomComponent {

    private final VerticalLayout mainLayout;

    private final DataUnitSelector selector;

    private PipelineExecution execution;

    private QueryView queryView;

    private static Logger LOG = LoggerFactory.getLogger(Browse.class);

    /**
     * Constructor.
     * 
     * @param execution
     */
    public Browse(PipelineExecution execution) {
        // set local execution
        this.execution = execution;

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);

        selector = new DataUnitSelector(execution);
        selector.addListener(new Listener() {
            @Override
            public void componentEvent(Event event) {
                if (event.getClass() == DataUnitSelector.BrowseRequestedEvent.class) {
                    queryView.browseDataUnit();
                } else if (event.getClass() == DataUnitSelector.EnableEvent.class) {
                    queryView.setQueryingEnabled(true);
                } else if (event.getClass() == DataUnitSelector.DisableEvent.class) {
                    queryView.setQueryingEnabled(false);
                } else if (event.getClass() == SelectionChangedEvent.class) {
                    SelectionChangedEvent changedEvent = (SelectionChangedEvent) event;
                    checkQueryView(changedEvent.getInfo());
                    queryView.setDataUnitInfo(changedEvent.getInfo());
                    queryView.setSelectedDpu(changedEvent.getDpu());
                }
            }
        });

        mainLayout.addComponent(selector);

        queryView = new RDFQueryView();
        queryView.setQueryingEnabled(false);
        queryView.setExecutionInfo(getExecutionInfo(execution));
        mainLayout.addComponent(queryView);

        setCompositionRoot(mainLayout);
    }

    void setDpu(DPUInstanceRecord debugDpu) {
        selector.setSelectedDPU(debugDpu);
    }

    void refreshDPUs(PipelineExecution pipelineExec) {
        execution = pipelineExec;
        selector.refresh(pipelineExec);
        queryView.setExecutionInfo(getExecutionInfo(pipelineExec));
        queryView.reset();
    }

    private void checkQueryView(DataUnitInfo info) {
        if (info == null) {
            return;
        }
        if (info.getType() == ManagableDataUnit.Type.FILES) {
            if (queryView.getClass() != FileQueryView.class) {
                mainLayout.removeComponent(queryView);
                queryView = new FileQueryView();
                mainLayout.addComponent(queryView);
            }
        } else if (info.getType() == ManagableDataUnit.Type.RDF) {
            if (queryView.getClass() != RDFQueryView.class) {
                mainLayout.removeComponent(queryView);
                queryView = new RDFQueryView();
                queryView.setExecutionInfo(getExecutionInfo(execution));
                mainLayout.addComponent(queryView);
            }
        } else if (info.getType() == ManagableDataUnit.Type.RELATIONAL) {
            if (this.queryView.getClass() != RelationalQueryView.class) {
                this.mainLayout.removeComponent(this.queryView);
                this.queryView = new RelationalQueryView();
                this.queryView.setExecutionInfo(getExecutionInfo(this.execution));
                this.mainLayout.addComponent(this.queryView);
            }
        } else {
            LOG.error("Unknown data unit type {}, unable to create query view");
            return;
        }
    }

    private ExecutionInfo getExecutionInfo(PipelineExecution exec) {
        if (exec == null) {
            return null;
        }
        ExecutionContextInfo context = exec.getContextReadOnly();
        if (context != null) {
            return new ExecutionInfo(context);
        }
        return null;
    }
}

package cz.cuni.mff.xrg.odcs.frontend.gui.components;

/**
 * Dialog for pipeline status information. Opens from DPU Template Details
 * Instances tab in {@link DPU}. Contains information about pipeline status and
 * last run of pipeline
 *
 * @author Maria Kukhar
 *
 */

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Status window for pipeline.
 * 
 * @author Bogo
 */
@Component
@Scope("prototype")
public class PipelineStatus extends Window {

    private String lastRunTimeStr;

    private Label lastRunTime;

    private Label runsNumber;

    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * Basic constructor
     */
    public PipelineStatus() {

        this.setResizable(false);
        this.setDraggable(false);
        this.setModal(true);
        this.setCaption(Messages.getString("PipelineStatus.status"));

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setWidth("200px");
        mainLayout.setHeight("100px");

        HorizontalLayout lastRunLayout = new HorizontalLayout();
        lastRunLayout.setSpacing(true);
        lastRunLayout.addComponent(new Label(Messages.getString("PipelineStatus.last.run")));

        lastRunTime = new Label();

        if (lastRunTimeStr == null) {
            lastRunTimeStr = "";
        }

        lastRunTime.setCaption(lastRunTimeStr);
        lastRunLayout.addComponent(lastRunTime);

        HorizontalLayout runsNumberLayout = new HorizontalLayout();
        runsNumberLayout.setSpacing(true);
        runsNumberLayout.addComponent(new Label(Messages.getString("PipelineStatus.run.numbers")));

        runsNumber = new Label();

        runsNumberLayout.addComponent(runsNumber);

        mainLayout.addComponent(lastRunLayout);
        mainLayout.addComponent(runsNumberLayout);

        this.setContent(mainLayout);
        setSizeUndefined();

    }

    /**
     * Get date of the last execution and number of run for specific pipeline.
     * 
     * @param selectedPipeline
     */
    public void setSelectedPipeline(Pipeline selectedPipeline) {

        Date maxDate = null;

        List<PipelineExecution> executions = pipelineFacade.getExecutions(selectedPipeline);

        //getting number of pipeline run and date of the last pipeline execution
        for (PipelineExecution item : executions) {
            if (maxDate == null || maxDate.getTime() < item.getStart().getTime()) {
                maxDate = item.getStart();
            }
        }
        if (maxDate != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, LocaleHolder.getLocale());
            lastRunTimeStr = df.format(maxDate);
        } else {
            lastRunTimeStr = "";
        }

        runsNumber.setCaption(Integer.toString(executions.size()));
        lastRunTime.setCaption(lastRunTimeStr);
    }
}

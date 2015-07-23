/*******************************************************************************
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
 *******************************************************************************/
/*******************************************************************************
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
 *******************************************************************************/
package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Container;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQuery;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryBuilder;
import cz.cuni.mff.xrg.odcs.commons.app.dao.db.DbQueryCount;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.DbPipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;
import cz.cuni.mff.xrg.odcs.frontend.container.accessor.PipelineNameAccessor;
import cz.cuni.mff.xrg.odcs.frontend.doa.container.InMemorySource;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Dialog for pipeline conflict creation.
 * 
 * @author Maria Kukhar
 * @author Petyr
 */
public class PipelineConflicts extends Window {

    private static final long serialVersionUID = 1L;

    private VerticalLayout mainLayout;

    private Container container;

    @Autowired
    private DbPipeline dbPipeline;

    private TwinColSelect selectPipe;

    private Button clearConflicts;

    private boolean isInitialized = false;

    private boolean result = false;

    @Autowired
    private PipelineFacade pipelineFacade;

    /**
     * The current pipeline.
     */
    private Pipeline pipeline;

    private InMemorySource<Pipeline, DbQueryBuilder<Pipeline>, DbQuery<Pipeline>, DbQueryCount<Pipeline>> source;

    /**
     * Empty constructor.
     */
    public PipelineConflicts() {

    }

    /**
     * Initializes the component.
     */
    public void init() {
        if (isInitialized) {
            // do nothing .. the init has already been done
            return;
        }

        // set dialog properties
        this.setResizable(false);
        this.setModal(true);
        this.setCaption(Messages.getString("PipelineConflicts.pipeline.conflicts"));
        // build layout
        buildMainLayout();
        this.setContent(mainLayout);
        setSizeUndefined();
        isInitialized = true;
    }

    /**
     * Set pipeline.
     * 
     * @param pipeline
     */
    public void setData(Pipeline pipeline) {
        result = false;
        this.pipeline = pipeline;

        Set<Pipeline> conflicts = pipeline.getConflicts();
        List<Long> conflictsNames = new ArrayList<>();
        for (Pipeline conflictitem : conflicts) {
            conflictsNames.add(conflictitem.getId());
        }
        selectPipe.setValue(conflictsNames);
        this.source.loadData(this.dbPipeline);
    }

    /**
     * Is component initialized.
     * 
     * @return If component is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Builds main layout
     * 
     * @return mainLayout VerticalLayout with all dialog components
     */
    @AutoGenerated
    private VerticalLayout buildMainLayout() {

        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("435px");

        this.source = new InMemorySource<>(new PipelineNameAccessor(), dbPipeline);
        container = new ReadOnlyContainer<>(this.source);

        Label description = new Label();
        description.setValue(Messages.getString("PipelineConflicts.description"));
        this.mainLayout.addComponent(description);

        //Component for pipelines select
        selectPipe = new TwinColSelect();
        selectPipe.setContainerDataSource(container);
        selectPipe.setNullSelectionAllowed(true);
        selectPipe.setMultiSelect(true);
        selectPipe.setImmediate(true);
        selectPipe.setWidth("400px");
        selectPipe.setHeight("200px");
        selectPipe.setItemCaptionPropertyId("name");
        selectPipe.setLeftColumnCaption(Messages.getString("PipelineConflicts.pipeline.list"));
        selectPipe.setRightColumnCaption(Messages.getString("PipelineConflicts.conflicting.pipelines"));

        mainLayout.addComponent(selectPipe);

        clearConflicts = new Button(Messages.getString("PipelineConflicts.clear.conflicts"));
        clearConflicts.setImmediate(true);
        clearConflicts.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                selectPipe.setValue(null);
            }
        });
        mainLayout.addComponent(clearConflicts);
        mainLayout.setComponentAlignment(clearConflicts, Alignment.MIDDLE_RIGHT);

        //Layout with buttons Save and Cancel
        HorizontalLayout buttonBar = new HorizontalLayout();

        //Save button
        Button saveButton = new Button();
        saveButton.setCaption(Messages.getString("PipelineConflicts.ok"));
        saveButton.setWidth("90px");
        saveButton.setImmediate(true);
        saveButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                Set<Object> selectedPipelines = (Set) selectPipe.getValue();
                Iterator<Object> it = selectedPipelines.iterator();

                Set<Pipeline> conflicts = pipeline.getConflicts();
                conflicts.clear();

                while (it.hasNext()) {
                    Object selectPipe = it.next();
                    Pipeline item = pipelineFacade.getPipeline((Long) selectPipe);
                    conflicts.add(item);
                }

                result = true;
                close();
            }

        });
        buttonBar.addComponent(saveButton);

        //Cancel button
        Button cancelButton = new Button(Messages.getString("PipelineConflicts.cancel"), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                result = false;
                close();

            }
        });
        cancelButton.setWidth("90px");
        cancelButton.setImmediate(true);
        buttonBar.addComponent(cancelButton);

        mainLayout.addComponent(buttonBar);

        return mainLayout;
    }

    /**
     * Get result.
     * 
     * @return result
     */
    public boolean getResult() {
        return result;
    }

}

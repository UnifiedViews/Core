package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.server.FileDownloader;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.transfer.ExportService;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportException;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandFileDownloader;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandStreamResource;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

public class DPUTemplatesExport extends Window {

    private static final long serialVersionUID = -2397548817493646720L;

    private static final Logger LOG = LoggerFactory.getLogger(DPUTemplatesExport.class);

    private static final Object CHECKBOX_ID = Messages.getString("DPUTemplatesExport.template");

    private TreeTable templatesTree;

    public DPUTemplatesExport(List<DPUTemplateRecord> dpuTemplates, final ExportService dpuExportService) {
        setModal(true);
        setHeight("500px");
        setWidth("400px");
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        templatesTree = new TreeTable(Messages.getString("DPUTemplatesExport.dpu.templates"));
        templatesTree.setSizeFull();
        templatesTree.addContainerProperty(CHECKBOX_ID, CheckBox.class, "");
        templatesTree.addContainerProperty(Messages.getString("DPUTemplatesExport.type"), String.class, "");
        templatesTree.setPageLength(dpuTemplates.size());

        for (DPUTemplateRecord template : dpuTemplates) {
            templatesTree.addItem(new Object[] { new CheckBox(template.getName(), true), template.getType().toString() }, template);
            if (template.getParent() != null) {
                templatesTree.setParent(template, template.getParent());
            }
        }
        for (Object itemId : templatesTree.getItemIds()) {
            templatesTree.setCollapsed(itemId, false);
        }

        ((HierarchicalContainer) templatesTree.getContainerDataSource()).setItemSorter(new ItemSorter() {
            private static final long serialVersionUID = 6274994221136029459L;

            @Override
            public void setSortProperties(Container.Sortable container, Object[] propertyId, boolean[] ascending) {
                //Ignore
            }

            @Override
            public int compare(Object itemId1, Object itemId2) {
                DPUTemplateRecord first = (DPUTemplateRecord) itemId1;
                DPUTemplateRecord second = (DPUTemplateRecord) itemId2;

                if (first.getType() != second.getType()) {
                    return first.getType().compareTo(second.getType());
                }

                if (first.getId() == null && second.getId() == null) {
                    return 0;
                } else {
                    return first.getName().compareTo(second.getName());
                }
            }
        });
        ((HierarchicalContainer) templatesTree.getContainerDataSource()).sort(null, null);

        templatesTree.setVisible(true);
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(templatesTree);
        mainLayout.addComponent(panel);
        mainLayout.setExpandRatio(panel, 1.0f);

        HorizontalLayout buttons = new HorizontalLayout();

        buttons.addComponent(new Button(Messages.getString("DPUTemplatesExport.checkAll"), new Button.ClickListener() {
            private static final long serialVersionUID = 3669980741182913698L;

            @Override
            public void buttonClick(ClickEvent event) {
                checkAll(true);
            }
        }));

        buttons.addComponent(new Button(Messages.getString("DPUTemplatesExport.uncheckAll"), new Button.ClickListener() {
            private static final long serialVersionUID = 8629125312701990026L;

            @Override
            public void buttonClick(ClickEvent event) {
                checkAll(false);
            }
        }));
        
        final Button closeButton = new Button(Messages.getString("DPUTemplatesExport.cancel"), new Button.ClickListener() {
            private static final long serialVersionUID = 1404822664816505889L;

            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        });

        Button exportButton = new Button(Messages.getString("DPUTemplatesExport.export"));
        FileDownloader fileDownloader = new OnDemandFileDownloader(new OnDemandStreamResource() {
            private static final long serialVersionUID = -7424330351227391587L;

            @Override
            public InputStream getStream() {
                List<DPUTemplateRecord> templatesToExport = getSelectedTemplates();
                File fileToExport;
                try {
                    fileToExport = dpuExportService.exportDPUs(templatesToExport);
                } catch (ExportException e) {
                    LOG.error("Failed to export templates.", e);
                    Notification.show(Messages.getString("DPUTemplatesExport.export.fail"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    return null;
                }
                try {
                    closeButton.click();
                    return new DeletingFileInputStream(fileToExport);
                } catch (FileNotFoundException e) {
                    LOG.error("Failed to export templates.", e);
                    Notification.show(Messages.getString("DPUTemplatesExport.export.fail.fileNotFound"), e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    return null;
                }
            }

            @Override
            public String getFilename() {
                return "dpuTemplates.zip";
            }
        });
        fileDownloader.extend(exportButton);
        buttons.addComponent(exportButton);
        buttons.addComponent(closeButton);
        mainLayout.addComponent(buttons);
        mainLayout.setComponentAlignment(buttons, Alignment.BOTTOM_LEFT);

        setContent(mainLayout);
    }

    protected void checkAll(boolean checkAll) {
        for (Object itemId : templatesTree.getItemIds()) {
            CheckBox checkBox = (CheckBox) templatesTree.getItem(itemId).getItemProperty(CHECKBOX_ID).getValue();
            checkBox.setValue(checkAll);
        }
    }

    private List<DPUTemplateRecord> getSelectedTemplates() {
        List<DPUTemplateRecord> selectedTemplates = new ArrayList<DPUTemplateRecord>();
        for (Object itemId : templatesTree.getItemIds()) {
            CheckBox checkBox = (CheckBox) templatesTree.getItem(itemId).getItemProperty(CHECKBOX_ID).getValue();
            if (checkBox.getValue()) { // checked
                selectedTemplates.add((DPUTemplateRecord) itemId);
            }
        }
        return selectedTemplates;
    }
}

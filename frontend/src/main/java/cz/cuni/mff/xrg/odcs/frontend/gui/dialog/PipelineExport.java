package cz.cuni.mff.xrg.odcs.frontend.gui.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import com.vaadin.server.FileDownloader;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.DpuItem;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportService;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportSetting;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandFileDownloader;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandStreamResource;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * @author Škoda Petr
 */
public class PipelineExport extends Window {

    private static final Logger LOG = LoggerFactory.getLogger(
            PipelineExport.class);

    private CheckBox chbExportDPUData;

    private CheckBox chbExportJars;

    private CheckBox chbExportSchedule;

    private Label usedJarsLabel;

    private Label usedJarsText;

    private AuthenticationContext authCtx;

    /**
     * Export service.
     */
    private final ExportService exportService;

    /**
     * Pipeline to export.
     */
    private Pipeline pipeline;

    public PipelineExport(ExportService exportService, Pipeline pipeline, AuthenticationContext authCtx) {
        this.exportService = exportService;
        this.pipeline = pipeline;
        this.authCtx = authCtx;
        init();
    }

    private void init() {
        this.setResizable(false);
        this.setModal(true);
        this.setWidth("500px");
        this.setHeight("350px");
        this.setCaption(Messages.getString("PipelineExport.pipeline.export"));

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        final VerticalLayout detailLayout = new VerticalLayout();
        detailLayout.setWidth("100%");

        chbExportDPUData = new CheckBox(Messages.getString("PipelineExport.dpu.export"));
        chbExportDPUData.setWidth("100%");
        chbExportDPUData.setValue(false);

        if (hasPermission("pipeline.exportDpuData")) {
            detailLayout.addComponent(chbExportDPUData);
        }
        chbExportJars = new CheckBox(Messages.getString("PipelineExport.jar.export"));
        chbExportJars.setWidth("100%");
        chbExportJars.setValue(false);

        if (hasPermission("pipeline.exportDpuJars")) {
            detailLayout.addComponent(chbExportJars);
        }

        chbExportSchedule = new CheckBox(Messages.getString("PipelineExport.schedule.export"));
        chbExportSchedule.setWidth("100%");
        chbExportSchedule.setValue(false);
        detailLayout.addComponent(chbExportSchedule);

        final VerticalLayout usedJarsLayout = new VerticalLayout();
        usedJarsLayout.setWidth("100%");

        Panel panel = new Panel(Messages.getString("PipelineExport.dpus.used"));
        panel.setWidth("100%");
        panel.setHeight("150px");

        TreeSet<DpuItem> usedDpus = exportService.getDpusInformation(pipeline);

        Table table = new Table();
        table.addContainerProperty(Messages.getString("PipelineExport.dpu.template"), String.class, null);
        table.addContainerProperty(Messages.getString("PipelineExport.dpu.jarName"), String.class, null);
        table.addContainerProperty(Messages.getString("PipelineExport.dpu.version"), String.class, null);
        table.setWidth("100%");
        table.setHeight("130px");
        //add dpu's information to table
        for (DpuItem entry : usedDpus) {
            table.addItem(new Object[] { entry.getDpuName(), entry.getJarName(), entry.getVersion() }, null);
        }

        panel.setContent(table);
        usedJarsLayout.addComponent(panel);

        final HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");
        Button btnExport = new Button(Messages.getString("PipelineExport.export"));
        buttonLayout.addComponent(btnExport);
        buttonLayout.setComponentAlignment(btnExport, Alignment.MIDDLE_LEFT);
        Button btnCancel = new Button(Messages.getString("PipelineExport.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        buttonLayout.addComponent(btnCancel);
        buttonLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_RIGHT);

        // add to the main layout
        mainLayout.addComponent(detailLayout);
        mainLayout.addComponent(usedJarsLayout);
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
        setContent(mainLayout);

        FileDownloader fileDownloader = new OnDemandFileDownloader(new OnDemandStreamResource() {

            @Override
            public String getFilename() {
                return pipeline.getName() + ".zip";
            }

            @Override
            public InputStream getStream() {
                ExportSetting setting = new ExportSetting(chbExportDPUData.getValue(), chbExportJars.getValue(), chbExportSchedule.getValue());
                LOG.debug("Exporting DPU date: {}", setting.isExportDPUUserData());
                LOG.debug("Exporting DPU's jars: {}", setting.isExportJars());
                LOG.debug("Exporting DPU's schedule: {}", setting.isChbExportSchedule());

                LOG.debug("Constructing output stream.");
                File pplFile;
                try {
                    pplFile = exportService.exportPipeline(pipeline, setting);
                } catch (ExportException ex) {
                    LOG.error("Faield to export pipeline", ex);
                    Notification.show(Messages.getString("PipelineExport.export.fail"), Notification.Type.ERROR_MESSAGE);
                    return null;
                }
                try {
                    return new FileInputStream(pplFile);
                } catch (FileNotFoundException ex) {
                    LOG.error("Faield to load file with pipeline", ex);
                    Notification.show(Messages.getString("PipelineExport.export.fail2"), Notification.Type.ERROR_MESSAGE);
                    return null;
                }
            }

        });
        fileDownloader.extend(btnExport);
    }

    /**
     * Check for permission.
     * 
     * @param type
     *            Required permission.
     * @return If the user has given permission
     */
    public boolean hasPermission(String type) {
        for (GrantedAuthority ga : authCtx.getUser().getAuthorities()) {
            if (type.equals(ga.getAuthority()))
                return true;
        }
        return false;
    }

}

package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

/**
 * Configuration class for {@link ExportService}.
 * 
 * @author Škoda Petr
 */
public class ExportSetting {

    private boolean exportDPUUserData;

    private boolean exportJars;

    private boolean chbExportSchedule;

    public ExportSetting(boolean exportDPUUserData, boolean exportJars, boolean chbExportSchedule) {
        this.exportDPUUserData = exportDPUUserData;
        this.exportJars = exportJars;
        this.chbExportSchedule = chbExportSchedule;
    }

    public boolean isExportDPUUserData() {
        return exportDPUUserData;
    }

    public void setExportDPUUserData(boolean exportDPUUserData) {
        this.exportDPUUserData = exportDPUUserData;
    }

    public boolean isExportJars() {
        return exportJars;
    }

    public void setExportJars(boolean exportJars) {
        this.exportJars = exportJars;
    }

    public boolean isChbExportSchedule() {
        return chbExportSchedule;
    }

    public void setChbExportSchedule(boolean chbExportSchedule) {
        this.chbExportSchedule = chbExportSchedule;
    }
}

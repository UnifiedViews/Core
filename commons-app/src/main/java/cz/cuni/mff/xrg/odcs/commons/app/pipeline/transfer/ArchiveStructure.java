package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

/**
 * @author Škoda Petr
 */
public enum ArchiveStructure {
    PIPELINE("pipeline.xml"),
    SCHEDULE("schedule.xml"),
    DPU_TEMPLATE("dpu.xml"),
    DPU_JAR("dpu_jar"),
    DPU_DATA_GLOBAL("dpu_data_global"),
    DPU_DATA_USER("dpu_data_user"),
    USED_DPUS("used_dpu.xml");

    private final String value;

    private ArchiveStructure(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

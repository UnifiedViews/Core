package cz.cuni.mff.xrg.odcs.commons.app.constants;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * The enum contains length limits based on database schema limitations.
 * 
 * @author Petyr
 * @author michal.klempa@eea.sk
 */
public class LenghtLimits {
    /**
     * Max length of DPU name. @see {@link DPURecord#name}
     */
    public static int DPU_NAME = 1024;

    /**
     * @see {@link DPUInstanceRecord#toolTip}
     */
    public static int DPU_TOOL_TIP = 512;

    /**
     * @see {@link DPUTemplateRecord#jarDirectory}
     */
    public static int DPU_JAR_DIRECTORY = 255;

    /**
     * @see {@link DPUTemplateRecord#jarName}
     */
    public static int DPU_JAR_NAME = 255;

    /**
     * @see {@link DPUTemplateRecord#jarName}
     */
    public static int DPU_JAR_DESCRIPTION = 1024;

    /**
     * @see {@link MessageRecord#shortMessage}
     */
    public static int SHORT_MESSAGE = 128;

    /**
     * Max length of pipeline name.
     */
    public static int PIPELINE_NAME = 1024;

    /**
     * @see {@link User#username}
     */
    public static final int USER_NAME = 25;

    /**
     * @see {@link User#fullName}
     */
    public static final int USER_FULLNAME = 55;

    /**
     * @see {@link NamespacePrefix#name}
     */
    public static final int NAMESPACE_PREFIX_NAME = 25;

    /**
     * @see {@link NamespacePrefix#prefixURI}
     */
    public static final int NAMESPACE_PREFIX_URI = 255;

    public static int SCHEDULE_NAME = 1024;

    public static int LOGGER_NAME = 254;
    
    public static int RUNTIME_PROPERTY_NAME_AND_VALUE = 100;
}

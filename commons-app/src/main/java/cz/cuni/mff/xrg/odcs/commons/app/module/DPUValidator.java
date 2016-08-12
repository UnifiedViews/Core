package cz.cuni.mff.xrg.odcs.commons.app.module;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;

/**
 * Can be used to add additional checks for loaded DPU. The validation class
 * will not be shared by multiple threads.
 * 
 * @author Petyr
 * @see DPUModuleManipulator
 */
public interface DPUValidator {

    /**
     * Check if {@link DPUTemplateRecord} and it's instance is functional and
     * can be used. If the DPU is invalid then should throw exception
     * with message for user.
     * 
     * @param dpu
     *            Data object that represent DPU to validate.
     * @param dpuInstance
     *            Instance of DPU to validate.
     * @throws DPUValidatorException
     */
    public void validate(DPUTemplateRecord dpu, Object dpuInstance)
            throws DPUValidatorException;

}

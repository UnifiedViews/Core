package cz.cuni.mff.xrg.odcs.frontend.dpu.validator;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.data.DataUnitDescription;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUValidator;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUValidatorException;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Check for duplicity in names of input and output data units.
 * 
 * @author Škoda Petr
 */
@Component
class DataUnitNamesValidator implements DPUValidator {

    @Autowired
    private DPUExplorer explorer;

    @Override
    public void validate(DPUTemplateRecord dpu, Object dpuInstance) throws DPUValidatorException {
        check(explorer.getInputs(dpu));
        check(explorer.getOutputs(dpu));
    }

    /**
     * Check given list for duplicity names, if there are some then throws an
     * exception.
     * 
     * @param dataUnits
     * @throws DPUValidatorException
     */
    private void check(List<DataUnitDescription> dataUnits) throws DPUValidatorException {
        HashSet<String> names = new HashSet<>();
        for (DataUnitDescription desc : dataUnits) {
            if (names.contains(desc.getName())) {
                // name collision
                throw new DPUValidatorException(Messages.getString("DataUnitNamesValidator.same.dataunit", desc.getName()));
            } else {
                names.add(desc.getName());
            }
        }
    }

}

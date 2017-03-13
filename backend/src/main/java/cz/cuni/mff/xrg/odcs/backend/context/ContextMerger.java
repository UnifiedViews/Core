package cz.cuni.mff.xrg.odcs.backend.context;

import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeInstructions;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Provide functionality to merge (add) one {@link Context} into another.
 * 
 * @author Petyr
 */
class ContextMerger {

    private static final Logger LOG = LoggerFactory
            .getLogger(ContextMerger.class);

    /**
     * Add data from source {@link Context} into target {@link Context}.
     * 
     * @param target
     * @param source
     * @param instruction
     *            Instructions that should be used for merging.
     */
    public void merge(Context target, Context source, String instruction)
            throws ContextException {
        // merge dataUnits
        merger(target.getInputsManager(), source.getOutputs(), instruction, target);
    }

    /**
     * Search for first command that can be applied to the DataUnit with given
     * name.
     * 
     * @param dataUnitName
     *            DataUnit's name.
     * @param instruction
     * @return Command or empty string.
     */
    private String findRule(String dataUnitName, String instruction) {
        // check for null
        if (instruction == null) {
            return "";
        }

        String[] rules = instruction.split(EdgeInstructions.Separator
                .getValue());
        for (String item : rules) {
            String[] elements = item.split(" ", 2);
            // test name ..
            if (elements.length < 2) {
                // not enough data .. skip
            } else { // elements.length == 2
                if (elements[0].compareToIgnoreCase(dataUnitName) == 0) {
                    // match !!
                    return elements[1];
                }
            }
        }
        return "";
    }

    /**
     * Merge the data from targets into sources. If the two Lists of DataUnits
     * can't be merge throw ContextException.
     * 
     * @param target
     *            Target {@link DataUnitManager}.
     * @param sources
     *            Source of DataUnits, do not change!
     * @param instruction
     *            Instruction for merger.
     * @param targetContext
     *            Context of the target DPU (so that the flags for optimistic mode may be prepared)
     * @throw ContextException
     */
    private void merger(DataUnitManager target, List<ManagableDataUnit> sources,
            String instruction, Context targetContext) throws ContextException {
        Iterator<ManagableDataUnit> iterSource = sources.iterator();

        // add the rest from source
        while (iterSource.hasNext()) {
            ManagableDataUnit source = iterSource.next();
            String sourceDataUnitName = source.getName();
            String expectedTargetDataUnitName;

            // STEP 1: Get the mapping command from the EDGE (e.g. that "output123" is mapped to "input123"
            String cmd = this.findRule(sourceDataUnitName, instruction);
            if (cmd.isEmpty()) {
                // there is no mapping
                // IGNORE DATAUNIT
                LOG.debug("{} ignored.", sourceDataUnitName);
                continue;
            } else {
                String[] cmdSplit = cmd.split(" ");
                if (cmdSplit[0].compareToIgnoreCase(EdgeInstructions.Rename
                        .getValue()) == 0) {
                    // renaming .. we need second arg
                    if (cmdSplit.length == 2) {
                        expectedTargetDataUnitName = cmdSplit[1];
                        LOG.debug("renaming: {} -> {}", sourceDataUnitName, expectedTargetDataUnitName);
                    } else {
                        // not enough parameters .. use name of source
                        expectedTargetDataUnitName = sourceDataUnitName;
                        LOG.debug("passing: {}", sourceDataUnitName);
                    }
                } else {
                    // unknown command
                    LOG.error("dataUnit droped bacause of unknown command: {}",
                            cmd);
                    continue;
                }
            }

            // STEP 2: Check whether the target data unit does not already exist
            ManagableDataUnit targetDataUnit = null;
            // first check for existing one
            for (ManagableDataUnit targetDataUnitCandidate : target.getDataUnits()) {
                if (targetDataUnitCandidate.getName().compareTo(expectedTargetDataUnitName) == 0
                        && targetDataUnitCandidate.getType() == source.getType()) {
                    LOG.debug("merge into existing dataUnit: {}",
                            expectedTargetDataUnitName);
                    // DataUnit with same name and type already exist, use it
                    targetDataUnit = targetDataUnitCandidate;
                    break;
                }
            }

            // STEP 2b: The target data unit does not exist, create new data unit (in context into which we merge)
            if (targetDataUnit == null) {
                LOG.debug("creating new dataUnit: {}", expectedTargetDataUnitName);
                try {
                    targetDataUnit = target.addDataUnit(source.getType(), expectedTargetDataUnitName);
                } catch (DataUnitException ex) {
                    throw new ContextException(ex);
                }
                // and clear it .. for sure that there is 
                // not data from previous executions
                //TODO Optimistic mode - do not clear!
                try {
                    targetDataUnit.clear();
                } catch (DataUnitException ex) {
                    throw new ContextException("Can't clear new data unit.", ex);
                }
            }

            // STEP 3 copy the data to target data unit
            try {
                LOG.debug("Called {}.merge({})", targetDataUnit.getName(), source.getName());
                targetDataUnit.merge(source);
            } catch (IllegalArgumentException e) {
                throw new ContextException(
                        "Can't merge data units, type miss match.", e);
            } catch (Throwable t) {
                throw new ContextException("Can't merge data units.", t);
            }

            // set up flag that the data unit can/cannot be optimized
            if (source.isConsumedByMultipleInputs()) {
                //set that the data unit can be optimalized
                targetContext.addNonOptimalizableDataUnit(targetDataUnit);
            } else {
                //set that the data unit can be optimalized
                //do nothing
            }
        }
    }

}

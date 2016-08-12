package cz.cuni.mff.xrg.odcs.backend.context;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeInstructions;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Provide functionality to merge (add) one {@link Context} into another.
 * 
 * @author Petyr
 */
class ContextMerger {

    private static final Logger LOG = LoggerFactory
            .getLogger(ContextMerger.class);

    /**
     * Add data from right {@link Context} into left {@link Context}.
     * 
     * @param left
     * @param right
     * @param instruction
     *            Instructions that should be used for merging.
     */
    public void merge(Context left, Context right, String instruction)
            throws ContextException {
        // merge dataUnits
        merger(left.getInputsManager(), right.getOutputs(), instruction);
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
     *            Instruction for merger. See {@link cz.cuni.mff.xrg.odcs.commons.app.execution.DataUnitMergerInstructions}
     * @throw ContextException
     */
    private void merger(DataUnitManager target, List<ManagableDataUnit> sources,
            String instruction) throws ContextException {
        Iterator<ManagableDataUnit> iterSource = sources.iterator();

        // add the rest from right
        while (iterSource.hasNext()) {
            ManagableDataUnit source = iterSource.next();
            String sourceName = source.getName();
            String targetName;
            // get command
            String cmd = this.findRule(sourceName, instruction);
            if (cmd.isEmpty()) {
                // there is no mapping
                // IGNORE DATAUNIT
                LOG.debug("{} ignored.", sourceName);
                continue;
            } else {
                String[] cmdSplit = cmd.split(" ");
                if (cmdSplit[0].compareToIgnoreCase(EdgeInstructions.Rename
                        .getValue()) == 0) {
                    // renaming .. we need second arg
                    if (cmdSplit.length == 2) {
                        targetName = cmdSplit[1];
                        LOG.debug("renaming: {} -> {}", sourceName, targetName);
                    } else {
                        // not enough parameters .. use name of source
                        targetName = sourceName;
                        LOG.debug("passing: {}", sourceName);
                    }
                } else {
                    // unknown command
                    LOG.error("dataUnit droped bacause of unknown command: {}",
                            cmd);
                    continue;
                }
            }

            // we need dataUnit into which merge data
            ManagableDataUnit targetDataUnit = null;
            // first check for existing one
            for (ManagableDataUnit item : target.getDataUnits()) {
                if (item.getName().compareTo(targetName) == 0
                        && item.getType() == source.getType()) {
                    LOG.debug("merge into existing dataUnit: {}",
                            targetName);
                    // DataUnit with same name and type already exist, use it
                    targetDataUnit = item;
                    break;
                }
            }

            // create new data unit (in context into which we merge)
            if (targetDataUnit == null) {
                LOG.debug("creating new dataUnit: {}", sourceName);
                try {
                    targetDataUnit = target.addDataUnit(source.getType(), targetName);
                } catch (DataUnitException ex) {
                    throw new ContextException(ex);
                }
                // and clear it .. for sure that there is 
                // not data from previous executions
                try {
                    targetDataUnit.clear();
                } catch (DataUnitException ex) {
                    throw new ContextException("Can't clear new data unit.", ex);
                }
            }

            // and copy the data
            try {
                LOG.debug("Called {}.merge({})", targetDataUnit.getName(), source.getName());
                targetDataUnit.merge(source);
            } catch (IllegalArgumentException e) {
                throw new ContextException(
                        "Can't merge data units, type miss match.", e);
            } catch (Throwable t) {
                throw new ContextException("Can't merge data units.", t);
            }
        }
    }

}

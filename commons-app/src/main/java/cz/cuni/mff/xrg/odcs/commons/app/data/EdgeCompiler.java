package cz.cuni.mff.xrg.odcs.commons.app.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import cz.cuni.mff.xrg.odcs.commons.app.data.handlers.LogAndIgnore;
import cz.cuni.mff.xrg.odcs.commons.app.data.handlers.StoreInvalidMappings;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;

/**
 * Class provides functionality that enables to create script for the single
 * edge from list of DataUnit's names and list of indexes. Also the list of
 * indexes can be recreated from list of names and program.
 * 
 * @author Petyr
 */
public final class EdgeCompiler {

    private final static ErrorHandler DEFAULT_HANDLER = new LogAndIgnore();

    /**
     * Interface used as error handler.
     */
    public interface ErrorHandler {

        /**
         * Called if index of input data unit is out of range.
         */
        public void sourceIndexOutOfRange();

        /**
         * Called if index of output data unit is out of range.
         */
        public void targetIndexOutOfRange();

        /**
         * Called when there is unknown command in script.
         * 
         * @param item
         *            Unknown command.
         */
        public void unknownCommand(String item);

        /**
         * Called in case of invalid mapping.
         * 
         * @param item
         *            The invalid mapping.
         */
        public void invalidMapping(String item);

    }

    /**
     * Translate given mapping into string representation. Does not check for
     * validity of mapping.
     * 
     * @param mappings
     *            Pairs of mapping from sources to targets.
     * @param sources
     *            Sources data unit.
     * @param targets
     *            Targets data unit.
     * @param handler
     *            Can be null, in such case default handler is used.
     * @return mapping translation into string
     */
    public String translate(List<MutablePair<Integer, Integer>> mappings,
            List<DataUnitDescription> sources,
            List<DataUnitDescription> targets,
            ErrorHandler handler) {
        // check for error handler
        if (handler == null) {
            handler = DEFAULT_HANDLER;
        }

        final StringBuilder script = new StringBuilder();
        // add information about version into mapping
//		script.append("#1");
//		script.append(EdgeInstructions.Separator.getValue());

        if (mappings.isEmpty()) {
            // just run after mapping
            //script.append(EdgeInstructions.RunAfter.getValue());
            //return script.toString();
            return "";
        }

        // go through the sources
        for (MutablePair<Integer, Integer> mapping : mappings) {
            if (mapping.left < 0 && mapping.left >= sources.size()) {
                handler.sourceIndexOutOfRange();
                continue;
            }
            if (mapping.right < 0 && mapping.right >= targets.size()) {
                handler.targetIndexOutOfRange();
                continue;
            }
            final String source = sources.get(mapping.left).getName();
            final String target = targets.get(mapping.right).getName();
            // add to mapping
            script.append(source);
            script.append(' ');
            script.append(EdgeInstructions.Rename.getValue());
            script.append(' ');
            script.append(target);
            script.append(EdgeInstructions.Separator.getValue());
        }
        return script.toString();
    }

    /**
     * Parse given script into the mapping. Does not check for validity of
     * mapping.
     * 
     * @param script
     *            The script with mapping.
     * @param sources
     *            Sources data unit.
     * @param targets
     *            Targets data unit.
     * @param handler
     *            Handler to use during translation, can be null.
     * @return If return it's never null.
     */
    public List<MutablePair<Integer, Integer>> translate(String script,
            List<DataUnitDescription> sources,
            List<DataUnitDescription> targets,
            ErrorHandler handler) {
        // check for error handler
        if (handler == null) {
            handler = DEFAULT_HANDLER;
        }

        List<MutablePair<Integer, Integer>> mappings = new LinkedList<>();
        // if script is empty return
        if (script == null || script.isEmpty()) {
            return mappings;
        }
        // parse commands
        final String[] commands = script.split(EdgeInstructions.Separator.getValue());
        for (String item : commands) {
            if (item.startsWith("#")) {
                // version, does not care about it now
                continue;
            }

            final String[] cmd = item.split(" ");
            if (cmd.length != 3) {
                // other then rename -> ignore
                handler.unknownCommand(item);
                continue;
            }
            if (cmd[1].equalsIgnoreCase(EdgeInstructions.Rename.getValue())) {
                // rename action
            } else if (cmd[1].equalsIgnoreCase(EdgeInstructions.RunAfter
                    .getValue())) {
                // just run after command, has no special meaning
            } else {
                // other action -> ignore
                handler.unknownCommand(item);
                continue;
            }
            // get indexes
            final Integer sourceIndex = getIndex(sources, cmd[0]);
            final Integer targetIndex = getIndex(targets, cmd[2]);
            if (sourceIndex == null || targetIndex == null) {
                // mapping for no longer existing DataUnitDescription
                handler.invalidMapping(item);
                continue;
            }
            // add mapping
            MutablePair<Integer, Integer> mapping = new MutablePair<>();
            mapping.left = sourceIndex;
            mapping.right = targetIndex;
            mappings.add(mapping);
        }
        return mappings;
    }

    /**
     * Construct default mapping from one {@link DPUInstanceRecord} to another.
     * 
     * @param dpuExplorer
     * @param edge
     *            Edge to save mapping into.
     * @param from
     *            Source DPU.
     * @param to
     *            Target DPU.
     */
    public void createDefaultMapping(DPUExplorer dpuExplorer, Edge edge,
            DPUInstanceRecord from, DPUInstanceRecord to) {
        final List<DataUnitDescription> sourceList = dpuExplorer.getOutputs(from);
        final List<DataUnitDescription> targetList = dpuExplorer.getInputs(to);

        if (sourceList.size() == 1 && targetList.size() == 1) {
            final DataUnitDescription source = sourceList.get(0);
            final DataUnitDescription target = targetList.get(0);
            // Check for type.
            if (source.getTypeName().compareTo(target.getTypeName()) == 0) {
                final List<MutablePair<Integer, Integer>> mapping = new ArrayList<>();
                mapping.add(new MutablePair<>(0, 0));
                edge.setScript(translate(mapping, sourceList, targetList, null));
            } else {
                edge.setScript("");
            }
        } else if (sourceList.isEmpty() || targetList.isEmpty()) {
            // no mapping
            final List<MutablePair<Integer, Integer>> mapping = new ArrayList<>();
            edge.setScript(translate(mapping, sourceList, targetList, null));
        } else {
            edge.setScript("");
        }
    }

    /**
     * @return Script that represents run-after edge.
     */
    public String createRunAfterMapping() {
        final StringBuilder script = new StringBuilder();
        script.append(EdgeInstructions.RunAfter.getValue());
        return script.toString();
    }

    /**
     * @param script
     * @return True if given script represents run-after edge, false otherwise.
     */
    public boolean isRunAfter(String script) {
        return script.contains(EdgeInstructions.RunAfter.getValue());
    }

    /**
     * Return index of first {@link DataUnitDescription} with given name or null
     * if there is no {@link DataUnitDescription} with required name.
     * 
     * @param data
     *            List of data units descriptions.
     * @param name
     *            Name of required data unit.
     * @return Index of to data or null.
     */
    private Integer getIndex(List<DataUnitDescription> data, String name) {
        int index = 0;
        for (DataUnitDescription item : data) {
            if (item.getName().compareTo(name) == 0) {
                return index;
            }
            ++index;
        }
        return null;
    }

    /**
     * @param graph
     *            Graph to check.
     * @param explorer
     * @return validation report as string
     * @deprecated Use {@link cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineValidator} instead
     */
    public String checkMandatoryInputsAndOutputs(PipelineGraph graph,
            DPUExplorer explorer) {
        String report = "";
        for (Node node : graph.getNodes()) {
            DPUInstanceRecord dpu = node.getDpuInstance();
            List<Edge> edgesTo = null;
            List<DataUnitDescription> inputs = explorer.getInputs(dpu);
            if (!inputs.isEmpty()) {
                edgesTo = graph.getEdgesTo(node);
            }
            for (DataUnitDescription input : inputs) {
                boolean found = false;
                if (input.getOptional()) {
                    continue;
                }
                for (Edge e : edgesTo) {
                    if (e.getScript().contains("-> " + input.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    report += "\n";
                    report += Messages.getString("EdgeCompiler.dpu.input", dpu
                            .getName(), input.getName());
                }
            }
            List<DataUnitDescription> outputs = explorer.getOutputs(dpu);
            List<Edge> edgesFrom = null;
            if (!outputs.isEmpty()) {
                edgesFrom = graph.getEdgesFrom(node);
            }
            for (DataUnitDescription output : outputs) {
                boolean found = false;
                if (output.getOptional()) {
                    continue;
                }
                for (Edge e : edgesFrom) {
                    if (e.getScript().contains(output.getName() + " ->")) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    report += "\n";
                    report += Messages.getString("EdgeCompiler.dpu.output", dpu
                            .getName(), output.getName());
                }
            }
        }
        if (report.isEmpty()) {
            return null;
        } else {
            return report;
        }
    }

    /**
     * Update script and remove invalid mappings.
     * 
     * @param edge
     *            Edge which script update.
     * @param sources
     *            List of sources data units descriptions
     * @param targets
     *            List of targets data units descriptions
     * @return List of invalid mappings.
     */
    public List<String> update(Edge edge,
            List<DataUnitDescription> sources,
            List<DataUnitDescription> targets) {

        StoreInvalidMappings handler = new StoreInvalidMappings();

        List<MutablePair<Integer, Integer>> mapping = translate(edge.getScript(), sources, targets, handler);
        if (handler.getInvalidMapping().isEmpty()) {
            // all is ok
            return new LinkedList<>();
        }

        String newScript = translate(mapping, sources, targets, null);
        edge.setScript(newScript);
        return handler.getInvalidMapping();
    }

}

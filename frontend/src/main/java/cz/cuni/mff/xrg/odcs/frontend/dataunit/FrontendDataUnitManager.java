package cz.cuni.mff.xrg.odcs.frontend.dataunit;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitFactory;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DpuContextInfo;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.ExecutionInfo;
import cz.cuni.mff.xrg.odcs.commons.app.rdf.RepositoryManager;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.rdf.repositories.GraphUrl;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.rdf.repository.RDFException;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;

/**
 * Class is designed to provide access to DataUnits for Frontend.
 * It's also responsible for closing of opened DataUnits after some time. TODO Implement better solution then
 * time-based closing?
 *
 * @author Škoda Petr
 */
public class FrontendDataUnitManager {

    private static class DataUnitHolder {

        private final ManagableDataUnit dataUnit;

        /**
         * Time when the underling DataUnit should be closed.
         */
        private long closeTime = 0l;

        private final Long executionId;

        public DataUnitHolder(ManagableDataUnit dataUnit, Long executionId) {
            this.dataUnit = dataUnit;
            this.executionId = executionId;
        }

        public ManagableDataUnit getDataUnit() {
            return dataUnit;
        }

        public Long getLastAccess() {
            return closeTime;
        }

        public Long getExecutionId() {
            return executionId;
        }

        public void setCloseTime(Long timeFromNow) {
            // Close time can be set to 1h and then someone ask for 15minutes, we should stick
            // with the 1h close time.
            long newCloseTime = (new Date()).getTime() + timeFromNow;
            if (newCloseTime > closeTime) {
                closeTime = newCloseTime;
            }
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(FrontendDataUnitManager.class);

    /**
     * Short period, use for data browsing etc.
     */
    public static final long CLOSE_TIME_SHORT = 10 * 60 * 1000;

    /**
     * Longer period use for operation up to one hour.
     */
    public static final long CLOSE_TIME_LONG = 60 * 60 * 1000;

    @Autowired
    private DataUnitFactory dataUnitFactory;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private RepositoryManager repositoryManager;

    /**
     * Cache for opened DataUnits.
     */
    private final Map<String, DataUnitHolder> dataUnits = Collections.synchronizedMap(new HashMap<String, DataUnitHolder>());

    /**
     * For each execution store number of opened DataUnits. If number of open DataUnits reach
     * zero then repository for given execution could be closed.
     */
    private final Map<Long, Integer> openedForExecution = new HashMap<>();

    public synchronized ManageableWritableRDFDataUnit getRDFDataUnit(ExecutionInfo executionInfo, DPUInstanceRecord dpuInstance, DataUnitInfo dataUnitInfo, long timeToClose) {
        // Some basic parameters check.
        if (dataUnitInfo == null) {
            return null;
        }
        if (executionInfo == null) {
            LOG.error("ExecutionInfo is null!");
            return null;
        }
        final DpuContextInfo dpuInfo = executionInfo.dpu(dpuInstance);
        if (dpuInfo == null) {
            LOG.error("DPU info is null!");
            return null;
        }
        // Prepara identifications.
        final String dataUnitId = executionInfo.getExecutionContext().generateDataUnitId(dpuInstance,
                dataUnitInfo.getIndex());
        final File directory;
        try {
            directory = resourceManager.getDataUnitWorkingDir(executionInfo.getExecutionContext()
                    .getExecution(), dpuInstance, dataUnitInfo.getIndex());
        } catch (MissingResourceException ex) {
            LOG.error("Missing resource.", ex);
            throw new RuntimeException(Messages.getString("FrontendDataUnitManager.resource"), ex);
        }
        final String directoryStr = directory.toString();
        // Check in cache use directory for identification.
        synchronized (dataUnits) {
            if (dataUnits.containsKey(directoryStr)) {
                final DataUnitHolder holder = dataUnits.get(directoryStr);
                holder.setCloseTime(timeToClose);
                return (ManageableWritableRDFDataUnit) holder.getDataUnit();
            }
        }
        // We need to create a new one.
        final ManagableDataUnit dataUnit;
        try {
            dataUnit = dataUnitFactory.create(ManagableDataUnit.Type.RDF,
                    executionInfo.getExecutionContext().getExecutionId(),
                    GraphUrl.translateDataUnitId(dataUnitId),
                    dataUnitInfo.getName(),
                    directory);
        } catch (DataUnitException | RDFException ex) {
            LOG.error("Can't create DataUnit.", ex);
            throw new RuntimeException(Messages.getString("FrontendDataUnitManager.dataUnit.create"), ex);
        }

        try {
            dataUnit.load();
        } catch (DataUnitException ex) {
            LOG.error("Can't load DataUnit.", ex);
            try {
                dataUnit.release();
            } catch (DataUnitException e) {
                LOG.warn("Can't close DataUnit.", e);
            }
            throw new RuntimeException(Messages.getString("FrontendDataUnitManager.dataUnit.load"), ex);
        }
        LOG.info("DataUnit has been created for directory '{}'", directoryStr);
        // Add to cache.
        final DataUnitHolder holder = new DataUnitHolder(dataUnit, executionInfo.getExecutionContext().getExecutionId());
        holder.setCloseTime(timeToClose);
        dataUnits.put(directoryStr, holder);
        incRepoCounter(holder);
        return (ManageableWritableRDFDataUnit) dataUnit;
    }

    /**
     * Release and close all opened repositories. Should be called when application is closing.
     */
    @PreDestroy
    private void closeAll() {
        LOG.info("Releasing all opened repositories...");
        synchronized (dataUnits) {
            // Release dataUnits.
            for (String directoryStr : dataUnits.keySet()) {
                final DataUnitHolder holder = dataUnits.get(directoryStr);
                try {
                    holder.getDataUnit().release();
                } catch (DataUnitException ex) {
                    LOG.warn("Can't release DataUnit.", ex);
                }
            }
            // Release all repositories.
            for (Long executionId : openedForExecution.keySet()) {
                try {
                    repositoryManager.release(executionId);
                } catch (RDFException ex) {
                    LOG.warn("Can't release repository for execution: {}", executionId, ex);
                }
            }
            // Empty lists.
            dataUnits.clear();
            openedForExecution.clear();
        }
        LOG.info("Releasing all opened repositories...done");
    }

    private void incRepoCounter(DataUnitHolder holder) {
        int value = 0;
        if (openedForExecution.containsKey(holder.getExecutionId())) {
            value = openedForExecution.get(holder.getExecutionId());
        }
        openedForExecution.put(holder.getExecutionId(), ++value);
    }

    private void decRepoCounter(DataUnitHolder holder) {
        int value = 1;
        if (openedForExecution.containsKey(holder.getExecutionId())) {
            value = openedForExecution.get(holder.getExecutionId());
        } else {
            LOG.warn("Missing value for: {}", holder.getExecutionId());
        }
        openedForExecution.put(holder.getExecutionId(), --value);
    }

    /**
     * Checks for opened DataUnit and close those whose are not used for 30 minutes.
     */
    @Scheduled(fixedDelay = 7 * 60 * 1000)
    private void closeOpened() {
        final long now = (new Date()).getTime();
        synchronized (dataUnits) {
            final List<String> toDelete = new LinkedList<>();

            for (String directoryStr : dataUnits.keySet()) {
                final DataUnitHolder holder = dataUnits.get(directoryStr);
                // Check for open times.
                if (now > holder.getLastAccess()) {
                    LOG.info("DataUnit in directory '{}' should be deleted.", directoryStr);
                    // Add to delte list.
                    toDelete.add(directoryStr);
                }
            }
            // Delete records.
            for (String directoryStr : toDelete) {
                final DataUnitHolder holder = dataUnits.get(directoryStr);
                decRepoCounter(holder);
                try {
                    holder.getDataUnit().release();
                } catch (DataUnitException ex) {
                    LOG.warn("Can't release DataUnit.", ex);
                }
                dataUnits.remove(directoryStr);
            }
            // Check for Repositories to close.
            boolean scanForNext = true;
            while (scanForNext) {
                scanForNext = false;
                for (Long executionId : openedForExecution.keySet()) {
                    if (openedForExecution.get(executionId) == 0) {
                        LOG.info("Closing repository for execution: {}", executionId);
                        // Close and remove.
                        openedForExecution.remove(executionId);
                        try {
                            repositoryManager.release(executionId);
                        } catch (RDFException ex) {
                            LOG.warn("Can't release repository for execution: {}", executionId, ex);
                        }
                        // And search for next.
                        scanForNext = true;
                        break;
                    }
                }
            }
        }
    }

}

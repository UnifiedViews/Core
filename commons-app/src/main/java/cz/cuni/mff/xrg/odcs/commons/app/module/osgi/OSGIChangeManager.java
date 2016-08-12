package cz.cuni.mff.xrg.odcs.commons.app.module.osgi;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DbDPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleDeleteEvent;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleEvent;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleNewEvent;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleUpdateEvent;

/**
 * As component receive {@link ModuleEvent} and react on them by calling methods
 * on {@link ModuleFacade} can eventually call methods on {@link DPUModule} in
 * order to update data in database.
 * If the DPU file is just replace with new version and if {@link #refreshDatabase} is true. Then try to load DPU's description into
 * database.
 * Warning the {@link ModuleUpdateEvent} cause unloading of current DPU's jar
 * file and loading new. If the new file is wrong, there is no way back.
 * 
 * @author Petyr
 */
class OSGIChangeManager implements ApplicationListener<ModuleEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(OSGIChangeManager.class);

    @Autowired
    private ModuleFacade osgiModule;

    @Autowired
    private DbDPUTemplateRecord dpuTemplateDao;

    @Autowired
    private DPUExplorer dpuExplorer;

    /**
     * If true then the class try to update database when receive {@link ModuleUpdateEvent}.
     */
    private final boolean refreshDatabase;

    public OSGIChangeManager(boolean refreshDatabase) {
        this.refreshDatabase = refreshDatabase;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ModuleEvent event) {
        final String directory = event.getDirectoryName();
        if (event instanceof ModuleDeleteEvent) {
            LOG.debug("Unloading bubdle from: {}", directory);
            osgiModule.unLoad(directory);
        } else if (event instanceof ModuleNewEvent) {
            LOG.debug("Loading jar-file for new DPU in: {}", directory);
            // get record for DPU from database
            final DPUTemplateRecord dpu = dpuTemplateDao.getByDirectory(directory);
            if (dpu == null) {
                LOG.warn("Missing record for new DPU in directory: ", directory);
            } else {
                // pre-load
                osgiModule.preLoadDPUs(Arrays.asList(dpu));
            }
        } else if (event instanceof ModuleUpdateEvent) {
            LOG.debug("Udating DPU in: {}", directory);
            final DPUTemplateRecord dpu = dpuTemplateDao.getByDirectory(directory);
            final ModuleUpdateEvent updateEvent = (ModuleUpdateEvent) event;
            if (dpu == null) {
                LOG.warn("Missing record for updating DPU in directory: {}",
                        directory);
                // error, so just unload the current
                osgiModule.unLoad(directory);
                return;
            }

            // we try to replace the loaded DPU jar file
            osgiModule.beginUpdate(dpu);
            try {
                osgiModule.update(directory, updateEvent.getJarName());
                // ...
                osgiModule.endUpdate(dpu, false);
            } catch (ModuleException e) {
                // reload failed ..
                osgiModule.endUpdate(dpu, true);
                LOG.error("Failed to reload bundle on notificaiton request.", e);
                // end ..
                return;
            }

            // should we update database
            if (refreshDatabase) {
                // we require same name for DPU's jar file
                if (updateEvent.getJarName().compareTo(dpu.getJarName()) == 0) {
                    // jar name is the same, so update
                    refreshDatabase(dpu);
                }
            }
        }
    }

    /**
     * Refresh data in database that are loaded from DPU.
     * 
     * @param dpu
     */
    private void refreshDatabase(DPUTemplateRecord dpu) {
        LOG.debug("Updating database record for template id: {} name: {} ",
                dpu.getId(), dpu.getName());
        // update information loaded from DPU
        dpu.setJarDescription(dpuExplorer.getJarDescription(dpu));
        // save DPU
        dpuTemplateDao.save(dpu);
    }

}

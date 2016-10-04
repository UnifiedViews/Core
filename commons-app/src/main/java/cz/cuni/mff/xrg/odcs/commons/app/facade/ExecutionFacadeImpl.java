package cz.cuni.mff.xrg.odcs.commons.app.facade;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.execution.server.DbExecutionServer;
import cz.cuni.mff.xrg.odcs.commons.app.execution.server.ExecutionServer;

public class ExecutionFacadeImpl implements ExecutionFacade {

    private static Logger LOG = LoggerFactory.getLogger(ExecutionFacadeImpl.class);

    @Autowired
    private DbExecutionServer dbExecutionServer;

    @Autowired
    private AppConfig appConfig;

    private int backendAliveLimit;

    private static final int BACKEND_ALIVE_DEFAULT_LIMIT = 10;

    @PostConstruct
    public void init() {
        try {
            this.backendAliveLimit = this.appConfig.getInteger(ConfigProperty.BACKEND_ALIVE_LIMIT);
        } catch (MissingConfigPropertyException e) {
            this.backendAliveLimit = BACKEND_ALIVE_DEFAULT_LIMIT;
        }
    }

    @Override
    public ExecutionServer getExecutionServer(String backendId) {
        return this.dbExecutionServer.getExecutionServer(backendId);
    }

    @Override
    public boolean checkAnyBackendActive() {
        boolean alive = false;
        List<ExecutionServer> backends = getAllExecutionServers();

        if (backends == null || backends.isEmpty()) {
            LOG.debug("No backend has ever run with this system");
            return false;
        }

        Long limitDateTime = System.currentTimeMillis() - (this.backendAliveLimit * 1000);
        Date limitDate = new Date(limitDateTime);

        for (ExecutionServer backend : backends) {
            if (backend.getLastUpdate().after(limitDate)) {
                alive = true;
            }
        }
        if (!alive) {
            LOG.debug("No backend has been active for at least {}s.", this.backendAliveLimit);
        }

        return alive;
    }

    @Override
    public List<ExecutionServer> getAllExecutionServers() {
        return this.dbExecutionServer.getAllExecutionServers();
    }

    @Override
    @Transactional
    public void updateBackendTimestamp(String backendId) {
        ExecutionServer backend = getExecutionServer(backendId);
        if (backend != null) {
            backend.setLastUpdate(new Date());
        } else {
            backend = new ExecutionServer();
            backend.setBackendId(backendId);
            backend.setLastUpdate(new Date());
        }

        this.dbExecutionServer.save(backend);
    }

    @Override
    public int allocateQueuedExecutionsForBackend(String backendID, int limit) {
        return this.dbExecutionServer.allocateQueuedExecutionsForBackendByPriority(backendID, limit);
    }

    @Override
    public long getCountOfUnallocatedQueuedExecutionsWithIgnorePriority() {
        return this.dbExecutionServer.getCountOfUnallocatedQueuedExecutionsWithIgnorePriority();
    }

}

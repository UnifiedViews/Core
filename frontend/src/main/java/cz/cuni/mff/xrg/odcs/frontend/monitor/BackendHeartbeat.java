/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.frontend.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import cz.cuni.mff.xrg.odcs.commons.app.communication.HeartbeatService;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ExecutionFacade;

/**
 * Periodically checks Backend status. As singleton component should prevent
 * multiple queries for backend status.
 * 
 * @author Škoda Petr
 */
public class BackendHeartbeat {

    @Autowired
    private ExecutionFacade executionFacade;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private HeartbeatService heartbeatService;

    private boolean backendClusterMode = false;

    /**
     * True if backend is alive.
     */
    private boolean alive = false;

    @Scheduled(fixedDelay = 6 * 1000)
    private void check() {
        try {
            if (this.backendClusterMode) {
                this.alive = this.executionFacade.checkAnyBackendActive();
            } else {
                this.alive = this.heartbeatService.isAlive();
            }
        } catch (Exception ex) {
            this.alive = false;
        }
    }

    public boolean checkIsAlive() {
        return this.alive;
    }

    @PostConstruct
    public void init() {
        try {
            this.backendClusterMode = this.appConfig.getBoolean(ConfigProperty.BACKEND_CLUSTER_MODE);
        } catch (MissingConfigPropertyException e) {
            // ignore
        }
    }

}

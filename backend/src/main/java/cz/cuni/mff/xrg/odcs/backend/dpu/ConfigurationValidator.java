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
package cz.cuni.mff.xrg.odcs.backend.dpu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import cz.cuni.mff.xrg.odcs.commons.app.communication.EmailSender;

/**
 * As a component periodically ask database for DPUs with possibly invalid
 * configuration.
 * At the end of the check round .. send messages to the owners of DPUs
 * which has invalid configuration.
 * Not implemented so far.
 * 
 * @author Petyr
 */
public class ConfigurationValidator implements ApplicationListener<ApplicationEvent> {

    /**
     * Component used to send email.
     */
    @Autowired
    private EmailSender emailSender;

    /**
     * Facade for access to DPUs.
     */
    @Autowired
    private cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade dpuFacade;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }

}

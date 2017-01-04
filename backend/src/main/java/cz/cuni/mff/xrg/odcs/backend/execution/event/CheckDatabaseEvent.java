package cz.cuni.mff.xrg.odcs.backend.execution.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event represent request on engine to check database for new
 * queued executions.
 * 
 * @author Petyr
 */
public class CheckDatabaseEvent extends ApplicationEvent {

    public CheckDatabaseEvent(Object source) {
        super(source);
    }

}

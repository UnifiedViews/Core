package cz.cuni.mff.xrg.odcs.backend.spring;

import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Store published event in in-memory storage. Should be used during
 * testing in order to monitor events. Provide access to
 * all published event by {@link #getEventList()}.
 * 
 * @author Petyr
 */
public class InMemoryEventListener implements ApplicationListener<ApplicationEvent> {

    /**
     * Store application events.
     */
    private final List<ApplicationEvent> eventList = new LinkedList<>();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        eventList.add(event);
    }

    public List<ApplicationEvent> getEventList() {
        return eventList;
    }

}

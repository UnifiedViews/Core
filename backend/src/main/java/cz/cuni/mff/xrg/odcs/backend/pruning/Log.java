package cz.cuni.mff.xrg.odcs.backend.pruning;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.execution.log.DbLogRead;

/**
 * Component for logs pruning based on settings provided in configuration file.
 * 
 * @author Petyr
 */
@Component
class Log {

    @Autowired
    private DbLogRead dbLog;

    @Autowired
    private AppConfig config;

    /**
     * If set then delete (prune) old logs.
     * Spring will run this at every midnight.
     */
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    private void execute() {

        // get user settings
        Integer history = null;
        try {
            history = config.getInteger(ConfigProperty.EXECUTION_LOG_HISTORY);
        } finally {
        }

        if (history == null || history <= -1) {
            // do not prune .. 
            return;
        }

        Calendar pruneLine = Calendar.getInstance();
        pruneLine.setTime(new Date());
        // move to the past
        pruneLine.add(Calendar.DAY_OF_YEAR, -history);

        // call delete
        dbLog.prune(pruneLine.getTime());

    }

}

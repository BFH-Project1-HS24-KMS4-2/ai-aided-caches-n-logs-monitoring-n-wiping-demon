
package ch.bfh.tracesentry.daemon.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
    }
}

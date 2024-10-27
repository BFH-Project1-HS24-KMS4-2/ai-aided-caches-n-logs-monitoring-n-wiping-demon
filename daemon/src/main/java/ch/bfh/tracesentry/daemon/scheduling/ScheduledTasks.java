
package ch.bfh.tracesentry.daemon.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        String filename = "current_time.txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Aktuelle Zeit: " + LocalDateTime.now());
            LOG.info("Aktuelle Zeit in Datei geschrieben.");
        } catch (IOException e) {
            LOG.error("Fehler beim Schreiben in die Datei.");
        }
    }
}

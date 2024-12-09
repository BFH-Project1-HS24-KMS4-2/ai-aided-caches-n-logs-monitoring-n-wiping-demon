package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class EvaluationServiceTempTest {

    @Autowired
    private EvaluationService evaluationDomainService;

    @Test
    void requestTest() {
        var files = List.of(
                "C:\\Users\\Luca\\.azure\\logs\\telemetry.log",
                "C:\\Users\\Luca\\AppData\\Local\\Discord\\Discord_updater_r00003.log",
                "C:\\Users\\Luca\\Ubiquiti UniFi\\logs\\server.log"
        );
        files.forEach(file -> {
            System.out.println(evaluationDomainService.evaluate(file));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}

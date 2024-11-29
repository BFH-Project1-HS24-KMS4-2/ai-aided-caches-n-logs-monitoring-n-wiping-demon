package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.EvaluationDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class EvaluationServiceTempTest {

    @Test
    void requestTest() {
        EvaluationDomainService.evaluate();
    }
}

package ch.bfh.tracesentry.cli;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
public class CliApplicationTests {
    @Test
    void test() {
        assert(true);
    }

}
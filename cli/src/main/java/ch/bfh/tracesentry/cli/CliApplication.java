package ch.bfh.tracesentry.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CliApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(CliApplication.class, args);
        } catch (Exception ignored) {
            // this try catch block is necessary to prevent printing the stack trace in non-interactive mode
            System.exit(1);
        }
    }
}

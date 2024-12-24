package ch.bfh.tracesentry.cli.config;

import jakarta.validation.ConstraintViolation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.ParameterValidationException;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;


@Configuration
public class ShellValidationConfig {

    /**
     * Custom exception resolver for parameter validation exceptions.
     * This resolver pretty-prints the constraint violations.
     */
    static class CustomExceptionResolver implements CommandExceptionResolver {

        @Override
        public CommandHandlingResult resolve(Exception e) {
            if (e instanceof ParameterValidationException) {
                var violations = ((ParameterValidationException) e).getConstraintViolations();
                var message = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .reduce("", (a, b) -> a + b + "\n");
                return CommandHandlingResult.of(message);
            }
            return null;
        }
    }
    @Bean
    CustomExceptionResolver customExceptionResolver() {
        return new CustomExceptionResolver();
    }
}
package ch.bfh.tracesentry.cli.command.validation.validators;

import ch.bfh.tracesentry.cli.command.validation.annotations.ValidSearchMode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class SearchModeValidator implements ConstraintValidator<ValidSearchMode, String> {
    final String[] SUPPORTED_MODES = {"log", "cache", "full", "pattern"};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !Arrays.asList(SUPPORTED_MODES).contains(value)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("Invalid mode. Use: " + String.join(", ", SUPPORTED_MODES) + ".")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
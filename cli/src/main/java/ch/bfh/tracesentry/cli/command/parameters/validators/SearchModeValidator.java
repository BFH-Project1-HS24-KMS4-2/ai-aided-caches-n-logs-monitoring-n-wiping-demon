package ch.bfh.tracesentry.cli.command.parameters.validators;

import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.lib.model.SearchMode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class SearchModeValidator implements ConstraintValidator<ValidSearchMode, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> searchModes = Arrays.stream(SearchMode.values()).map(Enum::name).map(String::toLowerCase).toList();
        if (value == null || !searchModes.contains(value)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("Invalid mode. Use: " + String.join(", ", searchModes) + ".")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
package ch.bfh.tracesentry.cli.command.parameters.validators;

import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidPattern;
import ch.bfh.tracesentry.lib.model.SearchMode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternValidator implements ConstraintValidator<ValidPattern, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException e) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("Invalid value for option '--pattern'.")
                    .addConstraintViolation();
            return false;
        }
    }

    public static boolean isValidPatternOccurrence(String pattern, SearchMode searchMode) {
        if (searchMode == SearchMode.PATTERN) {
            if (pattern.isEmpty()) {
                throw new IllegalArgumentException("Option '--pattern' is required for mode 'pattern'.");
            }
            return true;
        } else {
            if (!pattern.isEmpty()) {
                throw new IllegalArgumentException("Option '--pattern' must not be set for mode '" + searchMode.toString().toLowerCase() + "'.");
            }
            return false;
        }
    }
}
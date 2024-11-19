package ch.bfh.tracesentry.cli.command.parameters.validators;

import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidRegex;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.cli.command.parameters.model.SearchMode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator implements ConstraintValidator<ValidRegex, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
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
}
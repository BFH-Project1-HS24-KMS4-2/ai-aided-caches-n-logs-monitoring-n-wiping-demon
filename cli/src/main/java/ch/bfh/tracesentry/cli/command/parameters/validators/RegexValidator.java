package ch.bfh.tracesentry.cli.command.parameters.validators;

import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidRegex;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator implements ConstraintValidator<ValidRegex, String> {
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
}
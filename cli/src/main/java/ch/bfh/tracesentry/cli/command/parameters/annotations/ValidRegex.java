package ch.bfh.tracesentry.cli.command.parameters.annotations;

import ch.bfh.tracesentry.cli.command.parameters.validators.RegexValidator;
import ch.bfh.tracesentry.cli.command.parameters.validators.SearchModeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating patterns.
 */
@Constraint(validatedBy = RegexValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegex {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
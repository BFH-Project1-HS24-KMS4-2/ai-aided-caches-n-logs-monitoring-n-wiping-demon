package ch.bfh.tracesentry.cli.command.parameters.annotations;

import ch.bfh.tracesentry.cli.command.parameters.validators.SearchModeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating search modes.
 * It's easier to customize the validation message with this annotation, instead of using the enum as a parameter type.
 */
@Constraint(validatedBy = SearchModeValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSearchMode {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
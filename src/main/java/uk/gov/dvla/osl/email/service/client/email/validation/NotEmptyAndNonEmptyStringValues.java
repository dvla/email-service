package uk.gov.dvla.osl.email.service.client.email.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which validates a List of strings against the following rules:
 *
 * - The List should not be empty.
 * - The List should contain non null and non empty string values.
 *
 */
@Documented
@Constraint(validatedBy = MandatoryStringListValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyAndNonEmptyStringValues {
    String message() default "Array empty or contains one or more empty values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

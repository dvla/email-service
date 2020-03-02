package uk.gov.dvla.osl.email.service.client.email.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class MandatoryStringListValidator implements ConstraintValidator<NotEmptyAndNonEmptyStringValues, List<String>> {

    @Override
    public boolean isValid(final List<String> values,
                           final ConstraintValidatorContext constraintValidatorContext) {
        if (values == null || values.isEmpty()) {
            return false;
        }

        final List<String> emptyStrings = values.stream()
                .filter(value -> (value == null || value.isEmpty()))
                .collect(Collectors.toList());

        return emptyStrings.isEmpty();
    }

}

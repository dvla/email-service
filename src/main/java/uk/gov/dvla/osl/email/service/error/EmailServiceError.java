package uk.gov.dvla.osl.email.service.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.dvla.osl.commons.error.api.ErrorCondition;

@AllArgsConstructor
public enum EmailServiceError implements ErrorCondition {

    UNEXPECTED_EXCEPTION_SEND_EMAIL_ERROR(99999, "Email could not be sent due to an unexpected exception."),
    INVALID_EMAIL_OBJECT_ERROR(99999, "Invalid email object."),
    MISSING_TEMPLATE_NAME_ERROR(99999, "htmlTemplateName or textTemplateName name must be supplied."),
    NOTIFICATIONS_TEMPLATE_ERROR(99999, "Both templates must be supplied for dvla-common-notifications."),
    UNEXPECTED_EMAIL_TEMPLATE_LOAD_ERROR(99999, "Email template could not be loaded due to an unexpected exception.");

    /**
     * The code for the error condition.
     */
    @Getter
    private Integer code;

    /**
     * The error message associated with the error condition.
     */
    @Getter
    private String message;
}

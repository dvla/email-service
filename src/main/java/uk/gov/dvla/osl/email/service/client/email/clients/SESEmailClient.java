package uk.gov.dvla.osl.email.service.client.email.clients;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.error.api.response.ErrorInfo;
import uk.gov.dvla.osl.commons.exception.ApplicationException;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;
import uk.gov.dvla.osl.email.service.error.EmailServiceError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controls the sending of emails via Amazon's SES (Simple Mail Service).
 */
@Slf4j
public class SESEmailClient implements EmailClient {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final EmailConfiguration emailConfiguration;

    public SESEmailClient(AmazonSimpleEmailService amazonSimpleEmailService, EmailConfiguration emailConfiguration) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
        this.emailConfiguration = emailConfiguration;
    }

    @Override
    public String sendEmail(Email email) {
        if (email == null) {
            log.error("Email object is null");
            throw new ApplicationException(new ErrorInfo(EmailServiceError.INVALID_EMAIL_OBJECT_ERROR));
        }

        try {
            SendEmailRequest sendEmailRequest = toSendEmailRequest(email);
            log.debug("In email amazonSimpleEmailService, about to send email to {}", email.getToAddresses());
            return amazonSimpleEmailService.sendEmail(sendEmailRequest).getMessageId();

        } catch (Exception ex) {
            log.error("Cannot send email to one or more recipients in this list {}. Exception message is : {}",
                    email.getToAddresses(), ex.getMessage(), ex);
            throw new ApplicationException(
                    new ErrorInfo(EmailServiceError.UNEXPECTED_EXCEPTION_SEND_EMAIL_ERROR), ex);
        }
    }

    private SendEmailRequest toSendEmailRequest(Email email) {

        if (email == null || StringUtils.isBlank(email.getFromAddress())
            || email.getToAddresses().isEmpty()) {
            throw new ApplicationException(
                    new ErrorInfo(EmailServiceError.INVALID_EMAIL_OBJECT_ERROR));
        }
        return new SendEmailRequest()
                .withSource(email.getFromAddress())
                .withDestination(buildDestination(email))
                .withMessage(buildMessage(email));
    }

    private Destination buildDestination(Email email) {

        List<String> ccAddresses = new ArrayList<>();
        email.getCcAddresses().ifPresent(ccAddresses::addAll);

        List<String> bccAddresses = new ArrayList<>();
        email.getBccAddresses().ifPresent(bccAddresses::addAll);

        return new Destination()
                .withToAddresses(email.getToAddresses())
                .withCcAddresses(ccAddresses)
                .withBccAddresses(bccAddresses);
    }

    private Message buildMessage(Email email) {
        Message message = new Message();
        Optional<String> subject = email.getSubject();
        if (subject.isPresent()) {
            message.withSubject(new Content(subject.get())).withBody(new Body());
        } else {
            message.withBody(new Body());
        }

        boolean isHtmlEmailRequired = email.getHtmlTemplateName().isPresent();
        boolean isTextEmailRequired = email.getTextTemplateName().isPresent();

        // We must have a text or html template, or both.
        if (!isHtmlEmailRequired && !isTextEmailRequired) {
            throw new ApplicationException(
                    new ErrorInfo(EmailServiceError.MISSING_TEMPLATE_NAME_ERROR));
        }

        Optional<String> htmlTemplate = email.getHtmlTemplateName();
        if (htmlTemplate.isPresent()) {
            String emailTemplate = loadEmailTemplate(htmlTemplate.get(), emailConfiguration);
            message.getBody().setHtml(new Content(populateTemplateWithDynamicData(emailTemplate, email)));
        }

        Optional<String> textTemplate = email.getTextTemplateName();
        if (textTemplate.isPresent()) {
            String emailTemplate = loadEmailTemplate(textTemplate.get(), emailConfiguration);
            message.getBody().setText(new Content(populateTemplateWithDynamicData(emailTemplate, email)));
        }

        return message;
    }

}

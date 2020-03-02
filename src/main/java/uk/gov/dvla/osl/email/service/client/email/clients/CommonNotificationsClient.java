package uk.gov.dvla.osl.email.service.client.email.clients;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.error.api.response.ErrorInfo;
import uk.gov.dvla.osl.commons.exception.ApplicationException;
import uk.gov.dvla.osl.email.service.CommonNotificationsConfig;
import uk.gov.dvla.osl.email.service.client.email.CommonNotificationsRetrofit;
import uk.gov.dvla.osl.email.service.client.email.entities.Message;
import uk.gov.dvla.osl.email.service.client.email.entities.MessageBody;
import uk.gov.dvla.osl.email.service.client.email.entities.Success;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;
import uk.gov.dvla.osl.email.service.error.EmailServiceError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controls the sending of emails via the dvla-common-notifications service
 */
@Slf4j
public class CommonNotificationsClient implements EmailClient {

    private final CommonNotificationsRetrofit commonNotificationsRetrofit;
    private final CommonNotificationsConfig commonNotificationsConfig;
    private final EmailConfiguration emailConfiguration;

    public CommonNotificationsClient(CommonNotificationsRetrofit retrofit,
                                     EmailConfiguration emailConfiguration,
                                     CommonNotificationsConfig commonNotificationsConfig) {
        this.commonNotificationsRetrofit = retrofit;
        this.emailConfiguration = emailConfiguration;
        this.commonNotificationsConfig = commonNotificationsConfig;
    }

    @Override
    public String sendEmail(Email email) {
        try {
            Message message = buildMessage(email);

            Response<Success> response = commonNotificationsRetrofit.sendEmailRequest(
                    message,
                    commonNotificationsConfig.getResourceHeader(),
                    commonNotificationsConfig.getOriginResourceHeader());

            return response.body().getEmailId();

        } catch (Exception ex) {
            throw new ApplicationException(
                    new ErrorInfo(EmailServiceError.UNEXPECTED_EXCEPTION_SEND_EMAIL_ERROR), ex
            );
        }
    }

    public Message buildMessage(Email email) {
        MessageBody messageBody = buildMessageBody(email);

        List<String> bccAddresses = new ArrayList<>();
        email.getBccAddresses().ifPresent(bccAddresses::addAll);

        List<String> ccAddresses = new ArrayList<>();
        email.getCcAddresses().ifPresent(ccAddresses::addAll);

        return Message.builder()
                .bcc(bccAddresses)
                .cc(ccAddresses)
                .from(email.getFromAddress())
                .subject(email.getSubject().orElse(""))
                .to(email.getToAddresses())
                .body(messageBody)
                .build();
    }

    public MessageBody buildMessageBody(Email email) {
        MessageBody messageBody = new MessageBody();
        boolean isHtmlEmailPresent = email.getHtmlTemplateName().isPresent();
        boolean isTextEmailPresent = email.getTextTemplateName().isPresent();

        // For dvla-common-notifications we must have both html and text content
        if (!isHtmlEmailPresent && !isTextEmailPresent) {
            throw new ApplicationException(new ErrorInfo(EmailServiceError.NOTIFICATIONS_TEMPLATE_ERROR));
        }

        Optional<String> htmlTemplate = email.getHtmlTemplateName();
        if (htmlTemplate.isPresent()) {
            String emailTemplate = loadEmailTemplate(htmlTemplate.get(), emailConfiguration);
            messageBody.setHtml(populateTemplateWithDynamicData(emailTemplate, email));
        }

        Optional<String> textTemplate = email.getTextTemplateName();
        if (textTemplate.isPresent()) {
            String emailTemplate = loadEmailTemplate(textTemplate.get(), emailConfiguration);
            messageBody.setPlainText(populateTemplateWithDynamicData(emailTemplate, email));
        }

        return messageBody;
    }
}

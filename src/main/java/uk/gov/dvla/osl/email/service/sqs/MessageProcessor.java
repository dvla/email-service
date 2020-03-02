package uk.gov.dvla.osl.email.service.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.encryption.Encryption;
import uk.gov.dvla.osl.commons.error.api.response.ErrorInfo;
import uk.gov.dvla.osl.commons.exception.ApplicationException;
import uk.gov.dvla.osl.email.service.client.email.clients.EmailClient;
import uk.gov.dvla.osl.email.service.error.EmailServiceError;

import java.util.Base64;

@Slf4j
public class MessageProcessor {

    private final AmazonSQSAsync sqsClient;
    private final String queueUrl;
    private final EmailClient emailClient;
    private final ObjectMapper objectMapper;
    private final Encryption encryption;

    public MessageProcessor(AmazonSQSAsync sqsClient, String queueUrl, EmailClient emailClient, ObjectMapper objectMapper, Encryption encryption) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.emailClient = emailClient;
        this.objectMapper = objectMapper;
        this.encryption = encryption;

    }

    void process(Message message) {
        String messageId = message.getMessageId();

        try {
            // Don't log the whole message for security reasons.
            log.debug("Received message with id: {}", messageId);

            sendEmail(message);

            log.debug("About to delete message with id: {}", messageId);
            deleteMessage(message.getReceiptHandle());

        } catch (Exception e) {
            log.error("Unable to to process message with id {} so deleting it.", messageId, e);
            // Note that SES will try to re-send the email if applicable so there is no point in this
            // class doing it. See the SES documentation for more information.
            deleteMessage(message.getReceiptHandle());
        }
    }

    private void sendEmail(Message message) {

        Email email = convertMessageToEmail(message);

        if (email != null) {
            log.debug("Email to address is {} for message id: {}", email.getToAddresses(), message.getMessageId());
            emailClient.sendEmail(email);
        } else {
            log.error("Email object is null. Message ID: {}", message.getMessageId());
            throw new ApplicationException(new ErrorInfo(EmailServiceError.INVALID_EMAIL_OBJECT_ERROR));
        }
    }

    private Email convertMessageToEmail(Message message) {
        Email email = null;
        try {
            byte[] decryptedDataAsBase64 = Base64.getDecoder().decode(message.getBody());
            byte[] decryptedData = encryption.decrypt(decryptedDataAsBase64);

            email = objectMapper.readValue(decryptedData, Email.class);
        } catch (Exception e) {
            log.error("Cannot convert message to Email object, so deleting this. " +
                      "Message id is: {}, Exception message is: {}", message.getMessageId(), e.getMessage(), e);
            deleteMessage(message.getReceiptHandle());
        }

        return email;
    }

    private void deleteMessage(String receiptHandle) {
        this.sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
    }

}

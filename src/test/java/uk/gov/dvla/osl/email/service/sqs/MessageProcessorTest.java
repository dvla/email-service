package uk.gov.dvla.osl.email.service.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.encryption.Encryption;
import uk.gov.dvla.osl.commons.encryption.EncryptionUtil;
import uk.gov.dvla.osl.email.service.client.email.EmailSendingException;
import uk.gov.dvla.osl.email.service.client.email.clients.SESEmailClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageProcessorTest {

    private static final String TEST_QUEUE = "test-queue";
    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_SUBJECT = "Test subject";
    private Encryption encryption;

    @BeforeEach
    void setUp() throws Exception {
        encryption = new EncryptionUtil("1234567890123456");
    }

    @Test
    void testProcessSuccess() throws Exception {

        AmazonSQSAsync sqsClient = mock(AmazonSQSAsync.class);
        SESEmailClient SESEmailClient = mock(SESEmailClient.class);
        ObjectMapper objectMapper = getObjectMapper();

        MessageProcessor messageProcessor = new MessageProcessor(sqsClient, TEST_QUEUE, SESEmailClient, objectMapper, encryption);

        Message message = new Message();
        message.setBody(createEmailMessageAsString());

        messageProcessor.process(message);

        verify(sqsClient, times(1)).deleteMessage(any());

    }

    @Test
    void testProcessExceptionSendingEmail() throws Exception {

        AmazonSQSAsync sqsClient = mock(AmazonSQSAsync.class);
        SESEmailClient SESEmailClient = mock(SESEmailClient.class);
        ObjectMapper objectMapper = getObjectMapper();

        MessageProcessor messageProcessor = new MessageProcessor(sqsClient, TEST_QUEUE, SESEmailClient, objectMapper, encryption);

        Message message = new Message();
        message.setBody(createEmailMessageAsString());

        when(SESEmailClient.sendEmail(any(Email.class))).thenThrow(new EmailSendingException("Test email sending exception"));
        messageProcessor.process(message);

        verify(sqsClient, times(1)).deleteMessage(any());

    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

    private String createEmailMessageAsString() throws Exception {

        List<String> toAddresses = new ArrayList<>(1);

        toAddresses.add(TEST_EMAIL);

        Email email = Email.builder()
                .fromAddress(TEST_EMAIL)
                .toAddresses(toAddresses)
                .subject(TEST_SUBJECT)
                .textTemplateName("textTemplateName")
                .build();

        ObjectMapper objectMapper = getObjectMapper();
        String message = objectMapper.writeValueAsString(email);
        byte[] encryptedData = encryption.encrypt(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);

    }
}

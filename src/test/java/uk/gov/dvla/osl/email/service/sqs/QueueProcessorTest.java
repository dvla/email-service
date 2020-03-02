package uk.gov.dvla.osl.email.service.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.encryption.Encryption;
import uk.gov.dvla.osl.commons.encryption.EncryptionUtil;
import uk.gov.dvla.osl.email.service.client.email.clients.SESEmailClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueueProcessorTest {

    private AmazonSQSAsync client;
    private SESEmailClient SESEmailClient;
    private MessageProcessor messageProcessor;
    private static final String QUEUE_URL = "testQueue";
    private final List<Message> messages = new ArrayList<>();
    private final List<Message> emptyMessages = new ArrayList<>();
    private ReceiveMessageResult result;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = getObjectMapper();
        client = mock(AmazonSQSAsyncClient.class);
        SESEmailClient = mock(SESEmailClient.class);
        Encryption encryption = new EncryptionUtil("1234567890123456");
        messageProcessor = new MessageProcessor(client, QUEUE_URL, SESEmailClient, objectMapper, encryption);
        Message message = new Message();
        String originalMessage = fixture("emailMessage.json");
        byte[] encryptedData = encryption.encrypt(originalMessage.getBytes());
        String encryptedDataAsBase64 = Base64.getEncoder().encodeToString(encryptedData);
        message.setBody(encryptedDataAsBase64);
        messages.add(message);
        result = mock(ReceiveMessageResult.class);

    }

    @Test
    void queueProcessorTest() throws InterruptedException {
        QueueProcessor processor = new QueueProcessor(client, QUEUE_URL, messageProcessor);
        when(client.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(result);
        when(result.getMessages()).thenReturn(messages).thenReturn(emptyMessages);

        processor.run();

        //Needed for asynchronous tasks to be completed.
        Thread.sleep(2000);

        verify(client, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(result, atLeastOnce()).getMessages();
        verify(client, times(1)).deleteMessage(any());
        verify(SESEmailClient, times(1)).sendEmail(any(Email.class));
    }

    @Test
    void queueProcessorTestEmptyQueue() {
        QueueProcessor processor = new QueueProcessor(client, QUEUE_URL, messageProcessor);
        when(client.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(result);
        when(result.getMessages()).thenReturn(emptyMessages);

        processor.run();

        verify(client, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(result, atLeastOnce()).getMessages();
        verify(client, times(0)).deleteMessage(any());
        verifyZeroInteractions(SESEmailClient);

    }

    private ObjectMapper getObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
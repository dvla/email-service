package uk.gov.dvla.osl.email.service.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class QueueProcessor implements Runnable {
    private AmazonSQSAsync client;
    private final String queueUrl;
    private final MessageProcessor messageProcessor;

    QueueProcessor(AmazonSQSAsync client, String queueUrl, MessageProcessor messageProcessor) {
        this.client = client;
        this.queueUrl = queueUrl;
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            for (;;) {
                ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(queueUrl);
                messageRequest.setMaxNumberOfMessages(10);
                List<Message> messages = client.receiveMessage(messageRequest).getMessages();
                if (messages.isEmpty()) {
                    log.trace("Queue is empty - Waiting before checking again.");
                    break;
                }
                messages.forEach(message -> executorService.submit(() -> messageProcessor.process(message)));
            }
        } catch (Exception e) {
            log.error("Unable to process queue : [{}]", e.getMessage(), e);
        }
    }
}

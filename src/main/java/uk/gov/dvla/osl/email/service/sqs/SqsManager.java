package uk.gov.dvla.osl.email.service.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.email.service.config.SqsConfiguration;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SqsManager implements Managed {
    private final SqsConfiguration configuration;
    private AmazonSQSAsync amazonSQS;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private MessageProcessor messageProcessor;
    private static final Integer DEFAULT_DELAY = 3000;
    private Integer delay;

    private final Logger logger = LoggerFactory.getLogger(SqsManager.class);

    public SqsManager(SqsConfiguration configuration, AmazonSQSAsync amazonSQS, MessageProcessor messageProcessor) {
        this.configuration = configuration;
        this.amazonSQS = amazonSQS;
        this.messageProcessor = messageProcessor;
        this.delay = Optional.ofNullable(configuration.getQueueRecheckInterval()).orElse(DEFAULT_DELAY);
    }

    @Override
    public void start() {
        logger.info("Starting Queue Processing with configuration: {}", configuration);
        QueueProcessor processor = new QueueProcessor(amazonSQS, configuration.getQueueUrl(), messageProcessor);
        scheduledExecutorService.scheduleWithFixedDelay(processor, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        logger.info("Stopping Queue Processing");
        amazonSQS.shutdown();
        scheduledExecutorService.shutdown();
    }

}

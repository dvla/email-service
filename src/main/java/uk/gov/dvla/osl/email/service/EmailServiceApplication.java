package uk.gov.dvla.osl.email.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.osl.commons.encryption.Encryption;
import uk.gov.dvla.osl.commons.encryption.EncryptionUtil;
import uk.gov.dvla.osl.dropwizard.bundles.health.ApplicationHealthCheckBundle;
import uk.gov.dvla.osl.dropwizard.bundles.systemprops.SystemPropertySubstitutor;
import uk.gov.dvla.osl.dropwizard.bundles.version.VedVersionBundle;
import uk.gov.dvla.osl.dropwizard.filters.CorrelationIdDynamicFilters;
import uk.gov.dvla.osl.dropwizard.health.SQSHealthCheck;
import uk.gov.dvla.osl.email.service.client.SqsClient;
import uk.gov.dvla.osl.email.service.client.email.CommonNotificationsRetrofit;
import uk.gov.dvla.osl.email.service.client.email.clients.EmailClient;
import uk.gov.dvla.osl.email.service.client.email.clients.EmailClientFactory;
import uk.gov.dvla.osl.email.service.sqs.MessageProcessor;
import uk.gov.dvla.osl.email.service.sqs.SqsManager;

/**
 * The Email Service Application class.
 */
@Slf4j
public class EmailServiceApplication extends Application<EmailServiceConfiguration> {

    private static final String QUEUE_NAME = "email-service";
    private static final String SQS_HEALTH_CHECK_NAME = "sqs";
    private static final String COMMON_NOTIFICATIONS_HEALTH_CHECK = "common-notifications-health-check";
    private static final String COMMON_CLIENT = "common-notifications";
    private AmazonSQSAsync sqsClient;
    private EmailClient emailClient;

    /**
     * Main method.
     *
     * @param args the args to pass in
     * @throws Exception the exception thrown
     */
    public static void main(String[] args) throws Exception {
        new EmailServiceApplication().run(args);
    }

    /**
     * @param bootstrap configuration file of the application
     */
    @Override
    public void initialize(Bootstrap<EmailServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new VedVersionBundle("uk.gov.dvla.osl", QUEUE_NAME));
        // Allow environment variables to be used in the config file.
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new SwaggerBundle<EmailServiceConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(EmailServiceConfiguration configuration) {
                return configuration.getSwaggerBundleConfiguration();
            }
        });

        SystemPropertySubstitutor.configure(bootstrap);

        bootstrap.addBundle(new ApplicationHealthCheckBundle(SQS_HEALTH_CHECK_NAME, COMMON_NOTIFICATIONS_HEALTH_CHECK));
    }

    /**
     * Run the application.
     *
     * @param config      application configuration
     * @param environment Dropwizard environment
     * @throws Exception unable to start Dropwizard application
     */
    @Override
    public void run(EmailServiceConfiguration config, Environment environment) throws Exception {

        final ObjectMapper objectMapper = environment.getObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        emailClient = EmailClientFactory.withConfig(config).getEmailClient();

        sqsClient = SqsClient.getClient(config.getSqsConfiguration());

        SqsManager sqsManager =
                new SqsManager(config.getSqsConfiguration(), sqsClient, getMessageProcessor(config, objectMapper));

        environment.healthChecks().register(
                SQS_HEALTH_CHECK_NAME, new SQSHealthCheck(sqsClient, config.getSqsConfiguration().getQueueUrl())
        );

        if (config.getEmailConfiguration().getEmailClient().equalsIgnoreCase(COMMON_CLIENT)) {
            CommonNotificationsRetrofit commonNotificationsRetrofit = new CommonNotificationsRetrofit(
                    config.getCommonNotificationsConfig().getScheme(),
                    config.getCommonNotificationsConfig().getHost(),
                    config.getCommonNotificationsConfig().getPort(),
                    config.getCommonNotificationsConfig().getTimeoutSeconds()
            );

            environment.healthChecks().register(
                    COMMON_NOTIFICATIONS_HEALTH_CHECK, new CommonNotificationsHealthCheck(commonNotificationsRetrofit)
            );
        }

        environment.jersey().register(new CorrelationIdDynamicFilters());
        environment.lifecycle().manage(sqsManager);

        log.info("Email Service setup complete.");
    }


    private MessageProcessor getMessageProcessor(EmailServiceConfiguration config,
                                                 ObjectMapper objectMapper) throws Exception {
        Encryption encryption = new EncryptionUtil(config.getEncryptionKey());
        return new MessageProcessor(
                sqsClient,
                config.getSqsConfiguration().getQueueUrl(),
                emailClient,
                objectMapper,
                encryption
        );
    }

}
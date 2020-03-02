package uk.gov.dvla.osl.email.service.client.email.clients;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.osl.email.service.CommonNotificationsConfig;
import uk.gov.dvla.osl.email.service.EmailServiceConfiguration;
import uk.gov.dvla.osl.email.service.client.email.CommonNotificationsRetrofit;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;

/**
 * Builds the appropriate email client implementation for the running environment.
 * {@link SESEmailClient} for sending directly via Amazon SES when running in EC2;
 * {@link CommonNotificationsClient} when running in K8S;
 * {@link LogEmailClient} when running in an environment that doesn't allow emailing.
 */
@Slf4j
public class EmailClientFactory {

    private final EmailServiceConfiguration config;

    private EmailClientFactory(EmailServiceConfiguration config) {
        this.config = config;
    }

    public static EmailClientFactory withConfig(EmailServiceConfiguration config) {
        return new EmailClientFactory(config);
    }

    public EmailClient getEmailClient() {
        String configuredClient = config.getEmailConfiguration().getEmailClient();
        Clients client = Clients.fromString(configuredClient);

        switch (client) {
            case SES:
                return buildSesEmailClient(config);
            case COMMON:
                return buildCommonNotificationsClient(config);
            case LOG:
                log.info("Configuring LogEmailService, will not send emails.");
                return new LogEmailClient();
            default:
                throw new IllegalStateException("Unconfigured email client: " + client);
        }
    }

    private SESEmailClient buildSesEmailClient(EmailServiceConfiguration serviceConfig) {
        log.info("Configuring email to end via Amazon SES");

        AmazonSimpleEmailService amazonSimpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(serviceConfig.getEmailConfiguration().getRegionName())
                .withClientConfiguration(getClientConfiguration(serviceConfig.getEmailConfiguration()))
                .build();

        return new SESEmailClient(amazonSimpleEmailService, serviceConfig.getEmailConfiguration());
    }

    private CommonNotificationsClient buildCommonNotificationsClient(EmailServiceConfiguration serviceConfig) {
        log.info("Configuring email to end via common-notifications");

        CommonNotificationsConfig commonNotificationsConfig = serviceConfig.getCommonNotificationsConfig();
        CommonNotificationsRetrofit retrofit = new CommonNotificationsRetrofit(
                commonNotificationsConfig.getScheme(),
                commonNotificationsConfig.getHost(),
                commonNotificationsConfig.getPort(),
                commonNotificationsConfig.getTimeoutSeconds()
        );

        return new CommonNotificationsClient(retrofit, serviceConfig.getEmailConfiguration(), commonNotificationsConfig);
    }

    private ClientConfiguration getClientConfiguration(EmailConfiguration emailConfiguration) {
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withConnectionTimeout(emailConfiguration.getConnectionTimeout())
                .withConnectionMaxIdleMillis(emailConfiguration.getConnectionMaxIdleMillis())
                .withRequestTimeout(emailConfiguration.getRequestTimeout());

        if (emailConfiguration.getProxy() != null
            && emailConfiguration.getProxy().getHost() != null
            && emailConfiguration.getProxy().getPort() != null) {
            clientConfiguration.setProxyHost(emailConfiguration.getProxy().getHost());
            clientConfiguration.setProxyPort(emailConfiguration.getProxy().getPort());
        }

        return clientConfiguration;
    }

}

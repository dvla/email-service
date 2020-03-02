package uk.gov.dvla.osl.email.service.client.email.clients;

import org.junit.jupiter.api.Test;
import uk.gov.dvla.osl.email.service.CommonNotificationsConfig;
import uk.gov.dvla.osl.email.service.EmailServiceConfiguration;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailClientFactoryTest {

    private final EmailServiceConfiguration mockEmailServiceConfig = mock(EmailServiceConfiguration.class);
    private final EmailConfiguration mockEmailConfiguration = mock(EmailConfiguration.class);
    private final CommonNotificationsConfig mockCommonConfig = mock(CommonNotificationsConfig.class);

    @Test
    void testReturnsSESClient_WithCorrectValues() {
        EmailClientFactory factoryUnderTest = EmailClientFactory.withConfig(mockEmailServiceConfig);

        when(mockEmailServiceConfig.getEmailConfiguration()).thenReturn(mockEmailConfiguration);
        when(mockEmailConfiguration.getEmailClient()).thenReturn("ses");
        when(mockEmailConfiguration.getRegionName()).thenReturn("eu-west-2");
        when(mockEmailConfiguration.getConnectionTimeout()).thenReturn(5000);
        when(mockEmailConfiguration.getConnectionMaxIdleMillis()).thenReturn(5000);
        when(mockEmailConfiguration.getRequestTimeout()).thenReturn(5000);

        assertThat(factoryUnderTest.getEmailClient()).isInstanceOf(SESEmailClient.class);
    }

    @Test
    void testReturnsCommonClient_WithCorrectValues() {
        EmailClientFactory factoryUnderTest = EmailClientFactory.withConfig(mockEmailServiceConfig);

        when(mockEmailServiceConfig.getEmailConfiguration()).thenReturn(mockEmailConfiguration);
        when(mockEmailServiceConfig.getCommonNotificationsConfig()).thenReturn(mockCommonConfig);
        when(mockEmailConfiguration.getEmailClient()).thenReturn("common-notifications");
        when(mockEmailConfiguration.getRegionName()).thenReturn("eu-west-2");
        when(mockCommonConfig.getScheme()).thenReturn("https");
        when(mockCommonConfig.getHost()).thenReturn("test-host");
        when(mockCommonConfig.getPort()).thenReturn("9200");
        when(mockCommonConfig.getTimeoutSeconds()).thenReturn(5);

        assertThat(factoryUnderTest.getEmailClient()).isInstanceOf(CommonNotificationsClient.class);
    }

    @Test
    void testReturnsLogClient_WithCorrectValues() {
        EmailClientFactory factoryUnderTest = EmailClientFactory.withConfig(mockEmailServiceConfig);

        when(mockEmailServiceConfig.getEmailConfiguration()).thenReturn(mockEmailConfiguration);
        when(mockEmailConfiguration.getEmailClient()).thenReturn("log");

        assertThat(factoryUnderTest.getEmailClient()).isInstanceOf(LogEmailClient.class);
    }

}
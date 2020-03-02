package uk.gov.dvla.osl.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailServiceConfigurationTest {

    private YamlConfigurationFactory<EmailServiceConfiguration> factory;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        Validator validator = Validators.newValidator();
        factory = new YamlConfigurationFactory<>(EmailServiceConfiguration.class, validator, objectMapper, "dw");
    }

    @Test
    void testAppConfigurationIsValid() throws Exception {
        final EmailServiceConfiguration config = factory.build(
                new SubstitutingSourceProvider(
                        new ResourceConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ), "config.yaml");
        assertNotNull(config);
    }

    @Test
    void testConfigurationMapsCorrectly() throws Exception {
        final CommonNotificationsConfig commonNotificationsConfig = factory.build(
                new SubstitutingSourceProvider(
                        new ResourceConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                ), "config-k8s.yaml").getCommonNotificationsConfig();

        assertAll(() -> {
                    assertNotNull(commonNotificationsConfig);
                    assertThat(commonNotificationsConfig.getScheme()).isEqualTo("http");
                    assertThat(commonNotificationsConfig.getPort()).isEqualTo("9200");
                    assertThat(commonNotificationsConfig.getHost()).isEqualTo("dvla-common-notifications.notification-${NOTIFICATIONS_ENVIRONMENT}.svc.cluster.local");
                    assertThat(commonNotificationsConfig.getTimeoutSeconds()).isEqualTo(120);
                }
        );
    }
}

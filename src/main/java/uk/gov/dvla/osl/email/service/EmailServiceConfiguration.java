package uk.gov.dvla.osl.email.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;
import uk.gov.dvla.osl.email.service.config.SqsConfiguration;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmailServiceConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private SqsConfiguration sqsConfiguration = new SqsConfiguration();

    @NotNull
    @JsonProperty
    private EmailConfiguration emailConfiguration = new EmailConfiguration();

    @JsonProperty(required = false)
    private CommonNotificationsConfig commonNotificationsConfig = new CommonNotificationsConfig();

    @NotNull
    @NotEmpty
    @JsonProperty(value = "encryptionKey")
    private String encryptionKey;

    @JsonProperty("swagger")
    private SwaggerBundleConfiguration swaggerBundleConfiguration;

    public EmailServiceConfiguration setEmailConfiguration(EmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
        return this;
    }

}
package uk.gov.dvla.osl.email.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class EmailConfiguration {

    @NotEmpty
    @JsonProperty
    private boolean emailSendingEnabled;

    @NotEmpty
    @JsonProperty
    private String regionName;

    @NotEmpty
    @JsonProperty
    private Integer connectionTimeout;

    @NotEmpty
    @JsonProperty
    private Integer connectionMaxIdleMillis;

    @NotEmpty
    @JsonProperty
    private Integer requestTimeout;

    @JsonProperty
    private ProxyConfiguration proxy = new ProxyConfiguration();

    @NotEmpty
    @JsonProperty
    private String templatePath;

    @NotEmpty
    @JsonProperty
    private String emailClient;
}

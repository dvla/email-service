package uk.gov.dvla.osl.email.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommonNotificationsConfig {

    @JsonProperty
    private String port;

    @JsonProperty
    private String scheme;

    @JsonProperty
    private String host;

    @JsonProperty
    private int timeoutSeconds;

    @JsonProperty
    private String resourceHeader;

    @JsonProperty
    private String originResourceHeader;
}

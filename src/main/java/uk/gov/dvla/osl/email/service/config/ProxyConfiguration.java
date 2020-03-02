package uk.gov.dvla.osl.email.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;

public class ProxyConfiguration {


    @NotEmpty
    @JsonProperty
    private String host;

    @NotEmpty
    @JsonProperty
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .toString();
    }
}

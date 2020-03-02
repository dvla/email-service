package uk.gov.dvla.osl.email.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class SqsConfiguration {

    @NotEmpty
    @JsonProperty
    private String queueUrl;

    @NotEmpty
    @JsonProperty
    private String regionName;

    @JsonProperty
    private Integer queueRecheckInterval;

    @JsonProperty
    private String endPoint;

    @NotNull
    @JsonProperty
    private ProxyConfiguration sqsProxy = new ProxyConfiguration();

    public String getQueueUrl() {
        return queueUrl;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public Integer getQueueRecheckInterval() {
        return queueRecheckInterval;
    }

    public void setQueueRecheckInterval(Integer queueRecheckInterval) {
        this.queueRecheckInterval = queueRecheckInterval;
    }

    public ProxyConfiguration getSqsProxy() {
        return sqsProxy;
    }

    public void setSqsProxy(ProxyConfiguration sqsProxy) {
        this.sqsProxy = sqsProxy;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("queueUrl", queueUrl)
                .add("regionName", regionName)
                .add("queueRecheckInterval", queueRecheckInterval)
                .add("endPoint", endPoint)
                .add("sqsProxy", sqsProxy)
                .toString();
    }
}

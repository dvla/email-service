package uk.gov.dvla.osl.email.service.client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import uk.gov.dvla.osl.email.service.config.SqsConfiguration;

public class SqsClient {

    public static AmazonSQSAsync getClient(SqsConfiguration configuration) {

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProxyHost(configuration.getSqsProxy().getHost());
        clientConfiguration.setProxyPort(configuration.getSqsProxy().getPort());
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.
                EndpointConfiguration(configuration.getEndPoint(), configuration.getRegionName());
        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
        builder.setClientConfiguration(clientConfiguration);
        builder.setEndpointConfiguration(endpointConfiguration);
        return builder.build();
    }
}

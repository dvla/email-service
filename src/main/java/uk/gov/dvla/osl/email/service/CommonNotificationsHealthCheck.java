package uk.gov.dvla.osl.email.service;

import com.codahale.metrics.health.HealthCheck;
import lombok.RequiredArgsConstructor;
import retrofit2.Response;
import uk.gov.dvla.osl.email.service.client.email.CommonNotificationsRetrofit;
import uk.gov.dvla.osl.email.service.client.email.entities.HealthResponse;

@RequiredArgsConstructor
public class CommonNotificationsHealthCheck extends HealthCheck {

    private final CommonNotificationsRetrofit commonNotificationsRetrofitClient;

    private static final String ERR_MSG = "Common Notifications service unavailable. Status: %s";

    @Override
    protected Result check() throws Exception {
        Response<HealthResponse> healthCheckResponse = commonNotificationsRetrofitClient.healthCheck();
        String status = healthCheckResponse.body().getStatus();

        return status.equalsIgnoreCase("up")
                ? Result.healthy()
                : Result.unhealthy(ERR_MSG, status);
    }
}

package uk.gov.dvla.osl.email.service.client.email;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import uk.gov.dvla.osl.email.service.client.email.entities.HealthResponse;
import uk.gov.dvla.osl.email.service.client.email.entities.Message;
import uk.gov.dvla.osl.email.service.client.email.entities.Success;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit HTTP client implementation for making calls to the dvla-common-notifications service
 */
public class CommonNotificationsRetrofit {

    private final NotificationsRetrofitService httpclient;

    public CommonNotificationsRetrofit(String scheme, String host, String port, long timeoutSeconds) {
        final HttpUrl baseUrl = new HttpUrl.Builder().scheme(scheme).host(host).port(Integer.parseInt(port)).build();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        this.httpclient = retrofit.create(NotificationsRetrofitService.class);
    }

    /**
     * Send email message to dvla-common-notifications service.
     *
     * @param message              constructed {@link Message} object
     * @param resourceHeader       intermediary service
     * @param originResourceHeader originating service
     * @return Response containing a success object
     * @throws IOException execute() throws this
     */
    public Response<Success> sendEmailRequest(Message message,
                                              String resourceHeader,
                                              String originResourceHeader) throws IOException {
        return this.httpclient.sendEmailRequest(message, resourceHeader, originResourceHeader).execute();
    }

    /**
     * Call the actuator health endpoint of notifications service.
     *
     * @return Response object containing health status
     * @throws IOException execute() throws this
     */
    public Response<HealthResponse> healthCheck() throws IOException {
        return this.httpclient.healthCheck().execute();
    }

    /**
     * Retrofit client interface
     */
    private interface NotificationsRetrofitService {

        @POST(value = "/v2/notifications/emails")
        Call<Success> sendEmailRequest(@Body final Message message,
                                       @Header(value = "DVLA-Resource") String resourceHeader,
                                       @Header(value = "DVLA-Origin-Resource") String originResourceHeader);

        @GET(value = "/actuator/health")
        Call<HealthResponse> healthCheck();
    }
}

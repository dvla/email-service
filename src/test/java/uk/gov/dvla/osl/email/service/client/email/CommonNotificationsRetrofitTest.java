package uk.gov.dvla.osl.email.service.client.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import uk.gov.dvla.osl.email.service.client.email.entities.Message;
import uk.gov.dvla.osl.email.service.client.email.entities.MessageBody;
import uk.gov.dvla.osl.email.service.client.email.entities.Success;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.dvla.osl.email.service.client.email.SESEmailClientTest.*;

class CommonNotificationsRetrofitTest {

    public static final String SCHEME = "http";
    public static final String HOST = "localhost";
    public static final String PORT = "9200";

    private static final WireMockServer WIREMOCK = new WireMockServer(Integer.parseInt(PORT));

    CommonNotificationsRetrofit commonNotificationsRetrofit = new CommonNotificationsRetrofit(SCHEME, HOST, PORT, 120);

    static List<String> TO_ADDRESSES = new ArrayList<>();
    static List<String> BCC_ADDRESSES = new ArrayList<>();
    static List<String> CC_ADDRESSES = new ArrayList<>();

    @BeforeEach
    void setup() {
        TO_ADDRESSES.add(TEST_EMAIL);
        BCC_ADDRESSES.add(TEST_EMAIL);
        CC_ADDRESSES.add(TEST_EMAIL);

        WIREMOCK.start();
    }

    @AfterEach
    void tearDown() {
        TO_ADDRESSES.clear();
        WIREMOCK.resetMappings();
        WIREMOCK.stop();
    }

    @Test
    void testEmail_SuccessfullyHitsEndpoint() throws IOException {

        Message validMessage = buildValidMessage();

        String jsonResponse = new ObjectMapper().writeValueAsString(new Success(MESSAGE_ID));

        WIREMOCK.stubFor(post(urlPathMatching("/v2/notifications/emails"))
                .willReturn(aResponse()
                        .withBody(jsonResponse)));

        Response<Success> response = commonNotificationsRetrofit.sendEmailRequest(validMessage, "", "");

        WIREMOCK.verify(postRequestedFor(urlPathMatching("/v2/notifications/emails")));
        assertThat(response.body().getEmailId()).isEqualTo(MESSAGE_ID);
    }

    private static Message buildValidMessage() {

        return Message.builder()
                .subject(TEST_SUBJECT)
                .to(TO_ADDRESSES)
                .from(TEST_EMAIL)
                .bcc(BCC_ADDRESSES)
                .cc(CC_ADDRESSES)
                .body(MessageBody.builder()
                        .plainText("plainText")
                        .html("html")
                        .build())
                .build();
    }

}
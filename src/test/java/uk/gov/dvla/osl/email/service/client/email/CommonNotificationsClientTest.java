package uk.gov.dvla.osl.email.service.client.email;

import com.amazonaws.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Response;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.email.service.CommonNotificationsConfig;
import uk.gov.dvla.osl.email.service.client.email.clients.CommonNotificationsClient;
import uk.gov.dvla.osl.email.service.client.email.clients.EmailClient;
import uk.gov.dvla.osl.email.service.client.email.entities.Message;
import uk.gov.dvla.osl.email.service.client.email.entities.MessageBody;
import uk.gov.dvla.osl.email.service.client.email.entities.Success;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.dvla.osl.email.service.client.email.CommonNotificationsRetrofitTest.*;
import static uk.gov.dvla.osl.email.service.client.email.SESEmailClientTest.*;

@ExtendWith(MockitoExtension.class)
class CommonNotificationsClientTest {

    private static EmailConfiguration emailConfiguration = mock(EmailConfiguration.class);
    private static CommonNotificationsConfig commonNotificationsConfig = mock(CommonNotificationsConfig.class);
    private static EmailClient emailClient = mock(EmailClient.class);
    private static ClassLoader mockClassLoader = mock(ClassLoader.class);

    @Mock
    private CommonNotificationsRetrofit mockCommonsRetrofit;

    @Test
    void testSendEmail() throws IOException {
        CommonNotificationsClient client =
                new CommonNotificationsClient(mockCommonsRetrofit, emailConfiguration, commonNotificationsConfig);

        Response<Success> responseSuccess = Response.success(new Success(MESSAGE_ID));

        when(commonNotificationsConfig.getResourceHeader()).thenReturn("email-service");
        when(commonNotificationsConfig.getOriginResourceHeader()).thenReturn("rave-web");
        when(emailConfiguration.isEmailSendingEnabled()).thenReturn(true);
        when(emailConfiguration.getTemplatePath()).thenReturn("templates/");
        when(mockCommonsRetrofit.sendEmailRequest(
                buildValidMessage(),
                "email-service",
                "rave-web"))
                .thenReturn(responseSuccess);

        String result = client.sendEmail(buildValidEmail());
        assertThat(result).isEqualTo("123");
    }

    @Test
    void testBuildMessage() throws IOException {
        CommonNotificationsClient client =
                new CommonNotificationsClient(mockCommonsRetrofit, emailConfiguration, commonNotificationsConfig);

        InputStream htmlStream = this.getClass().getClassLoader().getResourceAsStream("templates/ved.feedback.survey.tpl.html");
        InputStream textStream = this.getClass().getClassLoader().getResourceAsStream("templates/ved.feedback.survey.tpl.txt");
        String htmlString = IOUtils.toString(htmlStream);
        String textString = IOUtils.toString(textStream);

        when(emailConfiguration.getTemplatePath()).thenReturn("templates/");
        when(emailClient.loadEmailTemplate("ved.feedback.survey.tpl.html", emailConfiguration))
                .thenReturn(IOUtils.toString(htmlStream));
        when(emailClient.loadEmailTemplate("ved.feedback.survey.tpl.txt", emailConfiguration))
                .thenReturn(IOUtils.toString(textStream));
        when(mockClassLoader.getResourceAsStream("templates/ved.feedback.survey.tpl.html"))
                .thenReturn(htmlStream);
        when(mockClassLoader.getResourceAsStream("templates/ved.feedback.survey.tpl.txt"))
                .thenReturn(textStream);

        Message message = client.buildMessage(buildValidEmail());

        assertAll(() -> {
            assertThat(message.getBody().getHtml()).isEqualTo(htmlString);
            assertThat(message.getBody().getPlainText()).isEqualTo(textString);
            assertThat(message.getFrom()).isEqualTo(TEST_EMAIL);
            assertThat(message.getTo()).isEqualTo(TO_ADDRESSES);
            assertThat(message.getSubject()).isEqualTo(TEST_SUBJECT);
            assertThat(message.getBcc()).isEqualTo(BCC_ADDRESSES);
            assertThat(message.getCc()).isEqualTo(CC_ADDRESSES);
        });
    }

    @Test
    void testSuccessEntity() {
        try {
            Success builderSuccess = Success.builder().emailId(MESSAGE_ID).build();
            assertThat(builderSuccess.getEmailId()).isEqualTo(MESSAGE_ID);

            Success constructorSuccess = new Success(MESSAGE_ID);
            assertThat(constructorSuccess.getEmailId()).isEqualTo(MESSAGE_ID);

            assertThat(constructorSuccess.getEmailId()).isEqualTo(MESSAGE_ID);
            constructorSuccess.setEmailId("890");
            assertThat(constructorSuccess.getEmailId()).isEqualTo("890");

            new Success();

        } catch (Exception ex) {
            fail("Instantiation of Success object, or getter/setter method call failed!", ex);
        }
    }

    private static Email buildValidEmail() {
        return Email.builder()
                .toAddresses(TO_ADDRESSES)
                .fromAddress(TEST_EMAIL)
                .subject(TEST_SUBJECT)
                .bccAddresses(BCC_ADDRESSES)
                .ccAddresses(CC_ADDRESSES)
                .dynamicData(Collections.emptyMap())
                .htmlTemplateName("ved.feedback.survey.tpl.html")
                .textTemplateName("ved.feedback.survey.tpl.txt")
                .build();
    }

    private Message buildValidMessage() {
        TO_ADDRESSES.add(TEST_EMAIL);
        BCC_ADDRESSES.add(TEST_EMAIL);
        CC_ADDRESSES.add(TEST_EMAIL);

        return Message.builder()
                .subject(TEST_SUBJECT)
                .to(TO_ADDRESSES)
                .from(TEST_EMAIL)
                .bcc(BCC_ADDRESSES)
                .cc(CC_ADDRESSES)
                .body(MessageBody.builder()
                        .plainText("${feedback.survey.text}")
                        .html("<p>${feedback.survey.text}</p>")
                        .build())
                .build();
    }

}

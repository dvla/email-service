package uk.gov.dvla.osl.email.service.client.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.exception.ApplicationException;
import uk.gov.dvla.osl.email.service.client.email.clients.SESEmailClient;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link SESEmailClient}. Uses both a mocked version of {@link AmazonSimpleEmailService} and
 * a real version to ensure adequate test coverage.
 */
class SESEmailClientTest {

    public static final String MESSAGE_ID = "123";
    public static final String TEST_EMAIL = "vedrtest@gmail.com";
    public static final String TEST_SUBJECT = "Test subject";

    private AmazonSimpleEmailService mockedAmazonSimpleEmailService;
    private SESEmailClient SESEmailClientWithMockedAmazonService;

    private List<String> toAddresses;

    private String textTemplateName;

    private String htmlTemplateName;

    @BeforeEach
    void setUp() {

        EmailConfiguration emailConfiguration = getEmailConfiguration();
        emailConfiguration.setEmailSendingEnabled(true);

        mockedAmazonSimpleEmailService = mock(AmazonSimpleEmailService.class);
        SESEmailClientWithMockedAmazonService = new SESEmailClient(mockedAmazonSimpleEmailService, emailConfiguration);

        SendEmailResult sendEmailResult = new SendEmailResult();
        sendEmailResult.setMessageId(MESSAGE_ID);

        when(mockedAmazonSimpleEmailService.sendEmail(any(SendEmailRequest.class))).thenReturn(sendEmailResult);

        toAddresses = new ArrayList<>(1);
        toAddresses.add(TEST_EMAIL);

        textTemplateName = "testTextEmailTemplate.tpl.txt";
        htmlTemplateName = "testHtmlEmailTemplate.tpl.html";
    }

    @Test
    void testSendEmail_NoTemplateName() {

        Email emailWithNoFromAddress = Email.builder().subject(TEST_SUBJECT).toAddresses(toAddresses).build();

        assertThrows(ApplicationException.class, () -> SESEmailClientWithMockedAmazonService.sendEmail(emailWithNoFromAddress));
    }

    @Test
    void testSendEmail_NoFromAddress() {

        Email emailWithNoFromAddress = Email.builder().subject(TEST_SUBJECT).toAddresses(toAddresses).htmlTemplateName(htmlTemplateName).build();

        assertThrows(ApplicationException.class, () -> SESEmailClientWithMockedAmazonService.sendEmail(emailWithNoFromAddress));
    }

    @Test
    void testSendEmail_NoToAddress() {

        Email emailWithNoFromAddress = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).textTemplateName(textTemplateName).build();

        assertThrows(ApplicationException.class, () -> SESEmailClientWithMockedAmazonService.sendEmail(emailWithNoFromAddress));
    }

    @Test
    void testSendEmail_Success_WithTextBody() {

        Email validEmailWithTextBody = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).toAddresses(toAddresses)
                .textTemplateName(textTemplateName).build();

        String result = SESEmailClientWithMockedAmazonService.sendEmail(validEmailWithTextBody);

        assertEquals(MESSAGE_ID, result);
    }

    @Test
    void testSendEmail_Success_WithTextBodyAndDynamicDataPopulatedWhenNullValue() {

        Map<String, String> dynamicData = new HashMap<>(1);
        dynamicData.put("registration.name", null);

        Email validEmailWithTextBody = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).toAddresses(toAddresses)
                .textTemplateName(textTemplateName)
                .dynamicData(dynamicData)
                .build();

        Destination expectedDestination = new Destination().withToAddresses(toAddresses);
        Body expectedBody = new Body().withText(new Content().withData("Test email template for "));
        Message expectedMessage = new Message().withBody(expectedBody).withSubject(new Content().withData(TEST_SUBJECT));

        SendEmailRequest expectedSendEmailRequest = new SendEmailRequest().withSource(TEST_EMAIL).withDestination(expectedDestination)
                .withMessage(expectedMessage);

        String result = SESEmailClientWithMockedAmazonService.sendEmail(validEmailWithTextBody);

        assertEquals(MESSAGE_ID, result);

        verify(mockedAmazonSimpleEmailService).sendEmail(expectedSendEmailRequest);
    }

    @Test
    void testSendEmail_Success_WithTextBodyAndDynamicDataPopulated() {

        Map<String, String> dynamicData = new HashMap<>(1);
        dynamicData.put("registration.name", "test name");

        Email validEmailWithTextBody = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).toAddresses(toAddresses)
                .textTemplateName(textTemplateName)
                .dynamicData(dynamicData)
                .build();

        Destination expectedDestination = new Destination().withToAddresses(toAddresses);
        Body expectedBody = new Body().withText(new Content().withData("Test email template for test name"));
        Message expectedMessage = new Message().withBody(expectedBody).withSubject(new Content().withData(TEST_SUBJECT));

        SendEmailRequest expectedSendEmailRequest = new SendEmailRequest().withSource(TEST_EMAIL).withDestination(expectedDestination)
                .withMessage(expectedMessage);

        String result = SESEmailClientWithMockedAmazonService.sendEmail(validEmailWithTextBody);

        assertEquals(MESSAGE_ID, result);

        verify(mockedAmazonSimpleEmailService).sendEmail(expectedSendEmailRequest);
    }

    @Test
    void testSendEmail_Success_WithHtmlBodyAndDynamicDataPopulated() {

        Map<String, String> dynamicData = new HashMap<>(1);
        dynamicData.put("registration.name", "test name");

        Email validEmailWithHtmlBody = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).toAddresses(toAddresses)
                .htmlTemplateName(htmlTemplateName)
                .dynamicData(dynamicData)
                .build();

        Destination expectedDestination = new Destination().withToAddresses(toAddresses);
        Body expectedBody = new Body().withHtml(new Content().withData("<html><body>Test email template for test name</body></html>"));
        Message expectedMessage = new Message().withBody(expectedBody).withSubject(new Content().withData(TEST_SUBJECT));

        SendEmailRequest expectedSendEmailRequest = new SendEmailRequest().withSource(TEST_EMAIL).withDestination(expectedDestination)
                .withMessage(expectedMessage);

        String result = SESEmailClientWithMockedAmazonService.sendEmail(validEmailWithHtmlBody);

        assertEquals(MESSAGE_ID, result);

        verify(mockedAmazonSimpleEmailService).sendEmail(expectedSendEmailRequest);
    }

    @Test
    void testSendEmail_Success_WithHtmlBody() {

        Email validEmailWithHtmlBody = Email.builder().subject(TEST_SUBJECT).fromAddress(TEST_EMAIL).toAddresses(toAddresses)
                .htmlTemplateName(htmlTemplateName).build();

        String result = SESEmailClientWithMockedAmazonService.sendEmail(validEmailWithHtmlBody);

        assertEquals(MESSAGE_ID, result);
    }

    private EmailConfiguration getEmailConfiguration() {
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setRegionName("test region");
        emailConfiguration.setRequestTimeout(1000);
        emailConfiguration.setConnectionMaxIdleMillis(1000);
        emailConfiguration.setConnectionTimeout(1000);
        emailConfiguration.setTemplatePath("templates/");
        return emailConfiguration;
    }
}

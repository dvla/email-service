package uk.gov.dvla.osl.email.service.client.email.clients;

import lombok.extern.slf4j.Slf4j;
import uk.gov.dvla.osl.commons.Email;

/**
 * When email-sending is disabled i.e. in dev/stage we come here to log the email
 */
@Slf4j
public class LogEmailClient implements EmailClient {

    @Override
    public String sendEmail(Email email) {

        log.info("Email sending disabled with email: {}", email.getSubject().orElse(""));

        return "";
    }

}

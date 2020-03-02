package uk.gov.dvla.osl.email.service.client.email.clients;

import com.amazonaws.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.text.StringSubstitutor;
import uk.gov.dvla.osl.commons.Email;
import uk.gov.dvla.osl.commons.error.api.response.ErrorInfo;
import uk.gov.dvla.osl.commons.exception.ApplicationException;
import uk.gov.dvla.osl.email.service.config.EmailConfiguration;
import uk.gov.dvla.osl.email.service.error.EmailServiceError;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface EmailClient {

    String sendEmail(Email email);

    default String loadEmailTemplate(String emailTemplateName, EmailConfiguration emailConfiguration) {
        try {
            String fullPathToTemplate;
            ClassLoader classLoader = getClass().getClassLoader();
            fullPathToTemplate = emailConfiguration.getTemplatePath() + emailTemplateName;
            InputStream template = Objects.requireNonNull(classLoader.getResourceAsStream(fullPathToTemplate));

            return IOUtils.toString(template);

        } catch (IOException | NullPointerException ex) {
            throw new ApplicationException(
                    new ErrorInfo(EmailServiceError.UNEXPECTED_EMAIL_TEMPLATE_LOAD_ERROR), ex);
        }
    }

    default String populateTemplateWithDynamicData(String emailTemplate, Email email) {

        Optional<Map<String, String>> testDynamicData = email.getDynamicData();
        if (testDynamicData.isPresent()) {
            Map<String, String> dynamicData = testDynamicData.get();

            // replace values which are null with blanks to avoid NullPointerExceptions when substituting
            for (Map.Entry<String, String> item : dynamicData.entrySet()) {
                if (item.getValue() == null) {
                    dynamicData.put(item.getKey(), "");
                }
            }

            ImmutableMap<String, Object> emailData = ImmutableMap.<String, Object>builder().putAll(dynamicData).build();
            StringSubstitutor strSubstitutor = new StringSubstitutor(emailData);
            emailTemplate = strSubstitutor.replace(emailTemplate);
        }

        return emailTemplate;
    }
}

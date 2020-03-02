package uk.gov.dvla.osl.email.service.client.email.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.dvla.osl.email.service.client.email.validation.NotEmptyAndNonEmptyStringValues;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @NotEmpty
    private String from;

    @NotEmptyAndNonEmptyStringValues
    private List<String> to;

    private List<String> cc;

    private List<String> bcc;

    @NotEmpty
    private String subject;

    @NotNull
    @Valid
    private MessageBody body;

}

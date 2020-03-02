package uk.gov.dvla.osl.email.service.client.email.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBody {

    private String plainText;

    private String html;

}

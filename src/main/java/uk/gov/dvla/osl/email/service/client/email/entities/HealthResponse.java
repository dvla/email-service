package uk.gov.dvla.osl.email.service.client.email.entities;

import lombok.Data;

/**
 * POJO representing response from common-notifications actuator/health endpoint
 */
@Data
public class HealthResponse {

    private String status;

}

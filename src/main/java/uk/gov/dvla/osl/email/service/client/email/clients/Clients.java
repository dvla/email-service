package uk.gov.dvla.osl.email.service.client.email.clients;

import java.util.stream.Stream;

public enum Clients {
    SES("ses"),
    COMMON("common-notifications"),
    LOG("log");

    private String name;

    Clients(String name) {
        this.name = name;
    }

    private String getName() {
        return this.name;
    }

    public static Clients fromString(String name) {
        return Stream.of(Clients.values())
                .filter(client -> client.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

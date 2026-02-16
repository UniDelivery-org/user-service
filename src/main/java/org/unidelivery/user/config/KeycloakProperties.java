package org.unidelivery.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeycloakProperties {
    private String keycloakServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String adminUsername;
    private String adminPassword;
}

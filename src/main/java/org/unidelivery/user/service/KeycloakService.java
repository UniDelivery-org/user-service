package org.unidelivery.user.service;

import org.unidelivery.user.config.KeycloakProperties;
import org.unidelivery.user.dto.LoginRequest;
import org.unidelivery.user.dto.AuthResponse;
import org.unidelivery.user.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {
    
    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;

    /**
     * Login user and get JWT token
     */
    public AuthResponse login(LoginRequest request) {
        String tokenUrl = properties.getKeycloakServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("username", request.getEmail());
        body.add("password", request.getPassword());
        body.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            AuthResponse loginResponse = new AuthResponse();
            loginResponse.setAccessToken((String) responseBody.get("access_token"));
            loginResponse.setTokenType((String) responseBody.get("token_type"));
            loginResponse.setExpiresIn(((Number) responseBody.get("expires_in")).longValue());
            loginResponse.setRefreshToken((String) responseBody.get("refresh_token"));

            log.info("User logged in successfully: {}", request.getEmail());
            return loginResponse;

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new RuntimeException("Invalid email or password");
        }
    }

    /**
     * Get admin access token for user management
     */
    private String getAdminToken() {
        String tokenUrl = properties.getKeycloakServerUrl() + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", properties.getAdminUsername());
        body.add("password", properties.getAdminPassword());
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            log.error("Failed to get admin token", e);
            throw new RuntimeException("Failed to authenticate with Keycloak admin");
        }
    }

    /**
     * Create user in Keycloak
     */
    public String createUser(RegisterRequest request) {
        String token = getAdminToken();
        String usersUrl = properties.getKeycloakServerUrl() + "/admin/realms/" + properties.getRealm() + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", request.getEmail());
        userData.put("email", request.getEmail());
        userData.put("enabled", true);
        userData.put("emailVerified", false);

        String[] nameParts = request.getFullName().trim().split("\\s+", 2);
        userData.put("firstName", nameParts[0]);
        userData.put("lastName", nameParts.length > 1 ? nameParts[1] : "");

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getPassword());
        credential.put("temporary", false);
        userData.put("credentials", List.of(credential));

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phone", List.of(request.getPhone()));
        userData.put("attributes", attributes);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userData, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    usersUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            String location = response.getHeaders().getLocation().toString();
            String keycloakId = location.substring(location.lastIndexOf('/') + 1);

            assignRoleToUser(keycloakId, request.getRole().name());

            log.info("Created Keycloak user with ID: {}", keycloakId);
            return keycloakId;

        } catch (Exception e) {
            log.error("Failed to create Keycloak user: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage());
        }
    }

    /**
     * Assign role to user
     */
    private void assignRoleToUser(String userId, String roleName) {
        String token = getAdminToken();

        String rolesUrl = properties.getKeycloakServerUrl() + "/admin/realms/" + properties.getRealm() + "/roles/" + roleName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(rolesUrl, HttpMethod.GET, entity, Map.class);
        String roleId = (String) roleResponse.getBody().get("id");

        String userRolesUrl = properties.getKeycloakServerUrl() + "/admin/realms/" + properties.getRealm() + "/users/" + userId + "/role-mappings/realm";

        List<Map<String, String>> roles = new ArrayList<>();
        Map<String, String> role = new HashMap<>();
        role.put("id", roleId);
        role.put("name", roleName);
        roles.add(role);

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        postHeaders.setBearerAuth(token);
        HttpEntity<List<Map<String, String>>> roleEntity = new HttpEntity<>(roles, postHeaders);

        restTemplate.exchange(userRolesUrl, HttpMethod.POST, roleEntity, Void.class);
        log.info("Assigned role {} to user {}", roleName, userId);
    }
}
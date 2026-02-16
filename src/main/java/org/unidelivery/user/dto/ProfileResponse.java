package org.unidelivery.user.dto;

import lombok.Data;
import org.unidelivery.user.model.UserRole;
import org.unidelivery.user.model.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

@Data
public class ProfileResponse {
    private UUID id;
    private String keycloakId;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private VerificationStatus verificationStatus;
    private Boolean isOnline;
    private Boolean isBlocked;
    private Double currentLat;
    private Double currentLon;
    private String avatarUrl;
    private Instant createdAt;
}

package org.unidelivery.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import org.unidelivery.user.dto.*;
import org.unidelivery.user.exception.InvalidCredentialsException;
import org.unidelivery.user.service.KeycloakService;
import org.unidelivery.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final KeycloakService keycloakService;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        ProfileResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = keycloakService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(JwtAuthenticationToken jwt) {
        if (jwt== null) {
            throw new InvalidCredentialsException("Missing or invalid token");
        }
        String keycloakId = jwt.getToken().getSubject();
        ProfileResponse profile = userService.getUserProfile(keycloakId);

        return ResponseEntity.ok(profile);
    }
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateProfile(JwtAuthenticationToken jwt,@Valid @ModelAttribute UpdateProfileRequestDTO request) {
        if (jwt== null) {
            throw new InvalidCredentialsException("Missing or invalid token");
        }
        String keycloakId = jwt.getToken().getSubject();
        ProfileResponse profile = userService.updateProfile(keycloakId, request);
        return ResponseEntity.ok(profile);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        AuthResponse response = keycloakService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteProfile(JwtAuthenticationToken jwt) {
        if (jwt== null) {
            throw new InvalidCredentialsException("Missing or invalid token");
        }
        String keycloakId = jwt.getToken().getSubject();

        keycloakService.deleteProfile(keycloakId);
        userService.deleteProfile(keycloakId);
        return ResponseEntity.ok("Profile deleted successfully");
    }
    @GetMapping("{id}")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}
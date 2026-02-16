package org.unidelivery.user.controller;

import org.unidelivery.user.dto.LoginRequest;
import org.unidelivery.user.dto.AuthResponse;
import org.unidelivery.user.dto.RegisterRequest;
import org.unidelivery.user.dto.ProfileResponse;
import org.unidelivery.user.service.KeycloakService;
import org.unidelivery.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
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
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        ProfileResponse profile = userService.getUserProfile(keycloakId);
        return ResponseEntity.ok(profile);
    }
}
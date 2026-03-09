package org.unidelivery.user.service;

import org.mapstruct.control.MappingControl;
import org.unidelivery.user.dto.RegisterRequest;
import org.unidelivery.user.dto.ProfileResponse;
import org.unidelivery.user.dto.UpdateProfileRequestDTO;
import org.unidelivery.user.exception.UserAlreadyExistsException;
import org.unidelivery.user.exception.UserNotFoundException;
import org.unidelivery.user.mapper.UserMapper;
import org.unidelivery.user.model.User;
import org.unidelivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final FileStorageService fileStorageService;
    private final UserMapper mapper;

    @Transactional
    public ProfileResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("User with phone " + request.getPhone() + " already exists");
        }
        String keycloakId = keycloakService.createUser(request);

        User user = mapper.toUser(request);
        user.setKeycloakId(keycloakId);

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}", savedUser.getEmail());

        return mapper.toProfileResponse(savedUser);
    }

    public ProfileResponse getUserProfile(String keycloakId) {
        User keycloakUser = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapper.toProfileResponse(keycloakUser);
    }
    public ProfileResponse getUserProfile(UUID userId) {
        return mapper.toProfileResponse(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));
    }
    @Transactional
    public ProfileResponse updateProfile(String keycloakId, UpdateProfileRequestDTO requestDTO) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found with this id: " + keycloakId));
        mapper.updateEntityFromDto(requestDTO, user);
        if (requestDTO.isDeleteAvatar()) {
            user.setAvatarUrl(null);
        }
        else if (requestDTO.getAvatar() != null && !requestDTO.getAvatar().isEmpty()) {
            String avatarUrl = fileStorageService.storeAvatar(requestDTO.getAvatar());
            user.setAvatarUrl(avatarUrl);
        }
        keycloakService.updateUser(keycloakId, requestDTO);
        return mapper.toProfileResponse(user);
    }
    public void deleteProfile(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId).orElseThrow(() -> new UserNotFoundException("User not found with this id: " + keycloakId));
        userRepository.delete(user);
    }
}
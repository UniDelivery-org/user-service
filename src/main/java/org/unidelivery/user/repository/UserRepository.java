package org.unidelivery.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unidelivery.user.model.User;
import org.unidelivery.user.model.UserRole;
import org.unidelivery.user.model.VerificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
    List<User> findByRoleAndVerificationStatus(UserRole role, VerificationStatus status);
}
package com.aech.auth.exam_backend.auth.Repos;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aech.auth.exam_backend.auth.Entities.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByJti(String jti);
}

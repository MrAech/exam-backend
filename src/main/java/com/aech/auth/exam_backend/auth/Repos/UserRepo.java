package com.aech.auth.exam_backend.auth.Repos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aech.auth.exam_backend.auth.Entities.User;

public interface UserRepo extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}

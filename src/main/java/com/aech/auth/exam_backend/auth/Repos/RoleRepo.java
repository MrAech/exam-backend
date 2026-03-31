package com.aech.auth.exam_backend.auth.Repos;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aech.auth.exam_backend.auth.Entities.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(String name);
}

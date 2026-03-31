package com.aech.auth.exam_backend.DTOs;

import com.aech.auth.exam_backend.auth.entities.Provider;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

  private UUID id;
  private String email;

  private String name;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  private boolean enable = true;

  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();
  private Provider provider = Provider.LOCAL;

  private Set<RoleDto> roles = new HashSet<>();
}

package com.aech.auth.exam_backend.auth.Entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class Role {

  @Id
  private UUID id = UUID.randomUUID();

  @Column(unique = true, nullable = false)
  private String name;

}

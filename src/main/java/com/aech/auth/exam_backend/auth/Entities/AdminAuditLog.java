package com.aech.auth.exam_backend.auth.Entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class AdminAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String actorEmail;

  @Column(nullable = false)
  private String action;

  private UUID targetUserId;

  private String targetEmail;

  @Column(length = 1200)
  private String metadata;

  @Column(nullable = false)
  private Instant createdAt;

}

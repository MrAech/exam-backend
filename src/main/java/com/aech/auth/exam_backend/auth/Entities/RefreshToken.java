package com.aech.auth.exam_backend.auth.Entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "refresh_tokens_jti_idx", columnList = "jti", unique = true),
    @Index(name = "refresh_tokens_user_id_idx", columnList = "user_id")
})
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false, updatable = false)
  private String jti;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(updatable = false, nullable = false)
  private Instant createAt;

  @Column(nullable = false)
  private Instant expiredAt;

  @Column(nullable = false)
  private boolean revoked;

  private String replacedByToken;

}

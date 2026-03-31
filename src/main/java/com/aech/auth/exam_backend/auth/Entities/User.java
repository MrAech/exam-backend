package com.aech.auth.exam_backend.auth.Entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "users") // had to do it cause of h2 having "user" as keyword and getting angry
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, length = 300, nullable = false)
  private String email;

  @Column(length = 500)
  private String name;

  private String password;
  private boolean enable = true;

  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  @Enumerated(EnumType.STRING)
  private Provider provider = Provider.LOCAL;
  private String prividerId;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<Role> roles = new HashSet<>();

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null)
      createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

}

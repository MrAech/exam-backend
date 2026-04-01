package com.aech.auth.exam_backend.exam.Entities;

import java.time.Instant;
import java.util.UUID;

import com.aech.auth.exam_backend.auth.Entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class ExamAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(nullable = false)
  private Instant startedAt;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant submittedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AttemptStatus status;

  @Column(nullable = false)
  private int warningsCount;

}

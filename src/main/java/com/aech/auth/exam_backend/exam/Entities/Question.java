package com.aech.auth.exam_backend.exam.Entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(nullable = false, length = 1200)
  private String text;

  @Column(nullable = false)
  private String optionA;

  @Column(nullable = false)
  private String optionB;

  @Column(nullable = false)
  private String optionC;

  @Column(nullable = false)
  private String optionD;

  @Column(nullable = false)
  private int correctOption;

}

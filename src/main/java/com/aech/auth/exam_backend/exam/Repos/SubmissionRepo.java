package com.aech.auth.exam_backend.exam.Repos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aech.auth.exam_backend.exam.Entities.Submission;

public interface SubmissionRepo extends JpaRepository<Submission, UUID> {
  List<Submission> findByUserEmailOrderBySubmittedAtDesc(String userEmail);

  List<Submission> findByExamId(UUID examId);
}

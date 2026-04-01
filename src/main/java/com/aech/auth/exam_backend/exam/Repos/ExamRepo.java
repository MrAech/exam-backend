package com.aech.auth.exam_backend.exam.Repos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aech.auth.exam_backend.exam.Entities.Exam;

public interface ExamRepo extends JpaRepository<Exam, UUID> {

  List<Exam> findByActiveTrueOrderByCreatedAtDesc();

  List<Exam> findByCreatedByEmailOrderByCreatedAtDesc(String email);
}

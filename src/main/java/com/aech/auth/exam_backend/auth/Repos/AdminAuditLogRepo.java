package com.aech.auth.exam_backend.auth.Repos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aech.auth.exam_backend.auth.Entities.AdminAuditLog;

public interface AdminAuditLogRepo extends JpaRepository<AdminAuditLog, UUID> {

  List<AdminAuditLog> findTop100ByOrderByCreatedAtDesc();
}

package com.aech.auth.exam_backend.DTOs;

import java.time.Instant;
import java.util.UUID;

public record AdminAuditLogDto(
    UUID id,
    String actorEmail,
    String action,
    UUID targetUserId,
    String targetEmail,
    String metadata,
    Instant createdAt) {

  // HAKUNAMA TATA

}

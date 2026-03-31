package com.aech.auth.exam_backend.DTOs;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
    String message,
    HttpStatus status,
    int statusCode) {
  // JHA JHA JHA Hei....nope
}

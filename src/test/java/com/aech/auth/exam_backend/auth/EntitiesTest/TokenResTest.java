package com.aech.auth.exam_backend.auth.EntitiesTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Entities.TokenRes;
import com.fasterxml.jackson.databind.ObjectMapper;

// Test for Token Res Serialization 

class TokenResTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void tokenResDoesNotExposeRefTknField() throws Exception {

    UserDto user = UserDto.builder()
        .email("student@example.com")
        .name("Student")
        .build();

    TokenRes res = TokenRes.of("access-token", 900, user);

    String json = objectMapper.writeValueAsString(res);

    assertTrue(json.contains("accessToken"));
    assertTrue(json.contains("expiresIn"));
    assertTrue(json.contains("tokenType"));
    assertFalse(json.contains("refreshToken"));
  }
}

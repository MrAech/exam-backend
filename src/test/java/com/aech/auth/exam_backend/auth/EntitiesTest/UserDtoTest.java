package com.aech.auth.exam_backend.auth.EntitiesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserDtoTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void passIsWriteOnlyNotSerialInRes() throws Exception {

    UserDto dto = UserDto.builder()
        .email("student@example.com")
        .name("student")
        .password("secret-value :|")
        .build();

    String json = objectMapper.writeValueAsString(dto);

    assertFalse(json.contains("password"));
    assertTrue(json.contains("student@example.com"));
  }

  @Test
  void passIsAccepDuringDeSer() throws Exception {

    String json = """
        {
          "email": "student@example.com",
            "name": "Student",
            "password": "supper Sec"
        }
          """;

    UserDto dto = objectMapper.readValue(json, UserDto.class);

    assertEquals("student@example.com", dto.getEmail());
    assertEquals("supper Sec", dto.getPassword());
  }
}

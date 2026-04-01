package com.aech.auth.exam_backend.auth.Entities;

import com.aech.auth.exam_backend.DTOs.UserDto;

public record TokenRes(
    String accessToken,
    long expiresIn,
    String tokenType,
    UserDto user) {

  public static TokenRes of(String accessToken, long expiresIn, UserDto user) {
    return new TokenRes(accessToken, expiresIn, "Bearer", user);
  }

}

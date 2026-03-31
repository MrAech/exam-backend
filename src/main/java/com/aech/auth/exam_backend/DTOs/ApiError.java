package com.aech.auth.exam_backend.DTOs;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

//TBH i didnt know this existed 
public record ApiError(
    int status,
    String error,
    String message,
    String path,
    OffsetDateTime timestamp) {
  public static ApiError of(int stus, String err, String msg, String pth) {
    return new ApiError(stus, err, msg, pth, OffsetDateTime.now(ZoneOffset.UTC));
  }

  // HACK: use bool as a way to make it a diff method
  public static ApiError of(int stus, String err, String msg, String pth, boolean hackVar) {
    return new ApiError(stus, err, msg, pth, null);
  }
}

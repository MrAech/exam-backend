package com.aech.auth.exam_backend.auth.Config;

public class AppConstants {

  public static final String[] AUTH_PUBLIC_URLS = {
      "/api/v1/auth/login",
      "/api/v1/auth/register",
      "/api/v1/auth/refresh",
      "/api/v1/auth/logout",
      "/oauth2/**",
      "/login/oauth2/**"
  };

  public static final String[] DOCS_PUBLIC_URLS = {
      "/v3/api-docs/**",
      "/swagger-ui.html",
      "/swagger-ui/**"
  };

  public static final String STUDENT_ROLE = "STUDENT";
  public static final String TEACHER_ROLE = "TEACHER";
  public static final String ADMIN_ROLE = "ADMIN";
}

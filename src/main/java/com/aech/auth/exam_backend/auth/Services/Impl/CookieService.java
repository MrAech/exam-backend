package com.aech.auth.exam_backend.auth.Services.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Service
@Getter
public class CookieService {

  private final String refreshTokenCookieName;
  private final boolean cookieHttpOnly;
  private final boolean cookieSecure;
  private final String cookieDomain;
  private final String cookieSameSite;

  private final Logger logger = LoggerFactory.getLogger(CookieService.class);

  public CookieService(
      @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
      @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
      @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
      @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
      @Value("${security.jwt.cookie-domain}") String cookieDomain) {
    this.refreshTokenCookieName = refreshTokenCookieName;
    this.cookieHttpOnly = cookieHttpOnly;
    this.cookieSecure = cookieSecure;
    this.cookieDomain = cookieDomain;
    this.cookieSameSite = cookieSameSite;
  }

  public void attachRefreshCookie(HttpServletResponse res, String value, int maxAge) {
    logger.info("Attaching cookie with name  : {}", refreshTokenCookieName);
    var resCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
        .httpOnly(cookieHttpOnly)
        .secure(cookieSecure)
        .path("/")
        .maxAge(maxAge)
        .sameSite(cookieSameSite);

    if (cookieDomain != null && !cookieDomain.isBlank()) {
      resCookieBuilder.domain(cookieDomain);
    }

    ResponseCookie resCookie = resCookieBuilder.build();
    res.addHeader(HttpHeaders.SET_COOKIE, resCookie.toString());
  }

  public void clearRefreshToken(HttpServletResponse res) {
    var builder = ResponseCookie.from(refreshTokenCookieName, "")
        .maxAge(0)
        .httpOnly(cookieHttpOnly)
        .path("/")
        .sameSite(cookieSameSite)
        .secure(cookieSecure);

    if (cookieDomain != null && !cookieDomain.isBlank()) {
      builder.domain(cookieDomain);
    }

    ResponseCookie resCookie = builder.build();

    res.addHeader(HttpHeaders.SET_COOKIE, resCookie.toString());
  }

  public void addNoStoreHeaders(HttpServletResponse res) {
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    res.setHeader("Pragma", "n-cache");
  }

}

package com.aech.auth.exam_backend.auth.Services.Impl;

import com.aech.auth.exam_backend.auth.Entities.Role;
import com.aech.auth.exam_backend.auth.Entities.User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureAlgorithm;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class JwtService {
  private final SecretKey key;
  private final long accessTTLSec;
  private final long refreshTTLSec;
  private final String issuer;

  // this is why no @Data which i love a bit
  public JwtService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.access-ttl-seconds}") long accessTTLSec,
      @Value("${security.jwt.refresh-ttl-seconds}") long refreshTTLSec,
      @Value("${security.jwt.issuer") String issuer) {

    if (secret == null || secret.length() < 64) {
      throw new IllegalArgumentException("Invalid Secret, either missing or less than 64 char");
    }

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTTLSec = accessTTLSec;
    this.refreshTTLSec = refreshTTLSec;
    this.issuer = issuer;
  }

  // Generate tokens babiiii
  public String generateAccessToken(User user) {
    Instant now = Instant.now();
    List<String> roles = user.getRoles() == null ? List.of() : user.getRoles().stream().map(Role::getName).toList();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(user.getId().toString())
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessTTLSec)))
        .claims(Map.of(
            "email", user.getEmail(),
            "roles", roles,
            "typ", "access"))
        .signWith(key, Jwts.SIG.HS512)
        .compact();
  }

  public String generateRefreshToken(User user, String jti) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(jti)
        .subject(user.getId().toString())
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(refreshTTLSec)))
        .claim("typ", "refresh")
        .signWith(key, Jwts.SIG.HS512)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }

  public boolean isAccessToken(String token) {
    Claims c = parse(token).getPayload();
    return "access".equals(c.get("typ"));
  }

  public boolean isRefreshToken(String token) {
    Claims c = parse(token).getPayload();
    return "refresh".equals(c.get("typ"));
  }

  public UUID getUserId(String token) {
    Claims c = parse(token).getPayload();
    return UUID.fromString(c.getSubject());
  }

  public String getJti(String token) {
    return parse(token).getPayload().getId();
  }

  public List<String> getRoles(String token) {
    Claims c = parse(token).getPayload();
    return (List<String>) c.get("roles");
  }

  public String getEmail(String token) {
    Claims c = parse(token).getPayload();
    return (String) c.get("email");
  }

}

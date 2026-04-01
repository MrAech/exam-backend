package com.aech.auth.exam_backend.auth.Controllers;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Entities.LoginReq;
import com.aech.auth.exam_backend.auth.Entities.RefreshToken;
import com.aech.auth.exam_backend.auth.Entities.RefreshTokenReq;
import com.aech.auth.exam_backend.auth.Entities.TokenRes;
import com.aech.auth.exam_backend.auth.Entities.User;
import com.aech.auth.exam_backend.auth.Repos.RefreshTokenRepo;
import com.aech.auth.exam_backend.auth.Repos.UserRepo;
import com.aech.auth.exam_backend.auth.Services.AuthService;
import com.aech.auth.exam_backend.auth.Services.Impl.CookieService;
import com.aech.auth.exam_backend.auth.Services.Impl.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final RefreshTokenRepo refreshTokenRepo;

  private final AuthenticationManager authenticationManager;
  private final UserRepo userRepo;
  private final JwtService jwtService;
  private final ModelMapper mapper;
  private final CookieService cookieService;

  @Value("${app.cors.front-end-url}")
  private String allowedOrigins;

  @PostMapping("/login")
  public ResponseEntity<TokenRes> login(@RequestBody LoginReq loginReq, HttpServletResponse res) {

    authenticate(loginReq);
    User user = userRepo.findByEmail(loginReq.email())
        .orElseThrow(() -> new BadCredentialsException("Invalid Username or Password"));

    if (!user.isEnable()) {
      throw new DisabledException("User is not Active contact admin"); // MEEEE
    }

    String jti = UUID.randomUUID().toString();
    var refTokenObj = RefreshToken.builder()
        .jti(jti)
        .user(user)
        .createAt(Instant.now())
        .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTTLSec()))
        .revoked(false)
        .build();

    refreshTokenRepo.save(Objects.requireNonNull(refTokenObj));

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user, refTokenObj.getJti());

    cookieService.attachRefreshCookie(res, refreshToken, (int) jwtService.getRefreshTTLSec());
    cookieService.addNoStoreHeaders(res);

    TokenRes tokenResponse = TokenRes.of(accessToken, jwtService.getAccessTTLSec(),
        mapper.map(user, UserDto.class));

    return ResponseEntity.ok(tokenResponse);
  }

  private Authentication authenticate(LoginReq req) {

    try {
      return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid Username or Passwrod");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenRes> refreshToken(
      @RequestBody(required = false) RefreshTokenReq body,
      HttpServletResponse res,
      HttpServletRequest req) throws InterruptedException {

    validateTrustedOrigin(req);

    String refreshToken = readRefreshTokenFromReq(body, req)
        .orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));

    if (!jwtService.isRefreshToken(refreshToken)) {
      throw new BadCredentialsException("Invalid Refresh Token Type");
    }

    String jti = jwtService.getJti(refreshToken);
    UUID userId = jwtService.getUserId(refreshToken);
    RefreshToken storedRefToken = refreshTokenRepo.findByJti(jti)
        .orElseThrow(() -> new BadCredentialsException("Refresh Token not recognized"));

    if (storedRefToken.isRevoked()) {
      throw new BadCredentialsException("Refresh Token revoked");
    }

    if (storedRefToken.getExpiredAt().isBefore(Instant.now())) {
      throw new BadCredentialsException("Refresh Token Expired");
    }

    if (!storedRefToken.getUser().getId().equals(userId)) {
      throw new BadCredentialsException("Refresh token does not belog to user");
    }

    storedRefToken.setRevoked(true);
    String newJti = UUID.randomUUID().toString();
    storedRefToken.setReplacedByToken(newJti);
    refreshTokenRepo.save(storedRefToken);

    User user = storedRefToken.getUser();

    var newRefTokenObj = RefreshToken.builder()
        .jti(newJti)
        .user(user)
        .createAt(Instant.now())
        .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTTLSec()))
        .revoked(false)
        .build();

    refreshTokenRepo.save(Objects.requireNonNull(newRefTokenObj));

    String newAccessToken = jwtService.generateAccessToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user, newRefTokenObj.getJti());

    cookieService.attachRefreshCookie(res, newRefreshToken, (int) jwtService.getRefreshTTLSec());
    cookieService.addNoStoreHeaders(res);

    return ResponseEntity
        .ok(TokenRes.of(newAccessToken, jwtService.getAccessTTLSec(), mapper.map(user, UserDto.class)));

  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
    validateTrustedOrigin(req);

    readRefreshTokenFromReq(null, req).ifPresent(token -> {
      try {
        if (jwtService.isRefreshToken(token)) {
          String jti = jwtService.getJti(token);
          refreshTokenRepo.findByJti(jti).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
          });
        }
      } catch (JwtException ignored) {
        // NOTE: Nothing yet may add logger later
      }
    });

    cookieService.clearRefreshToken(res);
    cookieService.addNoStoreHeaders(res);
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private Optional<String> readRefreshTokenFromReq(RefreshTokenReq body, HttpServletRequest req) {

    if (req.getCookies() != null) {

      Optional<String> fromCookie = Arrays.stream(req.getCookies())
          .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
          .map(Cookie::getValue)
          .filter(v -> !v.isBlank())
          .findFirst();

      if (fromCookie.isPresent()) {
        return fromCookie;
      }
    }

    return Optional.empty();
  }

  private void validateTrustedOrigin(HttpServletRequest req) {
    String origin = req.getHeader("Origin");

    if (origin == null || origin.isBlank()) {
      origin = req.getHeader("Referer");
    }

    if (origin == null || origin.isBlank()) {
      throw new BadCredentialsException("Missing Trusted origin");
    }

    String normalized = normalizeOrigin(origin);
    boolean allowed = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(this::normalizeOrigin)
        .anyMatch(normalized::equalsIgnoreCase);

    if (!allowed) {
      throw new BadCredentialsException("Untrusted Origin");
    }
  }

  private String normalizeOrigin(String value) {
    try {
      URI uri = URI.create(value);
      if (uri.getScheme() == null || uri.getHost() == null) {
        return value;
      }
      int port = uri.getPort();
      if (port > 0) {
        return uri.getScheme() + "://" + uri.getHost() + ":" + port;
      }
      return uri.getScheme() + "://" + uri.getHost();
    } catch (Exception ignored) {
      return value;
    }
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> me(Authentication auth) {

    if (auth == null || auth.getName() == null) {
      throw new BadCredentialsException("Not Authorized");
    }

    User user = userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new BadCredentialsException("User not Found"));

    return ResponseEntity.ok(mapper.map(user, UserDto.class));
  }
}

package com.aech.auth.exam_backend.auth.Config;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.aech.auth.exam_backend.auth.Entities.Provider;
import com.aech.auth.exam_backend.auth.Entities.RefreshToken;
import com.aech.auth.exam_backend.auth.Entities.Role;
import com.aech.auth.exam_backend.auth.Entities.User;
import com.aech.auth.exam_backend.auth.Repos.RefreshTokenRepo;
import com.aech.auth.exam_backend.auth.Repos.RoleRepo;
import com.aech.auth.exam_backend.auth.Repos.UserRepo;
import com.aech.auth.exam_backend.auth.Services.Impl.CookieService;
import com.aech.auth.exam_backend.auth.Services.Impl.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final UserRepo userRepo;
  private final RoleRepo roleRepo;
  private final JwtService jwtService;
  private final CookieService cookieService;
  private final RefreshTokenRepo refreshTokenRepo;

  @Value("${app.auth.frontend.success-redirect}")
  private String frontEndSuccessUrl;

  @Value("${app.auth.role-bootstrap.admin-emails:}")
  private String adminEmails;

  @Value("${app.auth.role-bootstrap.teacher-emails:}")
  private String teacherEmails;

  @Override
  @Transactional
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    logger.info("Successful authentication");

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    String registrationId = "unknown";
    if (authentication instanceof OAuth2AuthenticationToken token) {
      registrationId = token.getAuthorizedClientRegistrationId();
    }

    logger.info("OAuth2 provider: {}", registrationId);

    if (!"google".equals(registrationId)) {
      throw new RuntimeException("Only Google OAuth is enabled");
    }

    String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
    String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
    String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();

    Role studentRole = roleRepo.findByName("ROLE_" + AppConstants.STUDENT_ROLE)
        .orElseThrow(() -> new RuntimeException("Student role missing"));

    Role teacherRole = roleRepo.findByName("ROLE_" + AppConstants.TEACHER_ROLE)
        .orElseThrow(() -> new RuntimeException("Teacher role missing"));

    Role adminRole = roleRepo.findByName("ROLE_" + AppConstants.ADMIN_ROLE)
        .orElseThrow(() -> new RuntimeException("Admin role missing"));

    Set<Role> desiredRoles = new HashSet<>();
    desiredRoles.add(studentRole);

    if (emailInList(email, teacherEmails)) {
      desiredRoles.add(teacherRole);
    }
    if (emailInList(email, adminEmails)) {
      desiredRoles.add(adminRole);
    }

    User newUser = User.builder()
        .email(email)
        .name(name)
        .enable(true)
        .provider(Provider.GOOGLE)
        .prividerId(googleId)
        .roles(new HashSet<>(desiredRoles))
        .build();

    User user = userRepo.findByEmail(email).map(existingUser -> {
      if (existingUser.getRoles() == null) {
        existingUser.setRoles(new HashSet<>());
      }
      existingUser.getRoles().addAll(desiredRoles);
      return userRepo.save(existingUser);
    }).orElseGet(() -> userRepo.save(newUser));

    String jti = UUID.randomUUID().toString();
    RefreshToken refreshTokenOb = RefreshToken.builder()
        .jti(jti)
        .user(user)
        .revoked(false)
        .createAt(Instant.now())
        .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTTLSec()))
        .build();

    refreshTokenRepo.save(refreshTokenOb);

    String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());
    cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTTLSec());
    response.sendRedirect(frontEndSuccessUrl);
  }

  private boolean emailInList(String email, String csv) {
    if (email == null || email.isBlank() || csv == null || csv.isBlank()) {
      return false;
    }

    String normalized = email.trim().toLowerCase();
    String[] parts = csv.split(",");
    for (String part : parts) {
      if (normalized.equals(part.trim().toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}

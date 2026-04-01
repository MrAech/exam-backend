package com.aech.auth.exam_backend.auth.Config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
  public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication authentication) throws IOException, ServletException {
    
    logger.info("Successful authentication");

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    String registrationId = "unknown";

    if(authentication instanceof OAuth2AuthenticationToken token){
      registrationId = token.getAuthorizedClientRegistrationId();
    }

    logger.info("OAuth2 Provider  : {}", registrationId);

    if(!"google".equals(registrationId)) {
      throw new RuntimeException()
    }
  }
}

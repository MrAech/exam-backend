package com.aech.auth.exam_backend.auth.TestControllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.aech.auth.exam_backend.DTOs.RoleDto;
import com.aech.auth.exam_backend.DTOs.UserDto;
import com.aech.auth.exam_backend.auth.Controllers.AuthController;
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

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

// This is made mainly to test cookie flow 

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;
  @Mock
  private RefreshTokenRepo refreshTokenRepo;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private UserRepo userRepo;
  @Mock
  private JwtService jwtService;
  @Mock
  private ModelMapper mapper;
  @Mock
  private CookieService cookieService;

  @Mock
  private Authentication authentication;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(
        authService,
        refreshTokenRepo,
        authenticationManager,
        userRepo,
        jwtService,
        mapper,
        cookieService);
    ReflectionTestUtils.setField(authController, "allowedOrigins", "http://localhost:5173");
  }

  @Test
  void login_setsRefreshCookieAndReturnsAccessTokenOnly() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("student@example.com")
        .password("hashed")
        .enable(true)
        .build();

    UserDto userDto = UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .roles(Set.of(RoleDto.builder().name("ROLE_STUDENT").build()))
        .build();

    MockHttpServletResponse response = new MockHttpServletResponse();

    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(userRepo.findByEmail("student@example.com")).thenReturn(Optional.of(user));
    when(jwtService.getRefreshTTLSec()).thenReturn(86400L);
    when(jwtService.getAccessTTLSec()).thenReturn(900L);
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(eq(user), anyString())).thenReturn("refresh-token");
    when(mapper.map(user, UserDto.class)).thenReturn(userDto);

    ResponseEntity<TokenRes> result = authController.login(
        new LoginReq("student@example.com", "password"),
        response);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals("access-token", result.getBody().accessToken());

    verify(cookieService).attachRefreshCookie(eq(response), eq("refresh-token"), eq(86400));
    verify(cookieService).addNoStoreHeaders(response);
    verify(refreshTokenRepo).save(any(RefreshToken.class));
  }

  @Test
  void refresh_rotatesToken_andAttachesNewCookie() throws Exception {
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .id(userId)
        .email("student@example.com")
        .enable(true)
        .build();

    UserDto userDto = UserDto.builder()
        .id(userId)
        .email(user.getEmail())
        .build();

    RefreshToken existing = RefreshToken.builder()
        .id(UUID.randomUUID())
        .jti("old-jti")
        .user(user)
        .createAt(Instant.now().minusSeconds(60))
        .expiredAt(Instant.now().plusSeconds(3600))
        .revoked(false)
        .build();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "refresh-token"));
    request.addHeader("Origin", "http://localhost:5173");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(cookieService.getRefreshTokenCookieName()).thenReturn("refreshToken");
    when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
    when(jwtService.getJti("refresh-token")).thenReturn("old-jti");
    when(jwtService.getUserId("refresh-token")).thenReturn(userId);
    when(refreshTokenRepo.findByJti("old-jti")).thenReturn(Optional.of(existing));
    when(jwtService.getRefreshTTLSec()).thenReturn(86400L);
    when(jwtService.getAccessTTLSec()).thenReturn(900L);
    when(jwtService.generateAccessToken(user)).thenReturn("new-access");
    when(jwtService.generateRefreshToken(eq(user), anyString())).thenReturn("new-refresh");
    when(mapper.map(user, UserDto.class)).thenReturn(userDto);

    ResponseEntity<TokenRes> result = authController.refreshToken(
        new RefreshTokenReq(null),
        response,
        request);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals("new-access", result.getBody().accessToken());

    ArgumentCaptor<RefreshToken> saveCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepo, times(2)).save(saveCaptor.capture());
    assertTrue(saveCaptor.getAllValues().getFirst().isRevoked());

    verify(cookieService).attachRefreshCookie(eq(response), eq("new-refresh"), eq(86400));
    verify(cookieService).addNoStoreHeaders(response);
  }

  @Test
  void logout_revokesRefreshTokenWhenValid_andClearsCookie() {
    User user = User.builder().id(UUID.randomUUID()).email("student@example.com").build();
    RefreshToken existing = RefreshToken.builder()
        .id(UUID.randomUUID())
        .jti("logout-jti")
        .user(user)
        .createAt(Instant.now().minusSeconds(120))
        .expiredAt(Instant.now().plusSeconds(3600))
        .revoked(false)
        .build();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "logout-refresh-token"));
    request.addHeader("Origin", "http://localhost:5173");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(cookieService.getRefreshTokenCookieName()).thenReturn("refreshToken");
    when(jwtService.isRefreshToken("logout-refresh-token")).thenReturn(true);
    when(jwtService.getJti("logout-refresh-token")).thenReturn("logout-jti");
    when(refreshTokenRepo.findByJti("logout-jti")).thenReturn(Optional.of(existing));

    ResponseEntity<Void> result = authController.logout(request, response);

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    assertTrue(existing.isRevoked());
    verify(refreshTokenRepo).save(existing);
    verify(cookieService).clearRefreshToken(response);
    verify(cookieService).addNoStoreHeaders(response);
  }

  @Test
  void logout_ignoresMalformedRefreshToken_andStillClearsCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "invalid-token"));
    request.addHeader("Origin", "http://localhost:5173");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(cookieService.getRefreshTokenCookieName()).thenReturn("refreshToken");
    when(jwtService.isRefreshToken("invalid-token")).thenThrow(new JwtException("bad token"));

    ResponseEntity<Void> result = authController.logout(request, response);

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    verify(refreshTokenRepo, never()).save(any());
    verify(cookieService).clearRefreshToken(response);
    verify(cookieService).addNoStoreHeaders(response);
  }

  @Test
  void refresh_rejectsBodyTokenWhenCookieMissing() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Origin", "http://localhost:5173");
    MockHttpServletResponse response = new MockHttpServletResponse();

    BadCredentialsException ex = assertThrows(
        BadCredentialsException.class,
        () -> authController.refreshToken(new RefreshTokenReq("body-token"), response, request));

    assertTrue(ex.getMessage().contains("Refresh token is missing"));
  }
}
